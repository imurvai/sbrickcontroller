package com.scn.sbrickmanager;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
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

        Log.i(TAG, "  Registering the scan broadcast receiver...");
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(scanBroadcastReceiver, filter);
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

        scannedSBrickDevices.clear();
        return bluetoothAdapter.startDiscovery();
    }

    @Override
    public void stopSBrickScan() {
        Log.i(TAG, "stopSBrickScan...");

        bluetoothAdapter.cancelDiscovery();
    }

    @Override
    public SBrick getSBrick(String sbrickAddress) {
        Log.i(TAG, "getSBrick - " + sbrickAddress);

        if (scannedSBrickDevices.containsKey(sbrickAddress)) {
            Log.i(TAG, "  SBrick device has already been created.");
            return scannedSBrickDevices.get(sbrickAddress);
        }

        Log.w(TAG, "  Create the SBrick device...");
        BluetoothDevice sbrickDevice = bluetoothAdapter.getRemoteDevice(sbrickAddress);
        if (sbrickDevice != null) {
            SBrick sbrick = new SBrickImpl(context, sbrickDevice);
            return sbrick;
        }

        return null;
    }

    //
    // Private methods
    //

    private final BroadcastReceiver scanBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "mScanBroadcastReceiver.onReceive...");

            switch (intent.getAction()) {
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    Log.i(TAG, "  ACTION_DISCOVERY_STARTED");
                    isScanning = true;
                    LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ACTION_START_SBRICK_SCAN));
                    break;

                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    Log.i(TAG, "  ACTION_DISCOVERY_FINISHED");
                    isScanning = false;
                    LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ACTION_STOP_SBRICK_SCAN));
                    break;

                case BluetoothDevice.ACTION_FOUND:
                    Log.i(TAG, "  ACTION_FOUND");

                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    BluetoothClass bluetoothClass = device.getBluetoothClass();

                    if (device != null) {
                        if (device.getName().equalsIgnoreCase("sbrick")) {
                            if (!scannedSBrickDevices.containsKey(device.getAddress())) {
                                Log.i(TAG, "  Storing SBrick.");
                                Log.i(TAG, "    Device name       : " + device.getName());
                                Log.i(TAG, "    Device address    : " + device.getAddress());
                                Log.i(TAG, "    Device class      : " + bluetoothClass.getDeviceClass());
                                Log.i(TAG, "    Device major class: " + bluetoothClass.getMajorDeviceClass());

                                SBrick sbrick = new SBrickImpl(context, device);
                                scannedSBrickDevices.put(device.getAddress(), sbrick);

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

                    break;

                case BluetoothDevice.ACTION_ACL_CONNECTED:
                    Log.i(TAG, "  ACTION_ACL_CONNECTED");
                    break;

                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    Log.i(TAG, "  ACTION_ACL_DISCONNECTED");
                    break;
            }
        }
    };
}
