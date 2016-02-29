package com.scn.sbrickcontrollerprofilemanager;

import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller config manager class.
 */
public class ControllerConfigManager {

    //
    // Private members
    //

    private static final String TAG = ControllerConfigManager.class.getSimpleName();
    private static final String ControllerConfigKey = "controller_config_key";

    private Context context;

    private Map<String, Integer> controllerActionIdToMotionEventMap = new HashMap<>();
    private Map<Integer, String> keyCodeToControllerActionIdMap = new HashMap<>();

    //
    // Constructor
    //

    ControllerConfigManager(Context context) {
        this.context = context;
    }

    //
    // API
    //

    /**
     * Loads the controller config.
     */
    public void loadConfig() {
        Log.i(TAG, "loadConfig");

        try {

        }
        catch (Exception ex) {
            Log.e(TAG, "Error loading the controller config.", ex);
            resetToDefault();
        }
    }

    /**
     * Saves the current controller config.
     */
    public void saveConfig() {
        Log.i(TAG, "saveConfig");

        try {
            // TODO: implement!
            resetToDefault();
        }
        catch (Exception ex) {
            Log.e(TAG, "Error saving the controller config.", ex);
        }
    }

    /**
     * Gets the motion event id for the given controller action id.
     * @param controllerActionId is the controller action id.
     * @return The motion event id.
     */
    public int getMotionEventForControllerActionId(String controllerActionId) {

        if (controllerActionIdToMotionEventMap.containsKey(controllerActionId))
            return controllerActionIdToMotionEventMap.get(controllerActionId);

        return -1;
    }

    /**
     * Sets the motion event for the given controller action id.
     * @param controllerActionId is the controller action id.
     * @param motionEvent is the motion event.
     */
    public void setMotionEventForControllerActionId(String controllerActionId, int motionEvent) {
        controllerActionIdToMotionEventMap.put(controllerActionId, motionEvent);
    }

    /**
     * Gets the controller action id for the given key code.
     * @param keyCode is the key code.
     * @return The controller action id.
     */
    public String getControllerActionIdForKeyCode(int keyCode) {

        if (keyCodeToControllerActionIdMap.containsKey(keyCode))
            return keyCodeToControllerActionIdMap.get(keyCode);

        return null;
    }

    /**
     * Sets the controller action id for the given key code.
     * @param keyCode is the key code.
     * @param controllerActionId is the controller action id.
     */
    public void setControllerActionIdForKeyCode(int keyCode, String controllerActionId) {
        keyCodeToControllerActionIdMap.put(keyCode, controllerActionId);
    }

    /**
     * Resets controller action maps to default values.
     */
    public void resetToDefault() {

        controllerActionIdToMotionEventMap.clear();
        keyCodeToControllerActionIdMap.clear();

        controllerActionIdToMotionEventMap.put(ControllerProfile.CONTROLLER_ACTION_DPAD_HORIZONTAL, MotionEvent.AXIS_HAT_X);
        controllerActionIdToMotionEventMap.put(ControllerProfile.CONTROLLER_ACTION_DPAD_VERTICAL, MotionEvent.AXIS_HAT_Y);
        controllerActionIdToMotionEventMap.put(ControllerProfile.CONTROLLER_ACTION_LEFT_JOY_HORIZONTAL, MotionEvent.AXIS_X);
        controllerActionIdToMotionEventMap.put(ControllerProfile.CONTROLLER_ACTION_LEFT_JOY_VERTICAL, MotionEvent.AXIS_Y);
        controllerActionIdToMotionEventMap.put(ControllerProfile.CONTROLLER_ACTION_RIGHT_JOY_HORIZONTAL, MotionEvent.AXIS_Z);
        controllerActionIdToMotionEventMap.put(ControllerProfile.CONTROLLER_ACTION_RIGHT_JOY_VERTICAL, MotionEvent.AXIS_RZ);
        controllerActionIdToMotionEventMap.put(ControllerProfile.CONTROLLER_ACTION_LEFT_TRIGGER, MotionEvent.AXIS_LTRIGGER);
        controllerActionIdToMotionEventMap.put(ControllerProfile.CONTROLLER_ACTION_RIGHT_TRIGGER, MotionEvent.AXIS_RTRIGGER);

        keyCodeToControllerActionIdMap.put(KeyEvent.KEYCODE_BUTTON_THUMBL, ControllerProfile.CONTROLLER_ACTION_LEFT_THUMB);
        keyCodeToControllerActionIdMap.put(KeyEvent.KEYCODE_BUTTON_THUMBR, ControllerProfile.CONTROLLER_ACTION_RIGHT_THUMB);
        keyCodeToControllerActionIdMap.put(KeyEvent.KEYCODE_BUTTON_A, ControllerProfile.CONTROLLER_ACTION_A);
        keyCodeToControllerActionIdMap.put(KeyEvent.KEYCODE_BUTTON_B, ControllerProfile.CONTROLLER_ACTION_B);
        keyCodeToControllerActionIdMap.put(KeyEvent.KEYCODE_BUTTON_X, ControllerProfile.CONTROLLER_ACTION_X);
        keyCodeToControllerActionIdMap.put(KeyEvent.KEYCODE_BUTTON_Y, ControllerProfile.CONTROLLER_ACTION_Y);
        keyCodeToControllerActionIdMap.put(KeyEvent.KEYCODE_BUTTON_L1, ControllerProfile.CONTROLLER_ACTION_LEFT_TRIGGER_BUTTON);
        keyCodeToControllerActionIdMap.put(KeyEvent.KEYCODE_BUTTON_R1, ControllerProfile.CONTROLLER_ACTION_RIGHT_TRIGGER_BUTTON);
        keyCodeToControllerActionIdMap.put(KeyEvent.KEYCODE_BUTTON_SELECT, ControllerProfile.CONTROLLER_ACTION_SELECT);
        keyCodeToControllerActionIdMap.put(KeyEvent.KEYCODE_BUTTON_START, ControllerProfile.CONTROLLER_ACTION_START);
    }
}
