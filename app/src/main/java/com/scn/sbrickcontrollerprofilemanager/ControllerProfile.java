package com.scn.sbrickcontrollerprofilemanager;

import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * SBrick controller profile.
 */
public class ControllerProfile implements Parcelable {

    //
    // Public constants
    //

    public static final String CONTROLLER_ACTION_DPAD_LEFT_RIGHT = "CONTROLLER_ACTION_DPAD_LEFT_RIGHT";
    public static final String CONTROLLER_ACTION_DPAD_UP_DOWN = "CONTROLLER_ACTION_DPAD_UP_DOWN";
    public static final String CONTROLLER_ACTION_AXIS_X = "CONTROLLER_ACTION_AXIS_X";
    public static final String CONTROLLER_ACTION_AXIS_Y = "CONTROLLER_ACTION_AXIS_Y";
    public static final String CONTROLLER_ACTION_THUMB_L = "CONTROLLER_ACTION_THUMB_L";
    public static final String CONTROLLER_ACTION_AXIS_Z = "CONTROLLER_ACTION_AXIS_Z";
    public static final String CONTROLLER_ACTION_AXIS_RZ = "CONTROLLER_ACTION_AXIS_RZ";
    public static final String CONTROLLER_ACTION_THUMB_R = "CONTROLLER_ACTION_THUMB_R";
    public static final String CONTROLLER_ACTION_A = "CONTROLLER_ACTION_A";
    public static final String CONTROLLER_ACTION_B = "CONTROLLER_ACTION_B";
    public static final String CONTROLLER_ACTION_X = "CONTROLLER_ACTION_X";
    public static final String CONTROLLER_ACTION_Y = "CONTROLLER_ACTION_Y";
    public static final String CONTROLLER_ACTION_R1 = "CONTROLLER_ACTION_R1";
    public static final String CONTROLLER_ACTION_R_TRIGGER = "CONTROLLER_ACTION_R_TRIGGER";
    public static final String CONTROLLER_ACTION_L1 = "CONTROLLER_ACTION_L1";
    public static final String CONTROLLER_ACTION_L_TRIGGER = "CONTROLLER_ACTION_L_TRIGGER";
    public static final String CONTROLLER_ACTION_START = "CONTROLLER_ACTION_START";
    public static final String CONTROLLER_ACTION_SELECT = "CONTROLLER_ACTION_SELECT";

    //
    // Private members
    //

    private static final String TAG = ControllerProfile.class.getSimpleName();

    private static final String ProfileNameKey = "profile_name_key";
    private static final String ControllerActionIdCountKey = "controller_action_id_count_key";
    private static final String ControllerActionIdKey = "controller_action_id_key";
    private static final String ControllerActionCountKey = "controller_action_cound_key";

    private String name;
    private Map<String, Set<ControllerAction>> controllerActionMap = new HashMap();

    //
    // Constructor
    //

    /**
     * Creates a new instance of the ControllerProfile class.
     * @param name is the name of the profile.
     */
    public ControllerProfile(String name) {
        Log.i(TAG, "ControllerProfile - " + name);
        this.name = name;
    }

    ControllerProfile(SharedPreferences prefs, int profileIndex) {
        Log.i(TAG, "ControllerProfile from shared preferences...");

        name = prefs.getString(profileIndex + ProfileNameKey, "");
        Log.i(TAG, "  name: " + name);

        int size = prefs.getInt(profileIndex + "-" + ControllerActionIdCountKey, 0);
        for (int controllerActionIdIndex = 0; controllerActionIdIndex < size; controllerActionIdIndex++) {

            String controllerActionId = prefs.getString(profileIndex + "-" + controllerActionIdIndex + "-" + ControllerActionIdKey, "");
            Set<ControllerAction> controllerActions = new HashSet<>();
            controllerActionMap.put(controllerActionId, controllerActions);

            int size2 = prefs.getInt(profileIndex + "-" + controllerActionId + "-" + ControllerActionIdCountKey, 0);
            for (int controllerActionIndex = 0; controllerActionIndex < size2; controllerActionIndex++) {

                ControllerAction controllerAction = new ControllerAction(prefs, profileIndex, controllerActionId, controllerActionIndex);
                controllerActions.add(controllerAction);
            }
        }
    }

    ControllerProfile(Parcel parcel) {
        Log.i(TAG, "ControllerProfile from parcel...");

        if (parcel == null)
            throw new RuntimeException("parcel is null.");

        name = parcel.readString();
        Log.i(TAG, "  name: " + name);

        int size = parcel.readInt();
        for (int i = 0; i < size; i++) {
            String controllerActionId = parcel.readString();
            Set<ControllerAction> controllerActions = new HashSet<>();
            controllerActionMap.put(controllerActionId, controllerActions);

            int size2 = parcel.readInt();
            for (int j = 0; j < size2; j++) {
                ControllerAction controllerAction = parcel.readParcelable(ControllerAction.class.getClassLoader());
                controllerActions.add(controllerAction);
            }
        }
    }

    //
    // API
    //

    /**
     * Gets the name of the profile.
     * @return the name.
     */
    public String getName() { return name; }

    /**
     * Sets the name of the profile.
     * @param name
     */
    public void setName(String name) { this.name = name; }

    /**
     * Gets the controller actions specified by its Id.
     * @param controllerActionId
     * @return Set of controller actions.
     */
    public Set<ControllerAction> getControllerActions(String controllerActionId) {

        if (!controllerActionMap.containsKey(controllerActionId))
            return new HashSet<>();

        return controllerActionMap.get(controllerActionId);
    }

    /**
     * Adds the controller action for the specified ID.
     * @param controllerActionId is the controller action ID.
     * @param controllerAction is the controller action object.
     */
    public void addControllerAction(String controllerActionId, ControllerAction controllerAction) {
        Log.i(TAG, "setControllerAction - " + controllerActionId);

        Set<ControllerAction> controllerActions;
        if (controllerActionMap.containsKey(controllerActionId)) {
            controllerActions = controllerActionMap.get(controllerActionId);
        }
        else {
            controllerActions = new HashSet<>();
            controllerActionMap.put(controllerActionId, controllerActions);
        }

        controllerActions.add(controllerAction);
    }

    /**
     * Updates the controller action for the specified controller action id.
     * @param controllerActionId is the controller action id.
     * @param originalControllerAction is the original controller action.
     * @param newControllerAction is the new controller action.
     */
    public void updateControllerAction(String controllerActionId, ControllerAction originalControllerAction, ControllerAction newControllerAction) {
        Log.i(TAG, "updateControllerAction...");

        Set<ControllerAction> controllerActions;
        if (controllerActionMap.containsKey(controllerActionId)) {
            controllerActions = controllerActionMap.get(controllerActionId);
        }
        else {
            controllerActions = new HashSet<>();
            controllerActionMap.put(controllerActionId, controllerActions);
        }

        if (controllerActions.contains(originalControllerAction)) {
            controllerActions.remove(originalControllerAction);
        }

        controllerActions.add(newControllerAction);
    }

    /**
     * Removes the controller action for the specified controller action id.
     * @param controllerActionId is the controller action ID.
     * @param controllerAction is the controller action to remove.
     */
    public void removeControllerAction(String controllerActionId, ControllerAction controllerAction) {
        Log.i(TAG, "removeControllerAction...");

        if (!controllerActionMap.containsKey(controllerActionId))
            return;

        Set<ControllerAction> controllerActions = controllerActionMap.get(controllerActionId);
        controllerActions.remove(controllerAction);

        if (controllerActions.size() == 0)
            controllerActionMap.remove(controllerActions);
    }

    /**
     * Gets all the SBrick addresses exist in any of the controller actions.
     * @return Collection of SBrick addresses.
     */
    public Collection<String> getSBrickAddresses() {
        Log.i(TAG, "getSBrickAddresses...");

        Set<String> addresses = new HashSet<>();

        for (Set<ControllerAction> controllerActions : controllerActionMap.values())
            for (ControllerAction controllerAction : controllerActions)
                addresses.add(controllerAction.getSBrickAddress());

        return addresses;
    }

    /**
     * Gets the user friendly controller action name.
     * @param controllerActionId
     * @return The controller action name.
     */
    public static String getControllerActionName(String controllerActionId) {
        switch (controllerActionId) {
            case CONTROLLER_ACTION_DPAD_LEFT_RIGHT: return "Dpad horizontal";
            case CONTROLLER_ACTION_DPAD_UP_DOWN: return "Dpad vertical";
            case CONTROLLER_ACTION_AXIS_X: return "Left joy horizontal";
            case CONTROLLER_ACTION_AXIS_Y: return "Left joy vertical";
            case CONTROLLER_ACTION_THUMB_L: return "Left thumb";
            case CONTROLLER_ACTION_AXIS_Z: return "Right joy horizontal";
            case CONTROLLER_ACTION_AXIS_RZ: return "Right joy vertical";
            case CONTROLLER_ACTION_THUMB_R: return "Right thumb";
            case CONTROLLER_ACTION_A: return "Button A";
            case CONTROLLER_ACTION_B: return "Button B";
            case CONTROLLER_ACTION_X: return "Button X";
            case CONTROLLER_ACTION_Y: return "Button Y";
            case CONTROLLER_ACTION_R1: return "Right trigger button";
            case CONTROLLER_ACTION_R_TRIGGER: return "Right trigger";
            case CONTROLLER_ACTION_L1: return "Left trigger button";
            case CONTROLLER_ACTION_L_TRIGGER: return "Left trigger";
            case CONTROLLER_ACTION_START: return "Start button";
            case CONTROLLER_ACTION_SELECT: return "Select button";
        }
        return "";
    }

    //
    // Internal API
    //

    void saveToPreferences(SharedPreferences.Editor editor, int profileIndex) {
        Log.i(TAG, "saveToPreferences - " + getName());

        editor.putString(profileIndex + "-" + ProfileNameKey, getName());
        editor.putInt(profileIndex + "-" + ControllerActionIdCountKey, controllerActionMap.size());

        int controllerActionIdIndex = 0;
        for (Map.Entry<String, Set<ControllerAction>> kvp : controllerActionMap.entrySet()) {

            String controllerActionId = kvp.getKey();
            Set<ControllerAction> controllerActions = kvp.getValue();

            editor.putString(profileIndex + "-" + controllerActionIdIndex + "-" + ControllerActionIdKey, controllerActionId);
            editor.putInt(profileIndex + "-" + controllerActionId + "-" + ControllerActionCountKey, controllerActions.size());

            int controllerActionIndex = 0;
            for (ControllerAction controllerAction : controllerActions) {

                controllerAction.saveToPreferences(editor, profileIndex, controllerActionId, controllerActionIndex);
                controllerActionIndex++;
            }

            controllerActionIdIndex++;
        }
    }

    //
    // Parcelable overrides
    //

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Log.i(TAG, "writeToParcel - " + name);

        dest.writeString(name);
        dest.writeInt(controllerActionMap.size());

        for (Map.Entry<String, Set<ControllerAction>> kvp : controllerActionMap.entrySet()) {

            String controllerActionId = kvp.getKey();
            Set<ControllerAction> controllerActions = kvp.getValue();

            dest.writeString(controllerActionId);
            dest.writeInt(controllerActions.size());

            for (ControllerAction controllerAction : controllerActions)
                dest.writeParcelable(controllerAction, flags);
        }
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {

        @Override
        public Object createFromParcel(Parcel source) {
            return new ControllerProfile(source);
        }

        @Override
        public Object[] newArray(int size) {
            return new ControllerProfile[size];
        }
    };

    //
    // Object overrides
    //

    @Override
    public String toString() {
        return getName();
    }
}
