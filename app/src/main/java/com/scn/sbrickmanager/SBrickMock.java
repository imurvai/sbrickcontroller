package com.scn.sbrickmanager;

import android.content.Context;
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

        return false;
    }

    @Override
    public void disconnect() {
        Log.i(TAG, "disconnect - " + getAddress());

        if (!isConnected) {
            Log.i(TAG, "  Already disconnected.");
            return;
        }

    }

    @Override
    public boolean getCharacteristicsAsync() {
        Log.i(TAG, "getCharacteristicsAsync - " + getAddress());

        if (!isConnected)
            throw new RuntimeException("SBrick hasn't been connected yet - " + getAddress());

        return false;
    }

    @Override
    public boolean sendCommand(int channel, int value, boolean invert) {
        Log.i(TAG, "sendCommand - " + getAddress());
        Log.i(TAG, "  channel: " + channel);
        Log.i(TAG, "  value:   " + value);
        Log.i(TAG, "  invert:  " + (invert ? "true" : "false"));

        if (!isConnected)
            throw new RuntimeException("SBrick hasn't been connected yet - " + getAddress());

        return true;
    }
}
