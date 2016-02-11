package com.scn.sbrickmanager;

import android.bluetooth.BluetoothGattCharacteristic;
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
        setName(name);
    }

    @Override
    public String getAddress() {
        return address;
    }

    @Override
    public synchronized boolean connect() {
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
            protected synchronized void onCancelled() {
                super.onCancelled();
                isConnected = false;
                connectionAsyncTask = null;
            }

            @Override
            protected synchronized void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                isConnected = true;
                connectionAsyncTask = null;
                isConnected = true;
                startCommandProcessing();
                sendLocalBroadcast(ACTION_SBRICK_CONNECTED);
            }
        }.execute();

        return true;
    }

    @Override
    public synchronized void disconnect() {
        Log.i(TAG, "disconnect - " + getAddress());

        if (connectionAsyncTask != null)
            connectionAsyncTask.cancel(true);

        if (!isConnected) {
            Log.i(TAG, "  Already disconnected.");
            return;
        }

        isConnected = false;
    }

    //
    // SBrickBase overrides
    //

    @Override
    protected boolean processCommand(Command command) {
        Log.i(TAG, "processCommand...");

        switch (command.getCommandType()) {

            case READ_CHARACTERISTIC:

                String value = "N/A";
                SBrickCharacteristicType characteristicType = (SBrickCharacteristicType)command.getCharacteristicType();

                switch (characteristicType) {
                    case DeviceName:
                        value = "SCNBrick";
                        break;

                    case ModelNumber:
                        value = "0";
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

                    default:
                        Log.i(TAG, "  Unknown characteristic.");
                        break;
                }

                Intent intent = new Intent();
                intent.setAction(ACTION_SBRICK_CHARACTERISTIC_READ);
                intent.putExtra(EXTRA_SBRICK_ADDRESS, getAddress());
                intent.putExtra(EXTRA_CHARACTERISTIC_TYPE, characteristicType.name());
                intent.putExtra(EXTRA_CHARACTERISTIC_VALUE, value);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

            case SEND_QUICK_DRIVE:
            case SEND_REMOTE_CONTROL:
                // Do nothing
                break;
        }

        commandSendingSemaphore.release();
        return true;
    }
}
