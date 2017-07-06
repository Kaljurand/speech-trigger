package ee.ioc.phon.android.speechtrigger;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

import ee.ioc.phon.android.speechutils.utils.PreferenceUtils;


public class PreferencesRecognitionService extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences_service);

            SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
            Resources res = getResources();
            setSummary(sp, res, R.string.keyPhraseEtEe);
            setSummary(sp, res, R.string.keyPhraseEnUs);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Preference pref = findPreference(key);
            if (pref instanceof EditTextPreference) {
                EditTextPreference etp = (EditTextPreference) pref;
                pref.setSummary(etp.getText());
            }
        }

        private void setSummary(SharedPreferences prefs, Resources res, int key) {
            Preference pref = findPreference(getString(key));
            if (pref != null) {
                pref.setSummary(PreferenceUtils.getPrefString(prefs, res, key));
            }
        }
    }
}
