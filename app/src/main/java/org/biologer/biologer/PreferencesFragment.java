package org.biologer.biologer;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

public class PreferencesFragment extends PreferenceFragmentCompat {

    private static final String TAG = "Biologer.Preferences";

    Boolean licence_has_changed = false;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // This is a workaround in order to change background color of the fragment
        getListView().setBackgroundColor(Color.WHITE);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the preferences from an XML resource
        setPreferencesFromResource(R.xml.fragment_preferences, rootKey);
        Log.d(TAG, "Loading preferences fragment");

        ListPreference dataLicense = (ListPreference) findPreference("data_license");
        ListPreference imageLicense = (ListPreference) findPreference("image_license");

        if (dataLicense != null || imageLicense != null) {
            getLicences(dataLicense);
            getLicences(imageLicense);
        }

        // Add button fot taxa sync process
        Preference button = findPreference("taxa_button");
        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final Intent fetchTaxa = new Intent(getActivity(), FetchTaxa.class);
                fetchTaxa.setAction(FetchTaxa.ACTION_START_FOREGROUND_SERVICE);
                getActivity().startService(fetchTaxa);
                return true;
            }
        });

        if (dataLicense != null) {
            dataLicense.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Log.d(TAG, "Data license changed to: " + newValue);
                    licence_has_changed = true;
                    return true;
                }
            });
        }

        if (imageLicense != null) {
            imageLicense.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Log.d(TAG, "Data license changed to: " + newValue);
                    licence_has_changed = true;
                    return true;
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Activity activity = getActivity();
        if (activity != null) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
            Log.d(TAG, "Data license set to: " + preferences.getString("data_license", "0"));
            Log.d(TAG, "Image license set to: " + preferences.getString("image_license", "0"));
            Log.d(TAG, "Project name is set to: " + preferences.getString("project_name", "0"));
            if (licence_has_changed) {
                updateLicense();
            }
        }
    }

    private void getLicences(ListPreference listpreference) {
        if(SettingsManager.getDatabaseName().equals("https://biologer.hr")) {
            CharSequence[] entries = {getString(R.string.license_default), getString(R.string.license_public), getString(R.string.license_timed)};
            CharSequence[] entryValues = {"0", "11", "35"};
            listpreference.setEntries(entries);
            listpreference.setDefaultValue("0");
            listpreference.setEntryValues(entryValues);
        } else {
            CharSequence[] entries = {getString(R.string.license_default), getString(R.string.license10), getString(R.string.license20), getString(R.string.license30), getString(R.string.license40)};
            CharSequence[] entryValues = {"0", "10", "20", "30", "40"};
            listpreference.setEntries(entries);
            listpreference.setDefaultValue("0");
            listpreference.setEntryValues(entryValues);
        }
    }

    private void updateLicense() {
        Intent update_licences = new Intent(getActivity(), UpdateLicenses.class);
        getActivity().startService(update_licences);
    }
}
