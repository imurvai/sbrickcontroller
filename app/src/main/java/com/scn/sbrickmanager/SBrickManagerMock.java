package com.scn.sbrickmanager;

import android.content.Context;
import android.util.Log;

class SBrickManagerMock extends SBrickManagerBase {

    //
    // Private members
    //

    private static final String TAG = SBrickManagerMock.class.getSimpleName();

    //
    // Constructor
    //

    SBrickManagerMock(Context context) {
        super(context);

        Log.i(TAG, "SBrickManagerMock...");
    }

    //
    // SBrickManager overrides
    //

    @Override
    public boolean startSBrickScan() {
        Log.i(TAG, "startSBrickScan...");

        if (isScanning) {
            Log.w(TAG, "  Already scanning.");
            return false;
        }

        return false;
    }

    @Override
    public void stopSBrickScan() {
        Log.i(TAG, "stopSBrickScan...");

        if (!isScanning) {
            Log.i(TAG, "  Not scanning.");
            return;
        }

    }

    @Override
    public SBrick getSBrick(String sbrickAddress) {
        Log.i(TAG, "getSBrick - " + sbrickAddress);

        if (scannedSBrickDevices.containsKey(sbrickAddress))
            return scannedSBrickDevices.get(sbrickAddress);

        SBrick sbrick = new SBrickMock(context, sbrickAddress, sbrickAddress);
        return sbrick;
    }

    //
    // Private methods
    //

}
