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
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.scn.sbrickmanager.SBrick;
import com.scn.sbrickmanager.SBrickManagerHolder;


/**
 * SBrick details fragment.
 */
public class SBrickDetailsFragment extends Fragment implements GameControllerActionListener {

    //
    // Private members
    //

    private static final String TAG = SBrickDetailsFragment.class.getSimpleName();

    private static final String ARG_SBRICK_ADDRESS = "arg_sbrick_address";

    private SBrick sbrick;

    private EditText etDisplayName;
    private TextView twDeviceName;
    private TextView twAddress;
    private TextView twModelNumber;
    private TextView twFirmwareRevision;
    private TextView twHardwareRevision;
    private TextView twSoftwareRevision;
    private TextView twManufacturerName;
    private SeekBar sbPort1;
    private SeekBar sbPort2;
    private SeekBar sbPort3;
    private SeekBar sbPort4;

    private ProgressDialog progressDialog;

    //
    // Constructors
    //

    public SBrickDetailsFragment() {
    }

    public static SBrickDetailsFragment newInstance(String sbrickAddress) {
        SBrickDetailsFragment fragment = new SBrickDetailsFragment();

        Bundle args = new Bundle();
        args.putString(ARG_SBRICK_ADDRESS, sbrickAddress);
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

        Bundle arguments = getArguments();
        if (arguments == null)
            throw new RuntimeException("SBrick address is needed for SBrickDetailsFragment.");

        String sbrickAddress = getArguments().getString(ARG_SBRICK_ADDRESS);
        sbrick = SBrickManagerHolder.getSBrickManager().getSBrick(sbrickAddress);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");

        View view = inflater.inflate(R.layout.fragment_sbrick_details, container, false);

        etDisplayName = (EditText)view.findViewById(R.id.edittext_display_name);
        twDeviceName = (TextView)view.findViewById(R.id.textview_device_name);
        twAddress = (TextView)view.findViewById(R.id.textview_address);
        twModelNumber = (TextView)view.findViewById(R.id.textview_model_number);
        twFirmwareRevision = (TextView)view.findViewById(R.id.textview_firmware_revision);
        twHardwareRevision = (TextView)view.findViewById(R.id.textview_hardware_revision);
        twSoftwareRevision = (TextView)view.findViewById(R.id.textview_software_revision);
        twManufacturerName = (TextView)view.findViewById(R.id.textview_manufacturer_name);
        sbPort1 = (SeekBar)view.findViewById(R.id.seekbar_port1);
        sbPort2 = (SeekBar)view.findViewById(R.id.seekbar_port2);
        sbPort3 = (SeekBar)view.findViewById(R.id.seekbar_port3);
        sbPort4 = (SeekBar)view.findViewById(R.id.seekbar_port4);

        twAddress.setText(sbrick.getAddress());
        etDisplayName.setText(sbrick.getName());

        sbPort1.setOnSeekBarChangeListener(seekBarChangeListener);
        sbPort2.setOnSeekBarChangeListener(seekBarChangeListener);
        sbPort3.setOnSeekBarChangeListener(seekBarChangeListener);
        sbPort4.setOnSeekBarChangeListener(seekBarChangeListener);

        sbPort1.setProgress(sbPort1.getMax() / 2);
        sbPort2.setProgress(sbPort2.getMax() / 2);
        sbPort3.setProgress(sbPort3.getMax() / 2);
        sbPort4.setProgress(sbPort4.getMax() / 2);

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

        if (sbrick.connect()) {
            progressDialog = Helper.showProgressDialog(
                    SBrickDetailsFragment.this.getActivity(),
                    "Connecting to SBrick...",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.i(TAG, "onClick...");
                            sbrick.disconnect();
                            MainActivity activity = (MainActivity)SBrickDetailsFragment.this.getActivity();
                            activity.goBackFromFragment();
                        }
                    });
        }
        else {
            Helper.showMessageBox(
                    SBrickDetailsFragment.this.getActivity(),
                    "Failed to start connecting to SBrick.",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            MainActivity activity = (MainActivity)SBrickDetailsFragment.this.getActivity();
                            activity.goBackFromFragment();
                        }
                    });
        }

        super.onResume();
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause...");

        sbrick.setName(etDisplayName.getText().toString());

        Log.i(TAG, "  Unregister the SBrick local broadcast receiver...");
        LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(sbrickBroadcastReceiver);

        Log.i(TAG, "  Disconnect from the SBrick...");
        sbrick.disconnect();

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
        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {

        if (!sbrick.isConnected())
            return false;

        if ((event.getSource() & InputDevice.SOURCE_JOYSTICK) != 0 && event.getAction() == MotionEvent.ACTION_MOVE) {
            int value1 = (int)(event.getAxisValue(MotionEvent.AXIS_Y) * 255);
            int value2 = (int)(event.getAxisValue(MotionEvent.AXIS_X) * 255);
            int value3 = (int)(event.getAxisValue(MotionEvent.AXIS_Z) * 255);
            int value4 = (int)(event.getAxisValue(MotionEvent.AXIS_RZ) * -255);

            sbrick.sendCommand(value1, value2, value3, value4);
            return true;
        }

        return false;
    }

    //
    // Private methods and classes
    //

    private final SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            int value1 = getPortValue(sbPort1);
            int value2 = getPortValue(sbPort2);
            int value3 = getPortValue(sbPort3);
            int value4 = getPortValue(sbPort4);
            if (!sbrick.sendCommand(value1, value2, value3, value4)) {
                Log.i(TAG, "Failed to send command.");
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            seekBar.setProgress(getSeekBarCenter(seekBar));
        }

        private int getPortValue(SeekBar seekBar) {
            int progress = seekBar.getProgress();
            int seekBarCenter = seekBar.getMax() / 2;
            int value = ((progress - seekBarCenter) * 280) / seekBarCenter;
            return Math.min(255, Math.max(-255, value));
        }

        private int getSeekBarCenter(SeekBar seekBar) {
            return seekBar.getMax() / 2;
        }
    };

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

                    sbrick.readCharacteristic(SBrick.SBrickCharacteristicType.DeviceName);
                    sbrick.readCharacteristic(SBrick.SBrickCharacteristicType.FirmwareRevision);
                    sbrick.readCharacteristic(SBrick.SBrickCharacteristicType.HardwareRevision);
                    sbrick.readCharacteristic(SBrick.SBrickCharacteristicType.SoftwareRevision);
                    sbrick.readCharacteristic(SBrick.SBrickCharacteristicType.ManufacturerName);
                    sbrick.readCharacteristic(SBrick.SBrickCharacteristicType.ModelNumber);
                    sbrick.readCharacteristic(SBrick.SBrickCharacteristicType.Appearance);

                    break;

                case SBrick.ACTION_SBRICK_DISCONNECTED:
                    Log.i(TAG, "  ACTION_SBRICK_DISCONNECTED");

                    if (progressDialog != null) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }

                    MainActivity activity = (MainActivity)getActivity();
                    activity.goBackFromFragment();

                    break;

                case SBrick.ACTION_SBRICK_CHARACTERISTIC_READ:
                    Log.i(TAG, "  ACTION_SBRICK_CHARACTERISTIC_READ");

                    try {
                        SBrick.SBrickCharacteristicType characteristicType = SBrick.SBrickCharacteristicType.valueOf(intent.getStringExtra(SBrick.EXTRA_CHARACTERISTIC_TYPE));
                        String value = intent.getStringExtra(SBrick.EXTRA_CHARACTERISTIC_VALUE);

                        switch (characteristicType) {
                            case DeviceName:
                                twDeviceName.setText(value);
                                break;
                            case ModelNumber:
                                twModelNumber.setText(value);
                                break;
                            case FirmwareRevision:
                                twFirmwareRevision.setText(value);
                                break;
                            case HardwareRevision:
                                twHardwareRevision.setText(value);
                                break;
                            case SoftwareRevision:
                                twSoftwareRevision.setText(value);
                                break;
                            case ManufacturerName:
                                twManufacturerName.setText(value);
                                break;
                        }
                    }
                    catch (Exception ex) {}

                    break;
            }
        }
    };
}
