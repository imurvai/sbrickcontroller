package com.scn.sbrickcontrollerprofilemanager;

import android.content.Context;
import android.util.Log;

/**
 * SBrick controller profile manager holder
 */
public final class ControllerProfileManagerHolder {

    //
    // Private members
    //

    private static final String TAG = ControllerProfileManagerHolder.class.getSimpleName();

    private static ControllerProfileManager manager = null;

    //
    // Private constructor
    //

    private ControllerProfileManagerHolder() {
    }

    //
    // API
    //

    public static synchronized void createSBrickControllerProfileManager(Context context) {
        Log.i(TAG, "createSBrickControllerProfileManager...");

        if (manager != null)
            return;

        manager = new ControllerProfileManagerImpl(context);
    }

    /**
     * Gets the SBRick controller profile manager instance.
     * @return The SBrick controller profile manager.
     */
    public static ControllerProfileManager getManager() {

        if (manager == null)
            throw new RuntimeException("Sbrick controller profile manager hasn't been created.");

        return manager;
    }
}
