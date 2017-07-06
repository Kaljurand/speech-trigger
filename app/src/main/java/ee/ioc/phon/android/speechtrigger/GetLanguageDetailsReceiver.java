package ee.ioc.phon.android.speechtrigger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;

import java.util.ArrayList;

public class GetLanguageDetailsReceiver extends BroadcastReceiver {
    public static final String DEFAULT_LANGUAGE_PREFERENCE = "en-US";

    public static final ArrayList<String> SUPPORTED_LANGUAGES;

    static {
        ArrayList<String> aList = new ArrayList<>();
        aList.add(DEFAULT_LANGUAGE_PREFERENCE);
        aList.add("et-EE");
        SUPPORTED_LANGUAGES = aList;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = new Bundle();
        extras.putString(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, DEFAULT_LANGUAGE_PREFERENCE);
        extras.putStringArrayList(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES, SUPPORTED_LANGUAGES);
        setResultExtras(extras);
    }
}