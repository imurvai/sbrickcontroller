package com.scn.sbrickcontroller;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Controller profiles fragment.
 */
public class ControllerProfilesFragment extends Fragment {

    //
    // Private members
    //

    private static final String TAG = ControllerProfilesFragment.class.getSimpleName();

    //
    // Constructors
    //

    public ControllerProfilesFragment() {
        // Required empty public constructor
    }

    public static ControllerProfilesFragment newInstance() {
        ControllerProfilesFragment fragment = new ControllerProfilesFragment();

        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    //
    // Fragment overrides
    //

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_controller_profiles, container, false);
        return view;
    }
}
