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

    private static final String ARG_CONTROLLER_PROFILE = "arg_controller_profile";

    private SBrickControllerProfile profile;
    private Map<String, SBrick> sbricksMap;
    private Map<String, int[]> channelValuesMap;

    private ProgressDialog progressDialog = null;

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
            SBrick sbrick = SBrickManagerHolder.getSBrickManager().getSBrick(address);
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
                        MainActivity activity = (MainActivity)getActivity();
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
                            MainActivity activity = (MainActivity)getActivity();
                            activity.goBackFromFragment();
                        }
                    });
        }

        super.onResume();
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause...");

        Log.i(TAG, "  Unregister the SBrick local broadcast receiver...");
        LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(sbrickBroadcastReceiver);

        disconnectFromSBricks();

        Log.i(TAG, "  Dismiss the progress dialog if open...");
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }

        super.onPause();
    }

    //
    // GameControllerActionListener overrides
    //

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if ((event.getSource() & InputDevice.SOURCE_GAMEPAD) != 0) {

        }
        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {

        if ((event.getSource() & InputDevice.SOURCE_JOYSTICK) != 0 && event.getAction() == MotionEvent.ACTION_MOVE) {

            int motionEventId = event.getAction();
            String controllerActionId = getSBrickControllerActionIdForMotionEventId(motionEventId);
            if (controllerActionId == null)
                return true;

            SBrickControllerProfile.ControllerAction controllerAction = profile.getControllerAction(controllerActionId);
            if (controllerAction == null)
                return true;

            String address = controllerAction.getSbrickAddress();
            int channel = controllerAction.getChannel();
            int value = (int)(event.getAxisValue(motionEventId) * (controllerAction.getInvert() ? -255 : 255));

            if (channelValuesMap.get(address)[channel] == value)
                return true;

            channelValuesMap.get(address)[channel] = value;

            SBrick sbrick = sbricksMap.get(address);
            int[] values = channelValuesMap.get(address);
            sbrick.sendCommand(values[0], values[1], values[2], values[3]);

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

    private String setChannelValue(MotionEvent event, int motionEventId) {
        String controllerActionId = getSBrickControllerActionIdForMotionEventId(motionEventId);
        if (controllerActionId == null)
            return null;

        SBrickControllerProfile.ControllerAction controllerAction = profile.getControllerAction(controllerActionId);
        if (controllerAction != null) {
            String address = controllerAction.getSbrickAddress();
            int channel = controllerAction.getChannel();
            int value = (int)(event.getAxisValue(motionEventId) * (controllerAction.getInvert() ? -255 : 255));

            if (channelValuesMap.get(address)[channel] != value) {
                channelValuesMap.get(address)[channel] = value;
                return address;
            }
        }
        return null;
    }

    private String getSBrickControllerActionIdForMotionEventId(int motionEventId) {
        switch (motionEventId) {
            case MotionEvent.AXIS_X: return SBrickControllerProfile.CONTROLLER_ACTION_AXIS_X;
            case MotionEvent.AXIS_Y: return SBrickControllerProfile.CONTROLLER_ACTION_AXIS_Y;
            case MotionEvent.AXIS_Z: return SBrickControllerProfile.CONTROLLER_ACTION_AXIS_Z;
            case MotionEvent.AXIS_RZ: return SBrickControllerProfile.CONTROLLER_ACTION_AXIS_RZ;
            case MotionEvent.AXIS_THROTTLE: return SBrickControllerProfile.CONTROLLER_ACTION_L_TRIGGER;
            case MotionEvent.AXIS_BRAKE: return SBrickControllerProfile.CONTROLLER_ACTION_R_TRIGGER;
        }
        return null;
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
