package com.scn.sbrickmanager;

import android.content.Context;
import android.util.Log;

/**
 * SBrickManager holder class.
 */
public class SBrickManagerHolder {

    //
    // Private members
    //

    private static final String TAG = SBrickManagerHolder.class.getSimpleName();

    private static SBrickManager manager = null;

    //
    // Private constructor
    //

    private SBrickManagerHolder() {
    }

    //
    // API
    //

    /**
     * Creates the one and only SBrickManager object.
     * @param context The application context.
     */
    public static void CreateSBrickManagerSingleton(Context context) {
        Log.i(TAG, "CreateSBrickManager...");

        if (manager != null)
            return;

        manager = new SBrickManagerImpl(context);
    }

    /**
     * Gets the SBrickManager object.
     * @return The SBrickManager.
     */
    public static synchronized SBrickManager getManager() {

        if (manager == null)
            throw new RuntimeException("SBrickManager hasn't been created yet.");

        return manager;
    }
}
