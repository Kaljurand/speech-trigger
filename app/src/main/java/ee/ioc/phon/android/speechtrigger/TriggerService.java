package ee.ioc.phon.android.speechtrigger;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.speech.RecognitionService;
import android.speech.RecognizerIntent;

import java.io.File;
import java.io.IOException;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;
import ee.ioc.phon.android.speechutils.Extras;
import ee.ioc.phon.android.speechutils.utils.BundleUtils;
import ee.ioc.phon.android.speechutils.utils.PreferenceUtils;

public class TriggerService extends RecognitionService {
    private static final String KWS_SEARCH = "wakeup";
    private static final String LANGUAGE_EN_US = "en-US";
    private static final String LANGUAGE_ET_EE = "et-EE";

    private SpeechRecognizer recognizer;

    private RecognitionService.Callback mCallback;

    @Override
    protected void onStartListening(Intent intent, Callback callback) {
        mCallback = callback;
        Log.i("onStartListening");
        runRecognizerSetup(intent);
    }

    @Override
    protected void onCancel(Callback callback) {
        Log.i("onCancel");
        results(new Bundle());
    }

    @Override
    protected void onStopListening(Callback callback) {
        Log.i("onStopListening");
        results(new Bundle());
    }


    private void runRecognizerSetup(final Intent intent) {
        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(TriggerService.this);
                    File assetDir = assets.syncAssets();
                    Bundle extras = intent.getExtras();
                    if (extras == null) {
                        extras = new Bundle();
                    }
                    setupRecognizer(assetDir, extras);
                } catch (IOException e) {
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception result) {
                if (result != null) {
                    Log.e("Failed to init recognizer " + result);
                    error(android.speech.SpeechRecognizer.ERROR_CLIENT);
                } else {
                    switchSearch(KWS_SEARCH);
                    readyForSpeech(new Bundle());
                }
            }
        }.execute();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        cancel();
    }

    private void cancel() {
        if (recognizer != null) {
            recognizer.cancel();
            recognizer.shutdown();
        }
    }

    private void switchSearch(String searchName) {
        recognizer.stop();
        recognizer.startListening(searchName);
    }

    private void setupRecognizer(File assetsDir, Bundle extras) throws IOException {
        String model = "est16k.cd_ptm_1000-mapadapt";
        //String model = "est16k.cd_cont_3000-mapadapt";
        String fileDict = "cmudict-et-ee.dict";
        String lang = extras.getString(RecognizerIntent.EXTRA_LANGUAGE);
        if ("en_US".equals(lang) || "en-us".equals(lang) || "en".equals(lang)) {
            lang = LANGUAGE_EN_US;
        }

        if (LANGUAGE_EN_US.equals(lang)) {
            model = "en-us-ptm";
            fileDict = "cmudict-en-us.dict";
        }

        recognizer = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(new File(assetsDir, model))
                .setDictionary(new File(assetsDir, fileDict))
                //.setKeywordThreshold(1e-30f)
                .setKeywordThreshold(1e-20f)
                //.setRawLogDir(assetsDir) // To disable logging of raw audio comment out this call (takes a lot of space on the device)
                //.setInteger("-vad_postspeech", 125) // TODO: test
                .getRecognizer();

        recognizer.addListener(new RecognitionListener() {
            @Override
            public void onBeginningOfSpeech() {
                beginningOfSpeech();
            }

            @Override
            public void onEndOfSpeech() {
            }

            @Override
            public void onPartialResult(Hypothesis hypothesis) {
                if (hypothesis != null) {
                    String text = hypothesis.getHypstr();
                    Log.i(text + " " + hypothesis.getBestScore() + " " + hypothesis.getProb());
                    results(BundleUtils.createResultsBundle(text));
                }
            }

            @Override
            public void onResult(Hypothesis hypothesis) {
            }

            @Override
            public void onError(Exception e) {
                Log.i(e.getMessage());
                // TODO: map the exception to the error code
                // Unfortunately not many errors get here (e.g. missing word in lexicon)
                error(android.speech.SpeechRecognizer.ERROR_CLIENT);
            }

            @Override
            public void onTimeout() {
                switchSearch(KWS_SEARCH);
            }
        });

        String phrase = getPhrase(extras, lang);
        if (phrase == null || phrase.isEmpty()) {
            String phrases = "et-ee.phrases.txt";
            if (LANGUAGE_EN_US.equals(lang)) {
                phrases = "en-us.phrases.txt";
            }
            recognizer.addKeywordSearch(KWS_SEARCH, new File(assetsDir, phrases));
            Log.i("Listening to multiple phrases: " + phrases);
        } else {
            if (LANGUAGE_ET_EE.equals(lang)) {
                // TODO: future work
                //Dict dict = Dict.create(phrase);
                // TODO: store it into the dict-file before the recognizer is constructed
                //Log.i("Dict: " + dict.toString());
            }
            recognizer.addKeyphraseSearch(KWS_SEARCH, phrase);
            Log.i("Listening to the single phrase: " + phrase);
        }
    }

    private String getPhrase(Bundle extras, String lang) {
        String phrase = extras.getString(Extras.EXTRA_PHRASE);
        if (phrase == null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            if (LANGUAGE_EN_US.equals(lang)) {
                return PreferenceUtils.getPrefString(prefs, getResources(), R.string.keyPhraseEnUs);
            }
            return PreferenceUtils.getPrefString(prefs, getResources(), R.string.keyPhraseEtEe);
        }
        return phrase;
    }


    private void readyForSpeech(Bundle bundle) {
        try {
            mCallback.readyForSpeech(bundle);
        } catch (RemoteException e) {
            // empty
        }
    }

    private void results(Bundle bundle) {
        cancel();
        try {
            mCallback.results(bundle);
        } catch (RemoteException e) {
            // empty
        }
    }

    private void beginningOfSpeech() {
        try {
            mCallback.beginningOfSpeech();
        } catch (RemoteException e) {
            // empty
        }
    }

    private void error(int errorCode) {
        cancel();
        try {
            mCallback.error(errorCode);
        } catch (RemoteException e) {
            // empty
        }
    }
}