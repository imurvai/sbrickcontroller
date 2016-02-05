package com.scn.sbrickmanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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
    // Protected abstract methods
    //

    protected abstract SBrick createSBrick(String sbrickAddress);

    //
    // SBrickManager overrides
    //

    @Override
    public boolean loadSBricks() {
        Log.i(TAG, "loadSBricks...");

        try {
            SharedPreferences prefs = context.getSharedPreferences(SBrickMapPreferencesName, Context.MODE_PRIVATE);

            sbrickMap.clear();

            HashMap<String, String> sbrickAddressAndNameMap = (HashMap<String, String>)prefs.getAll();
            for (String sbrickAddress : sbrickAddressAndNameMap.keySet()) {
                SBrick sbrick = createSBrick(sbrickAddress);
                sbrick.setName(sbrickAddressAndNameMap.get(sbrickAddress));
            }
        }
        catch (Exception ex) {
            Log.e(TAG, "Error during loading SBricks.", ex);
            return false;
        }

        return true;
    }

    @Override
    public boolean saveSBricks() {
        Log.i(TAG, "saveSBricks...");

        try {
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
        catch (Exception ex) {
            Log.e(TAG, "Error during saving SBricks.", ex);
            return false;
        }

        return true;
    }

    @Override
    public List<SBrick> getSBricks() {
        Log.i(TAG, "getSBricks...");

        return new ArrayList<>(sbrickMap.values());
    }

    @Override
    public List<String> getSBrickAddresses() {
        Log.i(TAG, "getSBrickAddresses...");

        List<String> sbrickAddresses = new ArrayList<>();
        for (SBrick sBrick : sbrickMap.values()) {
            sbrickAddresses.add(sBrick.getAddress());
        }

        return sbrickAddresses;
    }

    @Override
    public void forgetSBrick(String sbrickAddress) {
        Log.i(TAG, "forgetSBrick - " + sbrickAddress);

        if (sbrickMap.containsKey(sbrickAddress))
            sbrickMap.remove(sbrickAddress);
    }
}
