package com.scn.sbrickcontroller;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

public class ControllerConfigActivity extends BaseActivity {

    //
    // Private members
    //

    //
    // Activity overrides
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_controller_config);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


}
