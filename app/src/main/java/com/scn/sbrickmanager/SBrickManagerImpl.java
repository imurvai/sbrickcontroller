package com.scn.sbrickmanager;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * SBrickManager implementation.
 */
class SBrickManagerImpl extends SBrickManagerBase {

    //
    // Private members
    //

    private static final String TAG = SBrickManagerImpl.class.getSimpleName();

    private BluetoothAdapter bluetoothAdapter;

    //
    // Constructor
    //

    SBrickManagerImpl(Context context) {
        super(context);

        Log.i(TAG, "SBrickManagerImpl...");

        if (!isBLESupported()) {
            Log.i(TAG, "BLE is not supported.");
            return;
        }

        Log.i(TAG, "  Retrieving the bluetooth adapter...");
        final BluetoothManager bluetoothManager = (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager == null)
            throw new RuntimeException("Can't find bluetooth manager.");

        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null)
            throw new RuntimeException("Can't find bluetooth adapter.");
    }

    //
    // SBrickManager overrides
    //

    @Override
    public boolean isBLESupported() {
        Log.i(TAG, "isBleSupported...");

        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    @Override
    public boolean isBluetoothOn() {
        Log.i(TAG, "isBluetoothOn...");

        final BluetoothManager bluetoothManager = (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager == null)
            throw new RuntimeException("Can't find bluetooth manager.");

        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null)
            throw new RuntimeException("Can't find bluetooth adapter.");

        return bluetoothAdapter.isEnabled();
    }

    @Override
    public boolean startSBrickScan() {
        Log.i(TAG, "startSBrickScan...");

        if (isScanning) {
            Log.w(TAG, "  Already scanning.");
            return false;
        }

        if (bluetoothAdapter.startLeScan(leScanCallback)) {
            isScanning = true;
            return true;
        }

        Log.w(TAG, "  Failed to start scanning.");
        return false;
    }

    @Override
    public void stopSBrickScan() {
        Log.i(TAG, "stopSBrickScan...");

        bluetoothAdapter.stopLeScan(leScanCallback);
        isScanning = false;
    }

    @Override
    public SBrick getSBrick(String sbrickAddress) {
        Log.i(TAG, "getSBrick - " + sbrickAddress);

        if (sbrickMap.containsKey(sbrickAddress)) {
            Log.i(TAG, "  SBrick device has already been created.");
            return sbrickMap.get(sbrickAddress);
        }

        Log.i(TAG, "  Create the SBrick device...");
        BluetoothDevice sbrickDevice = bluetoothAdapter.getRemoteDevice(sbrickAddress);
        if (sbrickDevice != null) {
            SBrick sbrick = new SBrickImpl(context, sbrickDevice);
            sbrickMap.put(sbrickAddress, sbrick);
            return sbrick;
        }

        return null;
    }

    //
    // Private methods
    //

    private final BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            Log.i(TAG, "onScanResult...");

            if (device != null && device.getName() != null) {
                if (device.getName().equalsIgnoreCase("sbrick")) {
                    if (!sbrickMap.containsKey(device.getAddress())) {
                        Log.i(TAG, "  Storing SBrick.");
                        Log.i(TAG, "    Device address    : " + device.getAddress());
                        Log.i(TAG, "    Device name       : " + device.getName());

                        SBrick sbrick = new SBrickImpl(context, device);
                        sbrickMap.put(device.getAddress(), sbrick);

                        Intent sendIntent = new Intent();
                        sendIntent.setAction(ACTION_FOUND_AN_SBRICK);
                        sendIntent.putExtra(EXTRA_SBRICK_NAME, device.getName());
                        sendIntent.putExtra(EXTRA_SBRICK_ADDRESS, device.getAddress());
                        LocalBroadcastManager.getInstance(context).sendBroadcast(sendIntent);
                    } else {
                        Log.i(TAG, "  Sbrick has already been discovered.");
                    }
                } else {
                    Log.i(TAG, "  Device is not an SBrick.");
                    Log.i(TAG, "    Name: " + device.getName());
                }
            }
            else {
                Log.i(TAG, "  Device is null.");
            }
        }
    };
}
