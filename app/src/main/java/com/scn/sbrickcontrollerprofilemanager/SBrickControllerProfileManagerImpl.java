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
    public synchronized int getProfileCount() {
        Log.i(TAG, "getProfileCount...");

        int size = controllerProfiles.size();
        Log.i(TAG, "  count: " + size);

        return size;
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
    public synchronized void addProfile(String name) {
        Log.i(TAG, "addProfile - " + name);

        if (getProfile(name) !=  null)
            throw new RuntimeException("A profile with the same name already exists.");

        controllerProfiles.add(new SBrickControllerProfile(name));
    }

    @Override
    public synchronized void removeProfile(SBrickControllerProfile profile) {
        Log.i(TAG, "removeProfile - " + profile.getName());

        if (controllerProfiles.contains(profile))
            controllerProfiles.remove(profile);
    }
}
