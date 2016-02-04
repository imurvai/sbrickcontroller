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
    public synchronized boolean loadProfiles() {
        Log.i(TAG, "loadProfiles...");

        controllerProfiles.clear();

        // Temp
        SBrickControllerProfile profile = new SBrickControllerProfile("TestProfile");
        String sbrickAddress = "00:07:80:2E:2C:05";
        SBrickControllerProfile.ControllerAction action1 = new SBrickControllerProfile.ControllerAction(sbrickAddress, 0, false);
        SBrickControllerProfile.ControllerAction action2 = new SBrickControllerProfile.ControllerAction(sbrickAddress, 1, false);
        SBrickControllerProfile.ControllerAction action3 = new SBrickControllerProfile.ControllerAction(sbrickAddress, 2, false);
        SBrickControllerProfile.ControllerAction action4 = new SBrickControllerProfile.ControllerAction(sbrickAddress, 3, true);
        SBrickControllerProfile.ControllerAction action5 = new SBrickControllerProfile.ControllerAction(sbrickAddress, 3, false);
        profile.setControllerAction(SBrickControllerProfile.CONTROLLER_ACTION_AXIS_X, action2);
        profile.setControllerAction(SBrickControllerProfile.CONTROLLER_ACTION_AXIS_RZ, action4);
        profile.setControllerAction(SBrickControllerProfile.CONTROLLER_ACTION_R_TRIGGER, action4);
        profile.setControllerAction(SBrickControllerProfile.CONTROLLER_ACTION_L_TRIGGER, action5);
        controllerProfiles.add(profile);

        return true;
    }

    @Override
    public synchronized boolean saveProfiles() {
        Log.i(TAG, "saveProfiles...");

        return true;
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
    public synchronized SBrickControllerProfile addNewProfile() {
        Log.i(TAG, "addNewProfile...");

        String name = "My profile";
        SBrickControllerProfile profile = new SBrickControllerProfile(name);
        controllerProfiles.add(profile);
        return profile;
    }

    @Override
    public synchronized void removeProfile(SBrickControllerProfile profile) {
        Log.i(TAG, "removeProfile - " + profile.getName());

        if (controllerProfiles.contains(profile))
            controllerProfiles.remove(profile);
    }
}
