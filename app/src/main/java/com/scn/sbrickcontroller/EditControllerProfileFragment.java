package com.scn.sbrickcontroller;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.scn.sbrickcontrollerprofilemanager.SBrickControllerProfile;
import com.scn.sbrickcontrollerprofilemanager.SBrickControllerProfileManagerHolder;


public class EditControllerProfileFragment extends Fragment {

    //
    // Public constants
    //

    public static final String ARG_CONTROLLER_PROFILE_NAME = "arg_controller_profile_name";

    //
    // Private members
    //

    private static final String TAG = EditControllerProfileFragment.class.getSimpleName();

    SBrickControllerProfile profile;

    //
    // Constructors
    //

    public EditControllerProfileFragment() {
        // Required empty public constructor
    }

    public static EditControllerProfileFragment newInstance(String controllerProfileName) {
        EditControllerProfileFragment fragment = new EditControllerProfileFragment();

        Bundle args = new Bundle();
        args.putString(ARG_CONTROLLER_PROFILE_NAME, controllerProfileName);
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
            String profileName = getArguments().getString(ARG_CONTROLLER_PROFILE_NAME);
            if (profileName == null) {
                profile = SBrickControllerProfileManagerHolder.getManager().addNewProfile();
            }
            else {
                profile = SBrickControllerProfileManagerHolder.getManager().getProfile(profileName);
            }
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
