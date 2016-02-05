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
            for (int i = 0; i < size; i++) {
                SBrickControllerProfile profile = new SBrickControllerProfile(prefs);
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
            for (SBrickControllerProfile profile : controllerProfiles) {
                profile.saveToPreferences(editor);
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
    public SBrickControllerProfile addProfile(String name) {
        Log.i(TAG, "addProfile - " + name);

        SBrickControllerProfile profile = new SBrickControllerProfile(name);
        controllerProfiles.add(profile);

        return profile;
    }

    @Override
    public void UpdateProfileAt(int position, SBrickControllerProfile profile) {

    }

    @Override
    public synchronized void removeProfile(SBrickControllerProfile profile) {
        Log.i(TAG, "removeProfile - " + profile.getName());

        if (controllerProfiles.contains(profile))
            controllerProfiles.remove(profile);
    }
}
