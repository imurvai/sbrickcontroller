package com.scn.sbrickcontroller;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.scn.sbrickmanager.SBrick;
import com.scn.sbrickmanager.SBrickCharacteristicType;
import com.scn.sbrickmanager.SBrickManagerHolder;

import java.util.Date;

import javax.xml.datatype.Duration;

public class SBrickDetailsActivity extends BaseActivity {

    //
    // Private members
    //

    private static final String TAG = SBrickDetailsActivity.class.getSimpleName();
    private static final String SBRICK_ADDRESS_KEY = "SBRICK_ADDRESS_KEY";

    private SBrick sbrick;

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
    // Activity overrides
    //

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate...");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sbrick_details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String sbrickAddress;
        if (savedInstanceState != null) {
            Log.i(TAG, "  saved instance...");
            sbrickAddress = savedInstanceState.getString(SBRICK_ADDRESS_KEY);
        }
        else {
            Log.i(TAG, "  new instance...");
            sbrickAddress = getIntent().getStringExtra(Constants.EXTRA_SBRICK_ADDRESS);
        }

        sbrick = SBrickManagerHolder.getManager().getSBrick(sbrickAddress);

        twDeviceName = (TextView)findViewById(R.id.textview_device_name);
        twAddress = (TextView)findViewById(R.id.textview_address);
        twModelNumber = (TextView)findViewById(R.id.textview_model_number);
        twFirmwareRevision = (TextView)findViewById(R.id.textview_firmware_revision);
        twHardwareRevision = (TextView)findViewById(R.id.textview_hardware_revision);
        twSoftwareRevision = (TextView)findViewById(R.id.textview_software_revision);
        twManufacturerName = (TextView)findViewById(R.id.textview_manufacturer_name);
        sbPort1 = (SeekBar)findViewById(R.id.seekbar_port1);
        sbPort2 = (SeekBar)findViewById(R.id.seekbar_port2);
        sbPort3 = (SeekBar)findViewById(R.id.seekbar_port3);
        sbPort4 = (SeekBar)findViewById(R.id.seekbar_port4);

        twAddress.setText(sbrick.getAddress());
        twDeviceName.setText(sbrick.getName());

        sbPort1.setOnSeekBarChangeListener(seekBarChangeListener);
        sbPort2.setOnSeekBarChangeListener(seekBarChangeListener);
        sbPort3.setOnSeekBarChangeListener(seekBarChangeListener);
        sbPort4.setOnSeekBarChangeListener(seekBarChangeListener);

        sbPort1.setProgress(sbPort1.getMax() / 2);
        sbPort2.setProgress(sbPort2.getMax() / 2);
        sbPort3.setProgress(sbPort3.getMax() / 2);
        sbPort4.setProgress(sbPort4.getMax() / 2);
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume...");
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction(SBrick.ACTION_SBRICK_CONNECTED);
        filter.addAction(SBrick.ACTION_SBRICK_CONNECT_FAILED);
        filter.addAction(SBrick.ACTION_SBRICK_DISCONNECTED);
        filter.addAction(SBrick.ACTION_SBRICK_CHARACTERISTIC_READ);
        LocalBroadcastManager.getInstance(this).registerReceiver(sbrickBroadcastReceiver, filter);

        Log.i(TAG, "  Start the SBrick command processing...");
        SBrickManagerHolder.getManager().startCommandProcessing();

        if (sbrick.connect()) {
            progressDialog = Helper.showProgressDialog(
                    SBrickDetailsActivity.this,
                    "Connecting to SBrick...",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.i(TAG, "onClick...");
                            sbrick.disconnect();
                            finish();
                        }
                    });
        }
        else {
            Helper.showMessageBox(
                    SBrickDetailsActivity.this,
                    "Failed to start connecting to SBrick.",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
        }
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause...");
        super.onPause();

        Log.i(TAG, "  Unregister the SBrick local broadcast receiver...");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(sbrickBroadcastReceiver);

        Log.i(TAG, "  Disconnect from the SBrick...");
        sbrick.disconnect();

        Log.i(TAG, "  Stop the SBrick command processing...");
        SBrickManagerHolder.getManager().stopCommandProcessing();

        Log.i(TAG, "  Dismiss the progress dialog if open...");
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.i(TAG, "onSaveInstanceState...");
        super.onSaveInstanceState(outState);

        outState.putString(SBRICK_ADDRESS_KEY, sbrick.getAddress());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "onCreateOptionsMenu...");

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_sbrick_details, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "onOptionsItemSelected...");

        switch (item.getItemId()) {

            case android.R.id.home:
                Log.i(TAG, "  home");
                NavUtils.navigateUpFromSameTask(this);
                return true;

            case R.id.menu_item_done:
                Log.i(TAG, "  menu_item_done");
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

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
            int value4 = (int)(event.getAxisValue(MotionEvent.AXIS_RZ) * 255);

            sbrick.sendCommand(value1, value2, value3, value4);
            return true;
        }

        return false;
    }

    //
    // Private methods and classes
    //

    private final SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

        private long lastSendCommand;

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            // Prevent too fast command sending
            long duration = System.currentTimeMillis() - lastSendCommand;
            if (duration < 100) return;

            int value1 = getPortValue(sbPort1);
            int value2 = getPortValue(sbPort2);
            int value3 = getPortValue(sbPort3);
            int value4 = getPortValue(sbPort4);

            if (!sbrick.sendCommand(value1, value2, value3, value4)) {
                Log.i(TAG, "Failed to send command.");
            }

            lastSendCommand = System.currentTimeMillis();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            seekBar.setProgress(getSeekBarCenter(seekBar));
            sbrick.sendCommand(0, 0, 0, 0);
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

                    sbrick.readCharacteristic(SBrickCharacteristicType.FirmwareRevision);
                    sbrick.readCharacteristic(SBrickCharacteristicType.HardwareRevision);
                    sbrick.readCharacteristic(SBrickCharacteristicType.SoftwareRevision);
                    sbrick.readCharacteristic(SBrickCharacteristicType.ManufacturerName);
                    sbrick.readCharacteristic(SBrickCharacteristicType.ModelNumber);
                    sbrick.readCharacteristic(SBrickCharacteristicType.Appearance);

                    break;

                case SBrick.ACTION_SBRICK_CONNECT_FAILED:
                    Log.i(TAG, "  ACTION_SBRICK_CONNECT_FAILED");

                    if (progressDialog != null) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }

                    Helper.showMessageBox(SBrickDetailsActivity.this, "Failed to connect to SBrick.", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.i(TAG, "onClick...");
                            SBrickDetailsActivity.this.finish();
                        }
                    });

                    break;

                case SBrick.ACTION_SBRICK_DISCONNECTED:
                    Log.i(TAG, "  ACTION_SBRICK_DISCONNECTED");

                    if (progressDialog != null) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }

                    progressDialog = Helper.showProgressDialog(SBrickDetailsActivity.this, "Reconnecting to SBrick...", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.i(TAG, "onClick...");
                            SBrickDetailsActivity.this.finish();
                        }
                    });

                    break;

                case SBrick.ACTION_SBRICK_CHARACTERISTIC_READ:
                    Log.i(TAG, "  ACTION_SBRICK_CHARACTERISTIC_READ");

                    try {
                        SBrickCharacteristicType characteristicType = SBrickCharacteristicType.valueOf(intent.getStringExtra(SBrick.EXTRA_CHARACTERISTIC_TYPE));
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
