package com.scn.sbrickmanager;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * SBrick base abstract class.
 */
abstract class SBrickBase implements SBrick {

    //
    // Private members
    //

    private static final String TAG = SBrickImpl.class.getSimpleName();

    protected final Context context;

    private String name = null;

    //
    // Object overrides
    //

    @Override
    public String toString() {
        return getName();
    }

    //
    // SBrick overrides
    //

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    //
    // Internal API
    //

    protected SBrickBase(Context context) {
        Log.i(TAG, "SBrickBase...");

        this.context = context;
    }

    protected Intent buildBroadcastIntent(String action) {
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra(EXTRA_SBRICK_ADDRESS, getAddress());
        return intent;
    }

    protected void sendLocalBroadcast(String action) {
        Log.i(TAG, "sendLocalBroadcast...");
        LocalBroadcastManager.getInstance(context).sendBroadcast(buildBroadcastIntent(action));
    }
}
