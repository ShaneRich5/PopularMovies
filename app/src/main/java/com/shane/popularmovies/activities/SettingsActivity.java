package com.shane.popularmovies.activities;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.shane.popularmovies.fragments.SettingsFragment;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SettingsFragment fragment = SettingsFragment.newInstance();

        getFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, fragment, fragment.getTag())
                .commit();
    }
}
