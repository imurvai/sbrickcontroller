package com.scn.sbrickcontrollerprofilemanager;

import android.content.Context;
import android.util.Log;

/**
 * SBrick controller profile manager holder
 */
public final class SBrickControllerProfileManagerHolder {

    //
    // Private members
    //

    private static final String TAG = SBrickControllerProfileManagerHolder.class.getSimpleName();

    private static SBrickControllerProfileManager manager = null;

    //
    // Private constructor
    //

    private SBrickControllerProfileManagerHolder() {
    }

    //
    // API
    //

    public static synchronized void createSBrickControllerProfileManager(Context context) {
        Log.i(TAG, "createSBrickControllerProfileManager...");

        if (manager != null)
            return;

        manager = new SBrickControllerProfileManagerImpl(context);
    }

    /**
     * Gets the SBRick controller profile manager instance.
     * @return The SBrick controller profile manager.
     */
    public static SBrickControllerProfileManager getManager() {

        if (manager == null)
            throw new RuntimeException("Sbrick controller profile manager hasn't been created.");

        return manager;
    }
}
