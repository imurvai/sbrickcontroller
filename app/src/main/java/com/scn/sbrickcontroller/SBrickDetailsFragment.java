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
import com.scn.sbrickmanager.SBrickCharacteristics;
import com.scn.sbrickmanager.SBrickManagerHolder;


/**
 * SBrick details fragment.
 */
public class SBrickDetailsFragment extends Fragment implements GameControllerActionListener {

    //
    // Public constants
    //

    public static final String ARG_SBRICK_ADDRESS = "arg_sbrick_address";

    //
    // Private members
    //

    private static final String TAG = SBrickDetailsFragment.class.getSimpleName();

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

    private boolean isCharacteristicsRead = false;

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

        if (sbrick == null)
            throw new RuntimeException("Can't find SBrick - " + sbrickAddress);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");

        View view = inflater.inflate(R.layout.fragment_sbrick_details, container, false);

        etDisplayName = (EditText)view.findViewById(R.id.editTextDisplayName);
        twDeviceName = (TextView)view.findViewById(R.id.textViewDeviceName);
        twAddress = (TextView)view.findViewById(R.id.textViewAddress);
        twModelNumber = (TextView)view.findViewById(R.id.textViewModelNumber);
        twFirmwareRevision = (TextView)view.findViewById(R.id.textViewFirmwareRevision);
        twHardwareRevision = (TextView)view.findViewById(R.id.textViewHardwareRevision);
        twSoftwareRevision = (TextView)view.findViewById(R.id.textViewSoftwareRevision);
        twManufacturerName = (TextView)view.findViewById(R.id.textViewManufacturerName);
        sbPort1 = (SeekBar)view.findViewById(R.id.seekBarPort1);
        sbPort2 = (SeekBar)view.findViewById(R.id.seekBarPort2);
        sbPort3 = (SeekBar)view.findViewById(R.id.seekBarPort3);
        sbPort4 = (SeekBar)view.findViewById(R.id.seekBarPort4);

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

        progressDialog = Helper.showProgressDialog(
                SBrickDetailsFragment.this.getActivity(),
                "Connecting to SBrick...",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i(TAG, "onClick...");
                        sbrick.disconnect();
                    }
                },
                new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        Log.i(TAG, "onDissmiss...");

                        if (!isCharacteristicsRead) {
                            MainActivity activity = (MainActivity)getActivity();
                            activity.goBackFromFragment();
                        }
                    }
                });

        if (!sbrick.connect()) {
            getCharacteristics();
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

        if (!isCharacteristicsRead)
            return false;

        if ((event.getSource() & InputDevice.SOURCE_JOYSTICK) != 0 && event.getAction() == MotionEvent.ACTION_MOVE) {
            int value1 = (int)(event.getAxisValue(MotionEvent.AXIS_X) * 255);
            int value2 = (int)(event.getAxisValue(MotionEvent.AXIS_Y) * 255);
            int value3 = (int)(event.getAxisValue(MotionEvent.AXIS_Z) * 255);
            int value4 = (int)(event.getAxisValue(MotionEvent.AXIS_RZ) * 255);

            sbrick.sendCommand(0, Math.abs(value1), value1 < 0);
            sbrick.sendCommand(1, Math.abs(value2), value2 < 0);
            sbrick.sendCommand(2, Math.abs(value3), value3 < 0);
            sbrick.sendCommand(3, Math.abs(value4), value4 < 0);

            return true;
        }

        return false;
    }

    //
    // Private methods and classes
    //

    private void getCharacteristics() {
        Log.i(TAG, "getCharacteristics");

        if (progressDialog != null) {
            progressDialog.setMessage("Reading SBrick details...");
        }

        if (!sbrick.getCharacteristicsAsync()) {
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
        }
    }

    private final SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            int channel = -1;

            if (seekBar == sbPort1)
                channel = 0;
            else if (seekBar == sbPort2)
                channel = 1;
            else if (seekBar == sbPort3)
                channel = 2;
            else if (seekBar == sbPort4)
                channel = 3;

            if (channel == -1)
                return;

            int seekBarCenter = getSeekBarCenter(seekBar);

            int value = Math.min(255, (Math.abs(progress - seekBarCenter) * 280) / seekBarCenter);
            boolean invert = progress < seekBarCenter;

            sbrick.sendCommand(channel, value, invert);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            seekBar.setProgress(getSeekBarCenter(seekBar));
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

                    if (!isCharacteristicsRead)
                        getCharacteristics();

                    break;

                case SBrick.ACTION_SBRICK_DISCONNECTED:
                    Log.i(TAG, "  ACTION_SBRICK_DISCONNECTED");

                    if (progressDialog != null) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }

                    break;

                case SBrick.ACTION_SBRICK_CHARACTERISTIC_READ:
                    Log.i(TAG, "  ACTION_SBRICK_CHARACTERISTIC_READ");

                    isCharacteristicsRead = true;

                    if (progressDialog != null) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }

                    SBrickCharacteristics characteristics = intent.getParcelableExtra(SBrick.EXTRA_CHARACTERISTICS);
                    twDeviceName.setText(characteristics.getDeviceName());
                    twModelNumber.setText(characteristics.getModelNumber());
                    twFirmwareRevision.setText(characteristics.getFirmwareRevision());
                    twHardwareRevision.setText(characteristics.getHardwareRevision());
                    twSoftwareRevision.setText(characteristics.getSoftwareRevision());
                    twManufacturerName.setText(characteristics.getManufacturerName());

                    break;
            }
        }
    };
}
