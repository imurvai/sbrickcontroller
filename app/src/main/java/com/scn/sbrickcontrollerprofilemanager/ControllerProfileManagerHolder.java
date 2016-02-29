package com.scn.sbrickcontrollerprofilemanager;

import android.content.Context;
import android.util.Log;

/**
 * Controller profile manager holder
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

    /**
     * Creates the controller profile manager.
     * @param context is the application context.
     */
    public static synchronized void createControllerProfileManager(Context context) {
        Log.i(TAG, "createControllerProfileManager...");

        if (manager != null)
            return;

        manager = new ControllerProfileManager(context);
    }

    /**
     * Gets the Controller profile manager instance.
     * @return The controller profile manager.
     */
    public static ControllerProfileManager getManager() {

        if (manager == null)
            throw new RuntimeException("Controller profile manager hasn't been created.");

        return manager;
    }
}
