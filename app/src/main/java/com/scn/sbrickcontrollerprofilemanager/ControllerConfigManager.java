package com.scn.sbrickcontrollerprofilemanager;

import android.util.Log;

/**
 * Controller config manager class.
 */
public class ControllerConfigManager {

    //
    // Private members
    //

    private static final String TAG = ControllerConfigManager.class.getSimpleName();

    //
    // Constructor
    //

    //
    // API
    //

    public void loadConfig() {
        Log.i(TAG, "loadConfig");
    }

    public void saveConfig() {
        Log.i(TAG, "saveConfig");
    }

    public int getMotionEventForControllerActionId(String controllerActionId) {

        switch (controllerActionId) {
            case ControllerProfile.CONTROLLER_ACTION_LEFT_JOY_HORIZONTAL:
            case ControllerProfile.CONTROLLER_ACTION_LEFT_JOY_VERTICAL:
            case ControllerProfile.CONTROLLER_ACTION_RIGHT_JOY_HORIZONTAL:
            case ControllerProfile.CONTROLLER_ACTION_RIGHT_JOY_VERTICAL:
            case ControllerProfile.CONTROLLER_ACTION_DPAD_HORIZONTAL:
            case ControllerProfile.CONTROLLER_ACTION_DPAD_VERTICAL:
            case ControllerProfile.CONTROLLER_ACTION_LEFT_TRIGGER:
            case ControllerProfile.CONTROLLER_ACTION_RIGHT_TRIGGER:



        }

        return -1;
    }
}
