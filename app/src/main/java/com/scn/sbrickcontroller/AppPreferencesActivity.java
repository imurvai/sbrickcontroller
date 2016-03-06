package com.scn.sbrickcontroller;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

public class AppPreferencesActivity extends BaseActivity {

    //
    // Activity overrides
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_preferences);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
