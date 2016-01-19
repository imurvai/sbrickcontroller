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

    private final String name;
    private final String address;

    private boolean isConnected = false;
    private boolean isReadingCharacterictics = false;

    private AsyncTask<Void, Void, Void> connectionAsyncTask = null;
    private AsyncTask<Void, Void, Void> readCharacteristicsAsyncTask = null;

    //
    // API
    //

    SBrickMock(Context context, String address, String name) {
        super(context);

        Log.i(TAG, "SBrickMock...");
        Log.i(TAG, "  Address: " + address);
        Log.i(TAG, "  Name   : " + name);

        this.address = address;
        this.name = name;
    }

    @Override
    public String getAddress() {
        return address;
    }

    @Override
    public String getName() {
        return name;
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
    public boolean getCharacteristicsAsync() {
        Log.i(TAG, "getCharacteristicsAsync - " + getAddress());

        if (!isConnected) {
            Log.i(TAG, "  Not connected yet.");
            return false;
        }

        if (isReadingCharacterictics) {
            Log.i(TAG, "  Already reading characteristics.");
            return false;
        }

        isReadingCharacterictics = true;
        readCharacteristicsAsyncTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {

                try {
                    Thread.sleep(300);
                }
                catch (Exception ex) {
                }

                SBrickCharacteristics characteristics = new SBrickCharacteristics();
                characteristics.setAddress(getAddress());
                characteristics.setDeviceName(getName());
                characteristics.setModelNumber("1");
                characteristics.setFirmwareRevision("1.0");
                characteristics.setHardwareRevision("1.0");
                characteristics.setSoftwareRevision("1.0");
                characteristics.setManufacturerName("SCN");

                Intent intent = new Intent();
                intent.setAction(ACTION_SBRICK_CHARACTERISTIC_READ);
                intent.putExtra(EXTRA_CHARACTERISTICS, characteristics);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

                return null;
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                isReadingCharacterictics = false;
                readCharacteristicsAsyncTask = null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                isReadingCharacterictics = false;
                readCharacteristicsAsyncTask = null;
            }
        }.execute();

        return true;
    }

    @Override
    public boolean sendCommand(int channel, int value, boolean invert) {
        Log.i(TAG, "sendCommand - " + getAddress());
        Log.i(TAG, "  channel: " + channel);
        Log.i(TAG, "  value:   " + value);
        Log.i(TAG, "  invert:  " + (invert ? "true" : "false"));

        if (!isConnected)
            return false;

        return true;
    }
}
