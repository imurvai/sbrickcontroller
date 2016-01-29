package com.scn.sbrickcontroller;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


/**
 * The main fragment.
 */
public class MainFragment extends Fragment {

    //
    // Private members
    //

    private static final String TAG = MainFragment.class.getSimpleName();

    //
    // Constructors
    //

    public MainFragment() {
    }

    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();

        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    //
    // Fragment overrides
    //

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate...");
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        Button buttonManageSBricks = (Button)view.findViewById(R.id.scan_sbricks_button);
        buttonManageSBricks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity activity = (MainActivity)getActivity();
                activity.startSBrickListFragment();
            }
        });

        Button buttonControllerProfiles = (Button)view.findViewById(R.id.controller_profiles_button);
        buttonControllerProfiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity activity = (MainActivity)getActivity();
                activity.startControllerProfileListFragment();
            }
        });

        return view;
    }
}
