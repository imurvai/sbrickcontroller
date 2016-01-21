package com.scn.sbrickmanager;

import android.content.Context;
import android.content.SharedPreferences;
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

    private static final String SBrickMapPreferencesName = "SBrickMapPrefs";

    //
    // Protected members
    //

    protected final Context context;
    protected final Map<String, SBrick> sbrickMap = new HashMap<>();

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
    public void loadSBricks() {
        Log.i(TAG, "loadSBricks...");

        SharedPreferences prefs = context.getSharedPreferences(SBrickMapPreferencesName, Context.MODE_PRIVATE);

        sbrickMap.clear();

        HashMap<String, String> sbrickAddressAndNameMap = (HashMap<String, String>)prefs.getAll();
        for (String sbrickAddress : sbrickAddressAndNameMap.keySet()) {
            SBrick sbrick = getSBrick(sbrickAddress);
            sbrick.setName(sbrickAddressAndNameMap.get(sbrickAddress));
        }
    }

    @Override
    public void saveSBricks() {
        Log.i(TAG, "saveSBricks...");

        SharedPreferences prefs = context.getSharedPreferences(SBrickMapPreferencesName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Clear first
        editor.clear();

        // Write the sbricks
        for (String sbrickAddress : sbrickMap.keySet()) {
            editor.putString(sbrickAddress, sbrickMap.get(sbrickAddress).getName());
        }

        editor.commit();
    }

    @Override
    public Collection<SBrick> getSBricks() {
        Log.i(TAG, "getSBricks...");

        return sbrickMap.values();
    }

    @Override
    public void forgetSBrick(String sbrickAddress) {
        Log.i(TAG, "forgetSBrick - " + sbrickAddress);

        if (sbrickMap.containsKey(sbrickAddress))
            sbrickMap.remove(sbrickAddress);
    }
}
