package com.scn.sbrickcontroller;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

/**
 * The one and only activity.
 */
public class MainActivity extends FragmentActivity {

    //
    // Private members
    //

    private final String TAG = MainActivity.class.getSimpleName();

    private final int REQUEST_ENABLE_BLUETOOTH = 0x1234;

    //
    // Activity overrides
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate...");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if (findViewById(R.id.fragment_container) != null) {

            if (savedInstanceState != null) {
                Log.i(TAG, "  savedInstanceState is not null.");
                return;
            }

            Log.i(TAG, "  Create the main fragment...");
            MainFragment mainFragment = MainFragment.newInstance();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, mainFragment)
                    .commit();
        }
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume...");
        super.onResume();

        Log.i(TAG, "  Register the BluetoothAdapter broadcast receiver...");
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bluetoothAdapterBroadcastReceiver, filter);

        if (!Helper.isBluetoothOn(this)) {
            Log.i(TAG, "  Bluetooth is off, ask to turn it on...");
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_ENABLE_BLUETOOTH);
        }
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause...");
        super.onPause();

        Log.i(TAG, "  Unregister the BluetoothAdapter broadcast receiver...");
        unregisterReceiver(bluetoothAdapterBroadcastReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult...");

        switch (requestCode) {
            case REQUEST_ENABLE_BLUETOOTH:
                Log.i(TAG, "  REQUEST_ENABLE_BLUETOOTH");
                if (resultCode == RESULT_OK)
                    Log.i(TAG, "  RESULT_OK");
                else {
                    Log.i(TAG, "  RESULT_CANCEL");
                    finish();
                }
                break;

            default:
                Log.i(TAG, "  Unknown request");
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        Fragment fr = getVisibleFragment();
        if (fr != null && fr instanceof GameControllerActionListener && ((GameControllerActionListener) fr).onKeyDown(keyCode, event))
            return true;

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        Fragment fr = getVisibleFragment();
        if (fr != null && fr instanceof GameControllerActionListener && ((GameControllerActionListener) fr).onKeyUp(keyCode, event))
            return true;

        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {

        Fragment fr = getVisibleFragment();
        if (fr != null && fr instanceof GameControllerActionListener && ((GameControllerActionListener) fr).onGenericMotionEvent(event))
            return true;

        return super.onGenericMotionEvent(event);
    }

    //
    // API
    //

    public void goBackFromFragment() {
        Log.i(TAG, "goBackFromFragment...");
        getFragmentManager().popBackStack();
    }

    public void startScanSBrickFragment() {
        Log.i(TAG, "startScanSBrickFragment...");

        SBrickListFragment sBrickListFragment = SBrickListFragment.newInstance();
        startFragment(sBrickListFragment);
    }

    public void startSBrickDetailsFragment(String sbrickAddress) {
        Log.i(TAG, "startSBrickDetailsFragment - " + sbrickAddress);

        SBrickDetailsFragment sBrickDetailsFragment = SBrickDetailsFragment.newInstance(sbrickAddress);
        startFragment(sBrickDetailsFragment);
    }

    //
    // Private methods
    //

    private void startFragment(Fragment fragment) {
        Log.i(TAG, "startFragment...");

        getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(null)
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private Fragment getVisibleFragment() {
        Log.i(TAG, "getVisibleFragment...");

        FragmentManager fm = getSupportFragmentManager();
        for (Fragment fr : fm.getFragments()) {
            if (fr.isVisible()) {
                return fr;
            }
        }

        Log.i(TAG, "  No visible fragment found.");
        return null;
    }

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
