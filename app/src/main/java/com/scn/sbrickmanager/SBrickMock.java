package com.scn.sbrickmanager;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * SBrick mock implementation.
 */
public class SBrickMock extends SBrickBase {

    //
    // Private fields
    //

    private final static String TAG = SBrickMock.class.getSimpleName();

    private final String address;

    private boolean isConnected = false;

    private AsyncTask<Void, Void, Void> connectionAsyncTask = null;

    //
    // API
    //

    SBrickMock(Context context, String address, String name) {
        super(context);

        Log.i(TAG, "SBrickMock...");
        Log.i(TAG, "  Address: " + address);
        Log.i(TAG, "  Name   : " + name);

        this.address = address;
        setName(name + " " + address);
    }

    @Override
    public String getAddress() {
        return address;
    }

    @Override
    public boolean connect() {
        Log.i(TAG, "connect - " + getAddress());

        if (isConnected) {
            Log.i(TAG, "  Already connected.");
            return false;
        }

        if (connectionAsyncTask != null) {
            Log.i(TAG, "  Already connecting.");
            return false;
        }

        connectionAsyncTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {

                try {
                    Thread.sleep(300);
                }
                catch (Exception ex) {
                }

                return null;
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                isConnected = false;
                connectionAsyncTask = null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                isConnected = true;
                connectionAsyncTask = null;
                sendLocalBroadcast(ACTION_SBRICK_CONNECTED);
            }
        }.execute();

        return true;
    }

    @Override
    public void disconnect() {
        Log.i(TAG, "disconnect - " + getAddress());

        if (!isConnected) {
            Log.i(TAG, "  Already disconnected.");
            return;
        }

        if (connectionAsyncTask != null)
            connectionAsyncTask.cancel(true);

        isConnected = false;
    }

    @Override
    public boolean isConnected() {
        return isConnected;
    }

    @Override
    public boolean readCharacteristic(SBrickCharacteristicType characteristicType) {
        Log.i(TAG, "readCharacteristic - " + getAddress());

        if (!isConnected) {
            Log.i(TAG, "  Not connected yet.");
            return false;
        }

        String value = "N/A";
        switch (characteristicType) {
            case DeviceName:
                value = "SCNBrick";
                break;
            case ModelNumber:
                value = "12345";
                break;
            case FirmwareRevision:
            case HardwareRevision:
            case SoftwareRevision:
                value = "1.0";
                break;
            case ManufacturerName:
                value = "SCN";
                break;

            case Appearance:
                value = "0";
                break;
        }

        Intent intent = new Intent();
        intent.setAction(ACTION_SBRICK_CHARACTERISTIC_READ);
        intent.putExtra(EXTRA_SBRICK_ADDRESS, getAddress());
        intent.putExtra(EXTRA_CHARACTERISTIC_TYPE, characteristicType);
        intent.putExtra(EXTRA_CHARACTERISTIC_VALUE, value);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        return true;
    }

    @Override
    public boolean sendCommand(int channel, int value) {
        Log.i(TAG, "sendCommand - " + getAddress());
        return isConnected;
    }

    @Override
    public boolean sendCommand(int v1, int v2, int v3, int v4) {
        Log.i(TAG, "sendCommand - " + getAddress());
        return isConnected;
    }
}
