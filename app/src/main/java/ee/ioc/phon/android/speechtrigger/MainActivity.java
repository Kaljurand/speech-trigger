package ee.ioc.phon.android.speechtrigger;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.Toast;

import ee.ioc.phon.android.speechutils.Extras;

public class MainActivity extends Activity {

    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.buttonSettingsService).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, PreferencesRecognitionService.class));
            }
        });

        findViewById(R.id.buttonSettingsApp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.fromParts("package", getPackageName(), null));
                startActivity(intent);
            }
        });

        // TODO: improve this, e.g. allow the user to edit the URL; pull the URL into a string
        // and import it into K6nele directly, as hands-free as possible.
        findViewById(R.id.buttonLoadRewrites).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.parse("https://docs.google.com/spreadsheets/d/1jYhX5ARj_I5c78K9ECUDmE9gr96xes732vFlJsuGLtk/export?format=tsv");
                intent.setData(uri);
                startActivity(intent);
            }
        });

        findViewById(R.id.buttonLaunchK6neleEn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchK6nele(getIntentEn());
            }
        });

        findViewById(R.id.buttonLaunchK6neleEt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchK6nele(getIntentEt());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                onSuccess(intent);
            } else if (resultCode == RESULT_CANCELED) {
                onCancel();
            } else {
                onError(resultCode);
            }
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }


    private void onSuccess(Intent intent) {
        finish();
    }

    private void onCancel() {
        toast(getString(R.string.errorResultCanceled));
        finish();
    }

    private void onError(int errorCode) {
        toast(getErrorMessage(errorCode));
        finish();
    }

    private void toast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    /**
     * TODO: if activity was not found then open a link to download K6nele APK
     * TODO: if we call ActivityForResult (which we want to do to be able to see the errors)
     * then web search is done (possibly K6nele bug)
     */
    private void launchK6nele(Intent intent) {
        if (false) {
            startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
        } else {
            startActivity(intent);
        }
    }

    private String getErrorMessage(int errorCode) {
        switch (errorCode) {
            case RecognizerIntent.RESULT_AUDIO_ERROR:
                return getString(R.string.errorResultAudioError);
            case RecognizerIntent.RESULT_CLIENT_ERROR:
                return getString(R.string.errorResultClientError);
            case RecognizerIntent.RESULT_NETWORK_ERROR:
                return getString(R.string.errorResultNetworkError);
            case RecognizerIntent.RESULT_SERVER_ERROR:
                return getString(R.string.errorResultServerError);
            case RecognizerIntent.RESULT_NO_MATCH:
                return getString(R.string.errorResultNoMatch);
            default:
                return "Unknown error";
        }
    }

    private static Intent getIntentEn() {
        return createRecognizerIntent(
                "en-US", "hey wake up", "Say: hey wake up",
                "en-US", "Say for example: switch on the living room lights",
                "com.google.android.googlequicksearchbox/com.google.android.voicesearch.serviceapi.GoogleRecognitionService");
    }

    private static Intent getIntentEt() {
        return createRecognizerIntent(
                "et-EE", "ärka üles", "Öelge: ärka üles",
                "et-EE", "öelge näiteks: pane elutoa lamp põlema",
                "ee.ioc.phon.android.speak/.service.WebSocketRecognitionService");
    }

    /**
     * TODO: Make this configurable in the speech-trigger settings
     */
    private static Intent createRecognizerIntent(String locale1, String phrase1, String prompt1,
                                                 String locale2, String prompt2, String service2) {
        Intent intent = new Intent(RecognizerIntent.ACTION_WEB_SEARCH);
        intent.setComponent(ComponentName.unflattenFromString("ee.ioc.phon.android.speak/.activity.SpeechActionActivity"));
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        intent.putExtra(Extras.EXTRA_VOICE_PROMPT, prompt1);
        // TODO: query the name of THIS service
        intent.putExtra(Extras.EXTRA_SERVICE_COMPONENT, "ee.ioc.phon.android.speechtrigger/.TriggerService");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, prompt1);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale1);
        intent.putExtra(Extras.EXTRA_AUTO_START, true);
        intent.putExtra(Extras.EXTRA_AUDIO_CUES, true);
        intent.putExtra(Extras.EXTRA_RETURN_ERRORS, true);
        intent.putExtra(Extras.EXTRA_FINISH_AFTER_LAUNCH_INTENT, false);
        intent.putExtra(Extras.EXTRA_PHRASE, phrase1);
        intent.putExtra(Extras.EXTRA_RESULT_UTTERANCE, ".+");
        intent.putExtra(Extras.EXTRA_RESULT_COMMAND, "activity");
        // TODO: create this from JSON
        intent.putExtra(Extras.EXTRA_RESULT_ARG1,
                "{\"component\": \"ee.ioc.phon.android.speak/.activity.SpeechActionActivity\"," +
                        "\"extras\":{" +
                        "\"ee.ioc.phon.android.extra.SERVICE_COMPONENT\":\"" + service2 + "\"," +
                        "\"android.speech.extra.PROMPT\":\"Lights\"," +
                        "\"ee.ioc.phon.android.extra.VOICE_PROMPT\":\"" + prompt2 + "\"," +
                        "\"android.speech.extra.MAX_RESULTS\":1," +
                        "\"android.speech.extra.LANGUAGE\":\"" + locale2 + "\"," +
                        "\"ee.ioc.phon.android.extra.AUTO_START\": True," +
                        "\"ee.ioc.phon.android.extra.AUDIO_CUES\": True," +
                        "\"ee.ioc.phon.android.extra.FINISH_AFTER_LAUNCH_INTENT\": True," +
                        "\"ee.ioc.phon.android.extra.RETURN_ERRORS\": True," +
                        "\"ee.ioc.phon.android.extra.RESULT_REWRITES\": [\"Lights\", \"Lights.Hue\"]}}");
        return intent;
    }
}