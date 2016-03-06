package com.scn.sbrickcontroller;

import android.preference.PreferenceFragment;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A placeholder fragment containing a simple view.
 */
public class AppPreferencesActivityFragment extends PreferenceFragment {

    //
    // Fragment overrides
    //

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.prefernces);
    }
}
