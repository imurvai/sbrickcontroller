package com.scn.sbrickcontroller;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.scn.sbrickcontrollerprofilemanager.SBrickControllerProfile;
import com.scn.sbrickcontrollerprofilemanager.SBrickControllerProfileManagerHolder;
import com.scn.sbrickmanager.SBrick;
import com.scn.sbrickmanager.SBrickCharacteristics;
import com.scn.sbrickmanager.SBrickManagerHolder;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public class ControllerFragment extends Fragment implements GameControllerActionListener {

    //
    // Private members
    //

    private static final String TAG = ControllerFragment.class.getSimpleName();

    private static final String ARG_CONTROLLER_PROFILE_NAME = "arg_controller_profile_name";

    private SBrickControllerProfile profile;
    private Map<String, SBrick> sbricksMap = new HashMap<>();

    private ProgressDialog progressDialog = null;

    //
    // Constructors
    //

    public ControllerFragment() {
    }

    public static ControllerFragment newInstance(String controllerProfileName) {
        ControllerFragment fragment = new ControllerFragment();

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

        Bundle arguments = getArguments();
        if (arguments == null)
            throw new RuntimeException("Controller profile name is needed for ControllerFragment.");

        // Find and store the controller profile
        String controllerProfileName = arguments.getString(ARG_CONTROLLER_PROFILE_NAME);
        profile = SBrickControllerProfileManagerHolder.getManager().getProfile(controllerProfileName);

        // Find and store the SBricks addressed in the profile
        Collection<String> sbrickAddresses = profile.getSBrickAddresses();
        for (String address : sbrickAddresses) {
            SBrick sbrick = SBrickManagerHolder.getSBrickManager().getSBrick(address);
            sbricksMap.put(address, sbrick);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_controller, container, false);

        return view;
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume...");

        super.onResume();
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause...");

        super.onPause();
    }

    //
    // GameControllerActionListener overrides
    //

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        return false;
    }

    //
    // Private members
    //

    private final BroadcastReceiver sbrickBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "sbrickBroadcastReceiver.onReceive...");

            switch (intent.getAction()) {
                case SBrick.ACTION_SBRICK_CONNECTED:
                    Log.i(TAG, "  ACTION_SBRICK_CONNECTED");

                    if (progressDialog != null) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }

                    break;

                case SBrick.ACTION_SBRICK_DISCONNECTED:
                    Log.i(TAG, "  ACTION_SBRICK_DISCONNECTED");

                    if (progressDialog != null) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }

                    break;
            }
        }
    };
}
