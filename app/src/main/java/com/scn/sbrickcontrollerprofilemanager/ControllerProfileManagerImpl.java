package com.scn.sbrickcontrollerprofilemanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller profile manager implementation.
 */
class ControllerProfileManagerImpl implements ControllerProfileManager {

    //
    // Private members
    //

    private static final String TAG = ControllerProfileManagerImpl.class.getSimpleName();

    private static final String SBrickControllerProfilesPreferencesName = "SBrickControllerProfiles";
    private static final String SBrickControllerProfileCountKey = "SBrickControllerProfileCountKey";

    private Context context;
    private List<ControllerProfile> controllerProfiles = new ArrayList<ControllerProfile>();

    //
    // Singleton
    //

    ControllerProfileManagerImpl(Context context) {
        Log.i(TAG, "ControllerProfileManagerImpl...");

        this.context = context;
    }

    //
    // ControllerProfileManager overrides
    //

    @Override
    public synchronized boolean loadProfiles() {
        Log.i(TAG, "loadProfiles...");

        try {
            SharedPreferences prefs = context.getSharedPreferences(SBrickControllerProfilesPreferencesName, Context.MODE_PRIVATE);

            controllerProfiles.clear();

            int size = prefs.getInt(SBrickControllerProfileCountKey, 0);
            for (int profileIndex = 0; profileIndex < size; profileIndex++) {
                ControllerProfile profile = new ControllerProfile(prefs, profileIndex);
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

                ControllerProfile profile = controllerProfiles.get(profileIndex);
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
    public synchronized List<ControllerProfile> getProfiles() {
        Log.i(TAG, "getProfiles");
        return controllerProfiles;
    }

    @Override
    public synchronized ControllerProfile getProfileAt(int position) {
        Log.i(TAG, "getProfileAt - " + position);

        if (position < 0 || controllerProfiles.size() <= position)
            throw new RuntimeException("position is out of bound.");

        return controllerProfiles.get(position);
    }

    @Override
    public void addProfile(ControllerProfile profile) {
        Log.i(TAG, "addProfile - " + profile.getName());

        controllerProfiles.add(profile);
    }

    @Override
    public void updateProfileAt(int position, ControllerProfile profile) {
        Log.i(TAG, "updateProfileAt - " + position);

        controllerProfiles.set(position, profile);
    }

    @Override
    public synchronized void removeProfile(ControllerProfile profile) {
        Log.i(TAG, "removeProfile - " + profile.getName());

        if (controllerProfiles.contains(profile))
            controllerProfiles.remove(profile);
    }
}
