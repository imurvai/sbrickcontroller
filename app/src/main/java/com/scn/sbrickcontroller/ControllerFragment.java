package com.scn.sbrickcontroller;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.scn.sbrickcontrollerprofilemanager.SBrickControllerProfile;
import com.scn.sbrickmanager.SBrick;
import com.scn.sbrickmanager.SBrickManagerHolder;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public class ControllerFragment extends Fragment implements GameControllerActionListener {

    //
    // Private members
    //

    private static final String TAG = ControllerFragment.class.getSimpleName();

    private static final String ARG_CONTROLLER_PROFILE = "arg_controller_profile";

    private SBrickControllerProfile profile;
    private Map<String, SBrick> sbricksMap;
    private Map<String, int[]> channelValuesMap;

    private ProgressDialog progressDialog = null;

    private boolean allSBrickOk = true;

    //
    // Constructors
    //

    public ControllerFragment() {
    }

    public static ControllerFragment newInstance(SBrickControllerProfile controllerProfile) {
        ControllerFragment fragment = new ControllerFragment();

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

        Bundle arguments = getArguments();
        if (arguments == null)
            throw new RuntimeException("Controller profile name is needed for ControllerFragment.");

        // Find and store the controller profile
        profile = arguments.getParcelable(ARG_CONTROLLER_PROFILE);
        Log.i(TAG, "  Profile name: " + profile.getName());

        sbricksMap = new HashMap<>();
        channelValuesMap = new HashMap<>();

        // Find and store the SBricks addressed in the profile
        Collection<String> sbrickAddresses = profile.getSBrickAddresses();
        for (String address : sbrickAddresses) {
            SBrick sbrick = SBrickManagerHolder.getManager().getSBrick(address);
            sbricksMap.put(address, sbrick);

            int[] channelValues = new int[4];
            channelValues[0] = channelValues[1] = channelValues[2] = channelValues[3] = 0;
            channelValuesMap.put(address, channelValues);
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

        allSBrickOk = true;
        for (Map.Entry<String, SBrick> kvp: sbricksMap.entrySet()) {
            if (kvp.getValue() == null) {
                Log.w(TAG, "SBrick from the profile is not found - " + kvp.getKey());
                allSBrickOk = false;

                Helper.showMessageBox(
                        getActivity(),
                        "The SBrick stored in this profile is unknown. Please do a scan.",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.i(TAG, "onClick...");
                                MainActivity activity = (MainActivity)ControllerFragment.this.getActivity();
                                activity.goBackFromFragment();
                            }
                        });
            }
        }

        if (allSBrickOk) {
            Log.i(TAG, "All the SBricks in this profile are OK.");

            IntentFilter filter = new IntentFilter();
            filter.addAction(SBrick.ACTION_SBRICK_CONNECTED);
            filter.addAction(SBrick.ACTION_SBRICK_DISCONNECTED);
            filter.addAction(SBrick.ACTION_SBRICK_CHARACTERISTIC_READ);
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(sbrickBroadcastReceiver, filter);

            progressDialog = Helper.showProgressDialog(
                    ControllerFragment.this.getActivity(),
                    "Connecting to SBrick(s)...",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.i(TAG, "onClick...");
                            MainActivity activity = (MainActivity) getActivity();
                            activity.goBackFromFragment();
                        }
                    });

            if (!connectToSBricks()) {
                progressDialog.dismiss();
                progressDialog = null;

                Helper.showMessageBox(
                        ControllerFragment.this.getActivity(),
                        "Could not start connecting to SBricks.",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.i(TAG, "onClick...");
                                MainActivity activity = (MainActivity) getActivity();
                                activity.goBackFromFragment();
                            }
                        });
            }
        }

        super.onResume();
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause...");

        if (allSBrickOk) {
            Log.i(TAG, "  Unregister the SBrick local broadcast receiver...");
            LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(sbrickBroadcastReceiver);

            disconnectFromSBricks();

            Log.i(TAG, "  Dismiss the progress dialog if open...");
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
        }

        super.onPause();
    }

    //
    // GameControllerActionListener overrides
    //

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.i(TAG, "onKeyDown...");

//        if ((event.getSource() & InputDevice.SOURCE_GAMEPAD) != 0) {
//            Log.i(TAG, "  gamepad event.");
//
//            return true;
//        }
        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.i(TAG, "onKeyUp...");

//        if ((event.getSource() & InputDevice.SOURCE_GAMEPAD) != 0) {
//            Log.i(TAG, "  gamepad event.");
//            return true;
//        }
        return false;
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        Log.i(TAG, "onGenericMotionEvent...");

        if ((event.getSource() & InputDevice.SOURCE_JOYSTICK) != 0 && event.getAction() == MotionEvent.ACTION_MOVE) {
            Log.i(TAG, "  Joystick event.");

            Map<String, Integer[]> channelNewValuesMap = new HashMap<>();

            processMotionEvent(event, MotionEvent.AXIS_X, SBrickControllerProfile.CONTROLLER_ACTION_AXIS_X, channelNewValuesMap);
            processMotionEvent(event, MotionEvent.AXIS_Y, SBrickControllerProfile.CONTROLLER_ACTION_AXIS_Y, channelNewValuesMap);
            processMotionEvent(event, MotionEvent.AXIS_Z, SBrickControllerProfile.CONTROLLER_ACTION_AXIS_Z, channelNewValuesMap);
            processMotionEvent(event, MotionEvent.AXIS_RZ, SBrickControllerProfile.CONTROLLER_ACTION_AXIS_RZ, channelNewValuesMap);
            processMotionEvent(event, MotionEvent.AXIS_THROTTLE, SBrickControllerProfile.CONTROLLER_ACTION_R_TRIGGER, channelNewValuesMap);
            processMotionEvent(event, MotionEvent.AXIS_BRAKE, SBrickControllerProfile.CONTROLLER_ACTION_L_TRIGGER, channelNewValuesMap);

            for (String sbrickAddress: channelNewValuesMap.keySet()) {
                SBrick sbrick = sbricksMap.get(sbrickAddress);

                int[] values = channelValuesMap.get(sbrickAddress);
                Integer[] newValues = channelNewValuesMap.get(sbrickAddress);

                int v1 = (newValues[0] != null && Math.abs(values[0]) < Math.abs(newValues[0])) ? newValues[0] : values[0];
                int v2 = (newValues[1] != null && Math.abs(values[1]) < Math.abs(newValues[1])) ? newValues[1] : values[1];
                int v3 = (newValues[2] != null && Math.abs(values[2]) < Math.abs(newValues[2])) ? newValues[2] : values[2];
                int v4 = (newValues[3] != null && Math.abs(values[3]) < Math.abs(newValues[3])) ? newValues[3] : values[3];

                sbrick.sendCommand(v1, v2, v3, v4);
            }

            return true;
        }

        return false;
    }

    //
    // Private members
    //

    private boolean connectToSBricks() {
        Log.i(TAG, "connectToSBricks...");

        boolean isAllOk = true;
        for (SBrick sbrick : sbricksMap.values()) {
            if (!sbrick.connect()) {
                isAllOk = false;
                break;
            }
        }

        if (!isAllOk) {
            disconnectFromSBricks();
        }

        return isAllOk;
    }

    private void disconnectFromSBricks() {
        Log.i(TAG, "disconnectFromSBricks");

        for (SBrick sbrick : sbricksMap.values()) {
            if (sbrick != null)
                sbrick.disconnect();
        }
    }

    private boolean isAllSBrickConnected() {
        Log.i(TAG, "isAllSBrickConnected");

        for (SBrick sbrick : sbricksMap.values()) {
            if (!sbrick.isConnected()) {
                Log.i(TAG, "  Not all SBrick is connected.");
                return false;
            }
        }

        Log.i(TAG, "  All SBrick is connected.");
        return true;
    }

    private void processMotionEvent(MotionEvent event, int motionEventId, String controllerActionId, Map<String, Integer[]> channelNewValuesMap) {

        SBrickControllerProfile.ControllerAction controllerAction = profile.getControllerAction(controllerActionId);
        if (controllerAction == null) {
            return;
        }

        String sbrickAddress = controllerAction.getSbrickAddress();
        int channel = controllerAction.getChannel();
        int value = (int)(event.getAxisValue(motionEventId) * (controllerAction.getInvert() ? -255 : 255));

        // Joystick not always goes back to 0 exactly
        if (Math.abs(value) < 10)
            value = 0;

        if (!channelNewValuesMap.containsKey(sbrickAddress))
            channelNewValuesMap.put(sbrickAddress, new Integer[4]);

        Integer oldValue = (channelNewValuesMap.get(sbrickAddress))[channel];
        if (oldValue == null || Math.abs(oldValue.intValue()) < Math.abs(value))
            channelNewValuesMap.get(sbrickAddress)[channel] = new Integer(value);
    }

    private final BroadcastReceiver sbrickBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "sbrickBroadcastReceiver.onReceive...");

            switch (intent.getAction()) {
                case SBrick.ACTION_SBRICK_CONNECTED:
                    Log.i(TAG, "  ACTION_SBRICK_CONNECTED");

                    if (isAllSBrickConnected()) {
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                            progressDialog = null;
                        }
                    }

                    break;

                case SBrick.ACTION_SBRICK_DISCONNECTED:
                    Log.i(TAG, "  ACTION_SBRICK_DISCONNECTED");

                    if (progressDialog != null) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }

                    progressDialog = Helper.showProgressDialog(
                            ControllerFragment.this.getActivity(),
                            "Reconnecting to SBricks...",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });

                    break;
            }
        }
    };
}
