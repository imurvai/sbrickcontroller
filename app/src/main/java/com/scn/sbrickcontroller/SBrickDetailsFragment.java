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
import android.view.LayoutInflater;
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
public class SBrickDetailsFragment extends Fragment {

    //
    // Private members
    //

    private static final String TAG = SBrickDetailsFragment.class.getSimpleName();

    public static final String ARG_SBRICK_ADDRESS = "arg_sbrick_address";
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
        etDisplayName.setText(sbrick.getDisplayName());

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

        progressDialog = Helper.showProgressDialog(SBrickDetailsFragment.this.getActivity(), "Connecting to SBrick...", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.i(TAG, "onClick...");

                // TODO: handle the cancel
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

            int value = Math.min(255, (Math.abs(progress - seekBarCenter) * 255) / seekBarCenter);
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
                    getCharacteristics();
                    break;

                case SBrick.ACTION_SBRICK_DISCONNECTED:
                    Log.i(TAG, "  ACTION_SBRICK_DISCONNECTED");

                    if (progressDialog != null) {
                        progressDialog.dismiss();
                        progressDialog = null;

                        MainActivity activity = (MainActivity)getActivity();
                        activity.goBackFromFragment();
                    }

                    break;

                case SBrick.ACTION_SBRICK_CHARACTERISTIC_READ:
                    Log.i(TAG, "  ACTION_SBRICK_CHARACTERISTIC_READ");

                    if (progressDialog != null) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }

                    SBrickCharacteristics characteristics = (SBrickCharacteristics)intent.getParcelableExtra(SBrick.EXTRA_CHARACTERISTICS);
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
