package com.scn.sbrickmanager;

import android.content.Context;
import android.util.Log;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * SBrick manager base abstract class.
 */
abstract class SBrickManagerBase implements SBrickManager {

    //
    // Private members
    //

    private static final String TAG = SBrickManagerBase.class.getSimpleName();

    //
    // Protected members
    //

    protected final Context context;
    protected final Map<String, SBrick> scannedSBrickDevices = new HashMap<>();

    protected boolean isScanning = false;

    //
    // Constructor
    //

    protected SBrickManagerBase(Context context) {
        Log.i(TAG, "SBrickManagerBase...");

        this.context = context;
    }

    //
    // SBrickManager overrides
    //

    @Override
    public Collection<SBrick> getScannedSBricks() {
        Log.i(TAG, "getScannedSBricks...");

        return scannedSBrickDevices.values();
    }
}
