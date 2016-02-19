package com.scn.sbrickcontrollerprofilemanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller profile manager class.
 */
public class ControllerProfileManager {

    //
    // Private members
    //

    private static final String TAG = ControllerProfileManager.class.getSimpleName();

    private static final String SBrickControllerProfilesPreferencesName = "SBrickControllerProfiles";
    private static final String SBrickControllerProfileCountKey = "SBrickControllerProfileCountKey";
    private static final String SBrickControllerProfileNameKey = "SBrickControllerProfileNameKey";

    private Context context;
    private Map<String, ControllerProfile> controllerProfiles = new HashMap<>();

    //
    // Singleton
    //

    ControllerProfileManager(Context context) {
        Log.i(TAG, "ControllerProfileManager...");

        this.context = context;
    }

    //
    // API
    //

    /**
     * Loads the profiles from the shared preferences.
     * @return true if the loading was successful, false otherwise.
     */
    public synchronized boolean loadProfiles() {
        Log.i(TAG, "loadProfiles...");

        try {
            SharedPreferences prefs = context.getSharedPreferences(SBrickControllerProfilesPreferencesName, Context.MODE_PRIVATE);

            controllerProfiles.clear();

            int profileCount = prefs.getInt(SBrickControllerProfileCountKey, 0);

            for (int i = 0; i < profileCount; i++) {
                String profileName = prefs.getString(SBrickControllerProfileNameKey + "_" + i, "");
                ControllerProfile profile = new ControllerProfile(prefs, profileName);
                controllerProfiles.put(profile.getName(), profile);
            }
        }
        catch (Exception ex) {
            Log.e(TAG, "Error during loading SBricks.", ex);
            return false;
        }

        return true;
    }

    /**
     * Saves the profiles to the shared preferences.
     * @return true if the saving was successful, false otherwise.
     */
    public synchronized boolean saveProfiles() {
        Log.i(TAG, "saveProfiles...");

        try {
            SharedPreferences prefs = context.getSharedPreferences(SBrickControllerProfilesPreferencesName, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            editor.clear();

            editor.putInt(SBrickControllerProfileCountKey, controllerProfiles.size());

            int profileNameIndex = 0;
            for (ControllerProfile profile : controllerProfiles.values()) {

                editor.putString(SBrickControllerProfileNameKey + "_" + profileNameIndex++, profile.getName());
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

    /**
     * Gets all the controller profiles.
     * @return List of the controller profiles.
     */
    public synchronized List<ControllerProfile> getProfiles() {
        Log.i(TAG, "getProfiles...");
        return new ArrayList<>(controllerProfiles.values());
    }

    /**
     * Gets the controller profile specified by its name.
     * @param profileName is the name of the controller profile.
     * @return The controller profile.
     */
    public synchronized ControllerProfile getProfile(String profileName) {
        Log.i(TAG, "getProfile...");
        return controllerProfiles.get(profileName);
    }

    /**
     * Adds or updates the controller profile.
     * @param profile is the controller profile to update.
     * @param newProfileName is the new profile name or null if doesn't change.
     */
    public void addOrUpdateProfile(ControllerProfile profile, String newProfileName) {
        Log.i(TAG, "addOrUpdateProfile - " + profile.getName());

        if (newProfileName == null || newProfileName == profile.getName()) {
            controllerProfiles.put(profile.getName(), profile);
        }
        else {
            Log.i(TAG, "  rename to: " + newProfileName);

            controllerProfiles.remove(profile.getName());
            profile.setName(newProfileName);
            controllerProfiles.put(profile.getName(), profile);
        }
    }

    /**
     * Removes the controller profile specified by its name.
     * @param profileName is the name of the controller profile to remove.
     */
    public synchronized void removeProfile(String profileName) {
        Log.i(TAG, "removeProfile - " + profileName);

        if (controllerProfiles.containsKey(profileName))
            controllerProfiles.remove(profileName);
    }

    /**
     * Checks if the profile name is being used.
     * @param profileName is the profile name to check.
     * @return true if the name is already being used, false otherwise.
     */
    public synchronized boolean isProfileNameUsed(String profileName) {
        Log.i(TAG, "isProfileNameUnique - " + profileName);
        return controllerProfiles.containsKey(profileName);
    }

    /**
     * Gets a unique profile name.
     * @return The unique profile name.
     */
    public synchronized String getUniqueProfileName() {
        Log.i(TAG, "getUniqueProfileName...");

        String baseName = "My profile";
        if (!isProfileNameUsed(baseName))
            return baseName;

        for (int i = 1; ;i++) {
            String profileName = baseName + " " + i;
            if (!isProfileNameUsed(profileName))
                return profileName;
        }
    }
}
