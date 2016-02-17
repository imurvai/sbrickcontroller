package com.scn.sbrickcontroller;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.scn.sbrickcontrollerprofilemanager.SBrickControllerProfile;
import com.scn.sbrickmanager.SBrick;
import com.scn.sbrickmanager.SBrickManagerHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ControllerActivity extends ActionBarActivity {

    //
    // Private members
    //

    private static final String TAG = ControllerActivity.class.getSimpleName();
    private static final String PROFILES_KEY = "PROFILES_KEY";

    private ArrayList<SBrickControllerProfile> profiles;
    private Map<String, SBrick> sbricksMap;
    private Map<String, int[]> channelValuesMap;

    private SBrickControllerProfile selectedProfile;

    private ProgressDialog progressDialog = null;

    private boolean allSBrickOk = true;

    //
    // Activity overrides
    //

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate...");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState != null) {
            Log.i(TAG, "  saved instance...");
            profiles = savedInstanceState.getParcelableArrayList(PROFILES_KEY);
        }
        else {
            Log.i(TAG, "  new instance...");
            profiles = getIntent().getParcelableArrayListExtra(Constants.EXTRA_CONTROLLER_PROFILES);
        }

        sbricksMap = new HashMap<>();
        channelValuesMap = new HashMap<>();

        // Find and store the SBricks addressed in the profiles
        for (SBrickControllerProfile profile : profiles) {
            Collection<String> sbrickAddresses = profile.getSBrickAddresses();
            for (String address : sbrickAddresses) {
                SBrick sbrick = SBrickManagerHolder.getManager().getSBrick(address);
                sbricksMap.put(address, sbrick);

                int[] channelValues = new int[4];
                channelValues[0] = channelValues[1] = channelValues[2] = channelValues[3] = 0;
                channelValuesMap.put(address, channelValues);
            }
        }

        ListView lwProfiles = (ListView)findViewById(R.id.listview_controller_profiles_controller);
        lwProfiles.setAdapter(new ArrayAdapter<SBrickControllerProfile>(this, R.layout.listview_item_controller_profile_name, R.id.textview_controller_profile_name2, profiles));
        lwProfiles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "onItemClick...");
                selectedProfile = profiles.get(position);
            }
        });
        lwProfiles.setItemChecked(0, true);
        selectedProfile = profiles.get(0);
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume...");
        super.onResume();

        allSBrickOk = true;
        for (Map.Entry<String, SBrick> kvp: sbricksMap.entrySet()) {
            if (kvp.getValue() == null) {
                Log.w(TAG, "SBrick from the profile is not found - " + kvp.getKey());
                allSBrickOk = false;

                Helper.showMessageBox(
                        this,
                        "The SBrick stored in this profile is unknown. Please do a scan.",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.i(TAG, "onClick...");
                                ControllerActivity.this.finish();
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
            LocalBroadcastManager.getInstance(this).registerReceiver(sbrickBroadcastReceiver, filter);

            if (connectToSBricks()) {
                progressDialog = Helper.showProgressDialog(
                        this,
                        "Connecting to SBrick(s)...",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.i(TAG, "onClick...");
                                ControllerActivity.this.finish();
                            }
                        });
            }
            else {
                Helper.showMessageBox(
                    this,
                    "Could not start connecting to SBricks.",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.i(TAG, "onClick...");
                            ControllerActivity.this.finish();
                        }
                    });
            }
        }
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause...");
        super.onPause();

        if (allSBrickOk) {
            Log.i(TAG, "  Unregister the SBrick local broadcast receiver...");
            LocalBroadcastManager.getInstance(this).unregisterReceiver(sbrickBroadcastReceiver);

            disconnectFromSBricks();

            Log.i(TAG, "  Dismiss the progress dialog if open...");
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "onOptionsItemSelected...");

        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.i(TAG, "onSaveInstanceState...");
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList(PROFILES_KEY, profiles);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if ((event.getSource() & InputDevice.SOURCE_GAMEPAD) != 0 && event.getRepeatCount() == 0) {
            //Log.i(TAG, "onKeyDown...");

            SBrickControllerProfile.ControllerAction controllerAction = getControllerActionForKeyCode(keyCode);
            if (controllerAction != null) {

                String sbrickAddress = controllerAction.getSBrickAddress();
                SBrick sbrick = sbricksMap.get(sbrickAddress);
                int channel = controllerAction.getChannel();
                boolean invert = controllerAction.getInvert();

                int value = invert ? -255 : 255;
                if (sbrick.sendCommand(channel, value))
                    channelValuesMap.get(sbrickAddress)[channel] = value;
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        if ((event.getSource() & InputDevice.SOURCE_GAMEPAD) != 0 && event.getRepeatCount() == 0) {
            //Log.i(TAG, "onKeyUp...");

            SBrickControllerProfile.ControllerAction controllerAction = getControllerActionForKeyCode(keyCode);
            if (controllerAction != null) {

                String sbrickAddress = controllerAction.getSBrickAddress();
                SBrick sbrick = sbricksMap.get(sbrickAddress);
                int channel = controllerAction.getChannel();

                int value = 0;
                if (sbrick.sendCommand(channel, value))
                    channelValuesMap.get(sbrickAddress)[channel] = value;
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        //Log.i(TAG, "onGenericMotionEvent...");

        if ((event.getSource() & InputDevice.SOURCE_JOYSTICK) != 0 && event.getAction() == MotionEvent.ACTION_MOVE) {
            //Log.i(TAG, "  Joystick event.");

            Map<String, Integer[]> channelNewValuesMap = new HashMap<>();

            processMotionEvent(event, MotionEvent.AXIS_X, SBrickControllerProfile.CONTROLLER_ACTION_AXIS_X, channelNewValuesMap);
            processMotionEvent(event, MotionEvent.AXIS_Y, SBrickControllerProfile.CONTROLLER_ACTION_AXIS_Y, channelNewValuesMap);
            processMotionEvent(event, MotionEvent.AXIS_Z, SBrickControllerProfile.CONTROLLER_ACTION_AXIS_Z, channelNewValuesMap);
            processMotionEvent(event, MotionEvent.AXIS_RZ, SBrickControllerProfile.CONTROLLER_ACTION_AXIS_RZ, channelNewValuesMap);
            processMotionEvent(event, MotionEvent.AXIS_GAS, SBrickControllerProfile.CONTROLLER_ACTION_L_TRIGGER, channelNewValuesMap);
            processMotionEvent(event, MotionEvent.AXIS_BRAKE, SBrickControllerProfile.CONTROLLER_ACTION_R_TRIGGER, channelNewValuesMap);
            processMotionEvent(event, MotionEvent.AXIS_HAT_X, SBrickControllerProfile.CONTROLLER_ACTION_DPAD_LEFT_RIGHT, channelNewValuesMap);
            processMotionEvent(event, MotionEvent.AXIS_HAT_Y, SBrickControllerProfile.CONTROLLER_ACTION_DPAD_UP_DOWN, channelNewValuesMap);

            for (String sbrickAddress: channelNewValuesMap.keySet()) {
                SBrick sbrick = sbricksMap.get(sbrickAddress);

                int[] values = channelValuesMap.get(sbrickAddress);
                Integer[] newValues = channelNewValuesMap.get(sbrickAddress);

                int v0 = (newValues[0] != null && Math.abs(values[0]) < Math.abs(newValues[0])) ? newValues[0] : values[0];
                int v1 = (newValues[1] != null && Math.abs(values[1]) < Math.abs(newValues[1])) ? newValues[1] : values[1];
                int v2 = (newValues[2] != null && Math.abs(values[2]) < Math.abs(newValues[2])) ? newValues[2] : values[2];
                int v3 = (newValues[3] != null && Math.abs(values[3]) < Math.abs(newValues[3])) ? newValues[3] : values[3];

                sbrick.sendCommand(v0, v1, v2, v3);
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

        SBrickControllerProfile.ControllerAction controllerAction = selectedProfile.getControllerAction(controllerActionId);
        if (controllerAction == null) {
            return;
        }

        String sbrickAddress = controllerAction.getSBrickAddress();
        int channel = controllerAction.getChannel();
        int value = (int)(event.getAxisValue(motionEventId) * (controllerAction.getInvert() ? -255 : 255));

        // Joystick not always goes back to 0 exactly
        if (Math.abs(value) < 10)
            value = 0;

        if (!channelNewValuesMap.containsKey(sbrickAddress))
            channelNewValuesMap.put(sbrickAddress, new Integer[4]);

        // Update the channel value if the new one is greater than the current one.
        // It can happen if more than one controller action is assigned to the same channel.
        Integer oldValue = (channelNewValuesMap.get(sbrickAddress))[channel];
        if (oldValue == null || Math.abs(oldValue.intValue()) < Math.abs(value))
            channelNewValuesMap.get(sbrickAddress)[channel] = new Integer(value);
    }

    private SBrickControllerProfile.ControllerAction getControllerActionForKeyCode(int keycode) {
        //Log.i(TAG, "getControllerActionForKeyCode - " + keycode);

        switch (keycode) {
            case KeyEvent.KEYCODE_BUTTON_A:
                return selectedProfile.getControllerAction(SBrickControllerProfile.CONTROLLER_ACTION_A);
            case KeyEvent.KEYCODE_BUTTON_B:
                return selectedProfile.getControllerAction(SBrickControllerProfile.CONTROLLER_ACTION_B);
            case KeyEvent.KEYCODE_BUTTON_X:
                return selectedProfile.getControllerAction(SBrickControllerProfile.CONTROLLER_ACTION_X);
            case KeyEvent.KEYCODE_BUTTON_Y:
                return selectedProfile.getControllerAction(SBrickControllerProfile.CONTROLLER_ACTION_Y);
            case KeyEvent.KEYCODE_BUTTON_R1:
                return selectedProfile.getControllerAction(SBrickControllerProfile.CONTROLLER_ACTION_R1);
            case KeyEvent.KEYCODE_BUTTON_L1:
                return selectedProfile.getControllerAction(SBrickControllerProfile.CONTROLLER_ACTION_L1);
            case KeyEvent.KEYCODE_BUTTON_SELECT:
                return selectedProfile.getControllerAction(SBrickControllerProfile.CONTROLLER_ACTION_SELECT);
            case KeyEvent.KEYCODE_BUTTON_START:
                return selectedProfile.getControllerAction(SBrickControllerProfile.CONTROLLER_ACTION_START);
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
                            ControllerActivity.this,
                            "Reconnecting to SBricks...",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Log.i(TAG, "onClick...");
                                    ControllerActivity.this.finish();
                                }
                            });

                    break;
            }
        }
    };
}
