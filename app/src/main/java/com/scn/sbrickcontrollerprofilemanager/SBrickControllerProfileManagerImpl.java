package com.scn.sbrickcontrollerprofilemanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.scn.sbrickmanager.SBrick;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * SBrick controller profile manager implementation.
 */
class SBrickControllerProfileManagerImpl implements SBrickControllerProfileManager {

    //
    // Private members
    //

    private static final String TAG = SBrickControllerProfileManagerImpl.class.getSimpleName();

    private static final String SBrickControllerProfilesPreferencesName = "SBrickControllerProfiles";
    private static final String SBrickControllerProfileCountKey = "SBrickControllerProfileCountKey";

    private Context context;
    private List<SBrickControllerProfile> controllerProfiles = new ArrayList<SBrickControllerProfile>();

    //
    // Singleton
    //

    SBrickControllerProfileManagerImpl(Context context) {
        Log.i(TAG, "SBrickControllerProfileManagerImpl...");

        this.context = context;
    }

    //
    // SBrickControllerProfileManager overrides
    //

    @Override
    public synchronized boolean loadProfiles() {
        Log.i(TAG, "loadProfiles...");

        try {
            SharedPreferences prefs = context.getSharedPreferences(SBrickControllerProfilesPreferencesName, Context.MODE_PRIVATE);

            controllerProfiles.clear();

            int size = prefs.getInt(SBrickControllerProfileCountKey, 0);
            for (int profileIndex = 0; profileIndex < size; profileIndex++) {
                SBrickControllerProfile profile = new SBrickControllerProfile(prefs, profileIndex);
                controllerProfiles.add(profile);
            }
        }
        catch (Exception ex) {
            Log.e(TAG, "Error during loading SBricks.", ex);
            return false;
        }

        return true;
    }

    @Override
    public synchronized boolean saveProfiles() {
        Log.i(TAG, "saveProfiles...");

        try {
            SharedPreferences prefs = context.getSharedPreferences(SBrickControllerProfilesPreferencesName, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            editor.clear();

            editor.putInt(SBrickControllerProfileCountKey, controllerProfiles.size());
            for (int profileIndex = 0; profileIndex < controllerProfiles.size(); profileIndex++) {

                SBrickControllerProfile profile = controllerProfiles.get(profileIndex);
                profile.saveToPreferences(editor, profileIndex);
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
    public synchronized List<SBrickControllerProfile> getProfiles() {
        Log.i(TAG, "getProfiles");
        return controllerProfiles;
    }

    @Override
    public synchronized SBrickControllerProfile getProfileAt(int position) {
        Log.i(TAG, "getProfileAt - " + position);

        if (position < 0 || controllerProfiles.size() <= position)
            throw new RuntimeException("position is out of bound.");

        return controllerProfiles.get(position);
    }

    @Override
    public void addProfile(SBrickControllerProfile profile) {
        Log.i(TAG, "addProfile - " + profile.getName());

        controllerProfiles.add(profile);
    }

    @Override
    public void updateProfileAt(int position, SBrickControllerProfile profile) {
        Log.i(TAG, "updateProfileAt - " + position);

        controllerProfiles.set(position, profile);
    }

    @Override
    public synchronized void removeProfile(SBrickControllerProfile profile) {
        Log.i(TAG, "removeProfile - " + profile.getName());

        if (controllerProfiles.contains(profile))
            controllerProfiles.remove(profile);
    }
}
