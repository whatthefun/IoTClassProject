package com.example.user.iotclassproject.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;
import com.example.user.iotclassproject.R;

public class SettingsActivity extends PreferenceActivity {

    private static final String TAG = "SettingsActivity";
    SharedPreferences sharedPreferences;
    Preference username;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);

        sharedPreferences = getSharedPreferences("result", MODE_PRIVATE);
        Log.d(TAG, "onCreate: " + sharedPreferences.getString("username", null));
        username = (Preference)findPreference("username");

        username.setSummary(sharedPreferences.getString("username", null));

    }

    @Override protected void onResume() {
        super.onResume();
        username.setSummary(sharedPreferences.getString("username", null));
    }
}
