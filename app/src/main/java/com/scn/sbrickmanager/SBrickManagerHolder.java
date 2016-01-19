package com.scn.sbrickmanager;

import android.content.Context;
import android.util.Log;

/**
 * SBrickManager holder class.
 */
public class SBrickManagerHolder {

    private static final String TAG = SBrickManagerHolder.class.getSimpleName();

    private static SBrickManager instance = null;

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

        if (instance != null)
            throw new RuntimeException("SBrickManager has already been created.");

        boolean isRealImplementation = false;
        instance = isRealImplementation ?
                new SBrickManagerImpl(context) :
                new SBrickManagerMock(context);
    }

    /**
     * Gets the SBrickManager object.
     * @return The SBrickManager.
     */
    public static SBrickManager getSBrickManager() {

        if (instance == null)
            throw new RuntimeException("SBrickManager hasn't been created yet.");

        return instance;
    }
}
