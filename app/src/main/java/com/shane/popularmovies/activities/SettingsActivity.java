package com.shane.popularmovies.activities;


import android.os.Bundle;

import com.shane.popularmovies.AppCompatPreferenceActivity;
import com.shane.popularmovies.R;

public class SettingsActivity extends AppCompatPreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
    }

}
