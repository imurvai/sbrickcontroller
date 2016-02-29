package com.scn.sbrickcontrollerprofilemanager;

import android.content.Context;
import android.util.Log;

/**
 * Controller config manager holder class.
 */
public class ControllerConfigManagerHolder {

    //
    // Private members
    //

    private static final String TAG = ControllerConfigManagerHolder.class.getSimpleName();

    private static ControllerConfigManager manager = null;

    //
    // Private constructor
    //

    private ControllerConfigManagerHolder() {
    }

    //
    // API
    //

    /**
     * Creates the controller config manager.
     * @param context is the application context.
     */
    public static synchronized void createControllerConfigManager(Context context) {
        Log.i(TAG, "createControllerConfigManager...");

        if (manager != null)
            return;

        manager = new ControllerConfigManager(context);
    }

    /**
     * Gets the Controller config manager instance.
     * @return The controller config manager.
     */
    public static ControllerConfigManager getManager() {

        if (manager == null)
            throw new RuntimeException("Controller config manager hasn't been created.");

        return manager;
    }
}
