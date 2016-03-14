package com.scn.sbrickmanager;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.scn.sbrickmanager.sbrickcommand.Command;
import com.scn.sbrickmanager.sbrickcommand.CommandMethod;

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
    private AsyncTask<Void, Void, Void> discoverServicesAsyncTask = null;

    //
    // Constructor
    //

    SBrickMock(Context context, SBrickManagerBase sbrickManager, String address, String name) {
        super(context, sbrickManager);

        Log.i(TAG, "SBrickMock...");
        Log.i(TAG, "  Address: " + address);
        Log.i(TAG, "  Name   : " + name);

        this.address = address;
        setName(name);
    }

    //
    // SBrick overrides
    //

    @Override
    public String getAddress() {
        return address;
    }

    //
    // SBrickBase overrides
    //

    @Override
    protected CommandMethod createConnectCommandMethod() {
        Log.i(TAG, "createConnectCommandMethod - " + getAddress());

        return new CommandMethod() {
            @Override
            public synchronized boolean execute() {

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

                        sbrickManager.releaseCommandSemaphore();
                    }

                    @Override
                    protected synchronized void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        connectionAsyncTask = null;

                        // Discover services
                        CommandMethod commandMethod = createDiscoverServicesCommandMethod();
                        sbrickManager.sendCommand(Command.newDiscoverServicesCommand(SBrickMock.this, commandMethod));

                        sbrickManager.releaseCommandSemaphore();
                    }
                }.execute();

                return true;
            }
        };
    }

    @Override
    protected CommandMethod createDiscoverServicesCommandMethod() {
        Log.i(TAG, "createDiscoverServicesCommandMethod - " + getAddress());

        return new CommandMethod() {
            @Override
            public synchronized boolean execute() {

                discoverServicesAsyncTask = new AsyncTask<Void, Void, Void>() {

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
                        discoverServicesAsyncTask = null;

                        sbrickManager.releaseCommandSemaphore();
                    }

                    @Override
                    protected synchronized void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        discoverServicesAsyncTask = null;
                        isConnected = true;

                        sbrickManager.releaseCommandSemaphore();
                        sendLocalBroadcast(ACTION_SBRICK_CONNECTED);
                    }
                }.execute();

                return true;
            }
        };
    }

    @Override
    protected CommandMethod createReadCharacteristicCommandMethod(final SBrickCharacteristicType characteristicType) {
        Log.i(TAG, "createReadCharacteristicCommandMethod - " + getAddress());

        return new CommandMethod() {
            @Override
            public boolean execute() {

                String value = "N/A";

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


                sbrickManager.releaseCommandSemaphore();

                Intent intent = new Intent();
                intent.setAction(ACTION_SBRICK_CHARACTERISTIC_READ);
                intent.putExtra(EXTRA_SBRICK_ADDRESS, getAddress());
                intent.putExtra(EXTRA_CHARACTERISTIC_TYPE, characteristicType.name());
                intent.putExtra(EXTRA_CHARACTERISTIC_VALUE, value);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

                return true;
            }
        };
    }

    @Override
    protected CommandMethod createWriteRemoteControlCommandMethod(int channel, int value) {
        Log.i(TAG, "createWriteRemoteControlCommandMethod - " + getAddress());

        return new CommandMethod() {
            @Override
            public boolean execute() {

                sbrickManager.releaseCommandSemaphore();
                return true;
            }
        };
    }

    @Override
    protected CommandMethod createWriteQuickDriveCommandMethod(int v0, int v1, int v2, int v3) {
        Log.i(TAG, "createWriteQuickDriveCommandMethod - " + getAddress());

        return new CommandMethod() {
            @Override
            public boolean execute() {

                sbrickManager.releaseCommandSemaphore();
                return true;
            }
        };
    }

    @Override
    public synchronized void disconnect() {
        Log.i(TAG, "disconnect - " + getAddress());

        if (connectionAsyncTask != null)
            connectionAsyncTask.cancel(true);

        if (discoverServicesAsyncTask != null)
            discoverServicesAsyncTask.cancel(true);

        if (!isConnected) {
            Log.i(TAG, "  Already disconnected.");
            return;
        }

        isConnected = false;
    }
}
