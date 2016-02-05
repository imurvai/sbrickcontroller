package com.scn.sbrickcontroller;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import com.scn.sbrickcontrollerprofilemanager.SBrickControllerProfileManagerHolder;
import com.scn.sbrickmanager.SBrickManagerHolder;

/**
 * The base activity.
 */
public class BaseActivity extends ActionBarActivity {

    //
    // Private members
    //

    private final String TAG = "BaseActivity";

    private final int REQUEST_ENABLE_BLUETOOTH = 0x1234;

    //
    // Activity overrides
    //


    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        Log.i(TAG, "onCreate...");
        super.onCreate(savedInstanceState, persistentState);

        if (!SBrickManagerHolder.getManager().isBLESupported()) {
            Helper.showMessageBox(this, "Your device doesn't support bluetooth low energy profile.", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    BaseActivity.this.finish();
                }
            });

            return;
        }
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume...");
        super.onResume();

        if (!SBrickManagerHolder.getManager().isBLESupported())
            return;

        Log.i(TAG, "  Register the BluetoothAdapter broadcast receiver...");
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bluetoothAdapterBroadcastReceiver, filter);

        if (!SBrickManagerHolder.getManager().isBluetoothOn()) {
            Log.i(TAG, "  Bluetooth is off, ask to turn it on...");
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_ENABLE_BLUETOOTH);
        }
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause...");
        super.onPause();

        if (!SBrickManagerHolder.getManager().isBLESupported())
            return;

        Log.i(TAG, "  Unregister the BluetoothAdapter broadcast receiver...");
        unregisterReceiver(bluetoothAdapterBroadcastReceiver);
    }

    //
    // Private methods and classes
    //

    private BroadcastReceiver bluetoothAdapterBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "bluetoothAdapterBroadcastReceiver.onReceive");

            final String action = intent.getAction();

            if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                Log.i(TAG, "  BluetoothAdapter.ACTION_STATE_CHANGED");

                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                logBluetoothAdapterState(state);

                if (state == BluetoothAdapter.STATE_OFF) {
                    Log.i(TAG, "  Ask to turn the bluetooth adapter on...");
                    Intent intent2 = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent2, REQUEST_ENABLE_BLUETOOTH);
                }
            }
        }
    };

    private void logBluetoothAdapterState(int state) {
        switch (state) {
            case BluetoothAdapter.STATE_CONNECTED:
                Log.i(TAG, "  STATE_CONNECTED");
                break;
            case BluetoothAdapter.STATE_CONNECTING:
                Log.i(TAG, "  STATE_CONNECTING");
                break;
            case BluetoothAdapter.STATE_DISCONNECTED:
                Log.i(TAG, "  STATE_DISCONNECTED");
                break;
            case BluetoothAdapter.STATE_DISCONNECTING:
                Log.i(TAG, "  STATE_DISCONNECTING");
                break;
            case BluetoothAdapter.STATE_OFF:
                Log.i(TAG, "  STATE_OFF");
                break;
            case BluetoothAdapter.STATE_ON:
                Log.i(TAG, "  STATE_ON");
                break;
            case BluetoothAdapter.STATE_TURNING_OFF:
                Log.i(TAG, "  STATE_TURNING_OFF");
                break;
            case BluetoothAdapter.STATE_TURNING_ON:
                Log.i(TAG, "  STATE_TURNING_ON");
                break;
            default:
                Log.i(TAG, "  unknown state.");
                break;
        }
    }
}
