package com.scn.sbrickcontrollerprofilemanager;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * SBrick controller profile manager implementation.
 */
class SBrickControllerProfileManagerImpl implements SBrickControllerProfileManager {

    //
    // Private members
    //

    private static final String TAG = SBrickControllerProfileManagerImpl.class.getSimpleName();

    private List<SBrickControllerProfile> controllerProfiles;

    //
    // Singleton
    //

    private static SBrickControllerProfileManagerImpl instance = null;

    private SBrickControllerProfileManagerImpl() {
        Log.i(TAG, "SBrickControllerProfileManagerImpl...");

        controllerProfiles = new ArrayList<SBrickControllerProfile>();
    }

    public static SBrickControllerProfileManager getInstance() {
        if (instance == null)
            instance = new SBrickControllerProfileManagerImpl();

        return instance;
    }

    //
    // SBrickControllerProfileManager overrides
    //

    @Override
    public synchronized void loadProfiles() {

    }

    @Override
    public synchronized void saveProfiles() {

    }

    @Override
    public synchronized Collection<SBrickControllerProfile> getProfiles() {
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
    public synchronized SBrickControllerProfile getProfile(String name) {
        Log.i(TAG, "getProfile - " + name);

        for (SBrickControllerProfile profile : controllerProfiles) {
            if (profile.getName() == name)
                return profile;
        }

        Log.w(TAG, "  profile not found.");
        return null;
    }

    @Override
    public synchronized SBrickControllerProfile addNewProfile() {
        Log.i(TAG, "addNewProfile...");

        String name = "Controller profile";
        if (getProfile(name) != null) {
            for (int i = 1; ; i++) {
                String nameWithPostfix = name + " " + i;
                if (getProfile(nameWithPostfix) != null) {
                    name = nameWithPostfix;
                    break;
                }
            }
        }

        SBrickControllerProfile profile = new SBrickControllerProfile(name);
        controllerProfiles.add(profile);
        return profile;
    }

    @Override
    public synchronized boolean renameProfile(SBrickControllerProfile profile, String newName) {
        Log.i(TAG, "renameProfile...");
        Log.i(TAG, "  from: " + profile.getName());
        Log.i(TAG, "  to:   " + newName);

        if (getProfile(newName) != null) {
            Log.w(TAG, "  Profile with the same name already exists.");
            return false;
        }

        profile.setName(newName);
        return true;
    }

    @Override
    public synchronized void removeProfile(SBrickControllerProfile profile) {
        Log.i(TAG, "removeProfile - " + profile.getName());

        if (controllerProfiles.contains(profile))
            controllerProfiles.remove(profile);
    }
}
