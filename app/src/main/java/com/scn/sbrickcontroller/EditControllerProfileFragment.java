package com.scn.sbrickcontroller;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.scn.sbrickcontrollerprofilemanager.SBrickControllerProfile;


public class EditControllerProfileFragment extends Fragment {

    //
    // Private members
    //

    private static final String TAG = EditControllerProfileFragment.class.getSimpleName();

    private static final String ARG_CONTROLLER_PROFILE = "arg_controller_profile";

    SBrickControllerProfile profile;

    //
    // Constructors
    //

    public EditControllerProfileFragment() {
    }

    public static EditControllerProfileFragment newInstance(SBrickControllerProfile controllerProfile) {
        EditControllerProfileFragment fragment = new EditControllerProfileFragment();

        Bundle args = new Bundle();
        args.putParcelable(ARG_CONTROLLER_PROFILE, controllerProfile);
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
            profile = getArguments().getParcelable(ARG_CONTROLLER_PROFILE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit_controller_profile, container, false);
        return view;
    }
}
