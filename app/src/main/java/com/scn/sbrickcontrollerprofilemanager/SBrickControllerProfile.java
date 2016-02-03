package com.scn.sbrickcontrollerprofilemanager;

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
public class SBrickControllerProfile implements Parcelable {

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

    private static final String TAG = SBrickControllerProfile.class.getSimpleName();

    private String name;
    private Map<String, ControllerAction> controllerActionMap = new HashMap();

    //
    // Constructor
    //

    /**
     * Creates a new instance of the SBrickControllerProfile class.
     * @param name is the name of the profile.
     */
    SBrickControllerProfile(String name) {
        Log.i(TAG, "SBrickControllerProfile - " + name);
        this.name = name;
    }

    SBrickControllerProfile(Parcel parcel) {

        if (parcel == null)
            throw new RuntimeException("parcel is null.");

        name = parcel.readString();

        int size = parcel.readInt();
        for (int i = 0; i < size; i++) {
            String controllerActionId = parcel.readString();
            ControllerAction controllerAction = parcel.readParcelable(ControllerAction.class.getClassLoader());
            controllerActionMap.put(controllerActionId, controllerAction);
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
     * Gets the controller action specified by its ID.
     * @param controllerActionId is the controller action ID.
     * @return The controller action object.
     */
    public ControllerAction getControllerAction(String controllerActionId) {
        Log.i(TAG, "getControllerAction - " + controllerActionId);

        if (controllerActionMap.containsKey(controllerActionId))
            return controllerActionMap.get(controllerActionId);

        return null;
    }

    /**
     * Sets the controller action for the specified ID.
     * @param controllerActionId is the controller action ID.
     * @param controllerAction is the controller action object.
     */
    public void setControllerAction(String controllerActionId, ControllerAction controllerAction) {
        Log.i(TAG, "setControllerAction - " + controllerActionId);
        controllerActionMap.put(controllerActionId, controllerAction);
    }

    /**
     * Removes the controller action.
     * @param controllerActionId is the controller action ID.
     */
    public void removeControllerAction(String controllerActionId) {
        Log.i(TAG, "deleteControllerAction...");

        if (controllerActionMap.containsKey(controllerActionId))
            controllerActionMap.remove(controllerActionId);
    }

    /**
     * Gets all the SBrick addresses exist in any of the controller actions.
     * @return Collection of SBrick addresses.
     */
    public Collection<String> getSBrickAddresses() {
        Log.i(TAG, "getSBrickAddresses...");

        Set<String> addresses = new HashSet<>();

        for (ControllerAction controllerAction : controllerActionMap.values()) {
            addresses.add(controllerAction.getSbrickAddress());
        }

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
    // Parcelable overrides
    //

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (dest == null)
            return;

        dest.writeString(name);
        dest.writeInt(controllerActionMap.size());
        for (Map.Entry<String, ControllerAction> entry : controllerActionMap.entrySet()) {
            dest.writeString(entry.getKey());
            dest.writeParcelable(entry.getValue(), flags);
        }
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {

        @Override
        public Object createFromParcel(Parcel source) {
            return new SBrickControllerProfile(source);
        }

        @Override
        public Object[] newArray(int size) {
            return new SBrickControllerProfile[size];
        }
    };

    //
    //
    //

    /**
     * Controller action class.
     */
    public static class ControllerAction implements Parcelable {

        //
        // Private members.
        //

        private String sbrickAddress;
        private int channel;
        private boolean invert;

        //
        // Constructor.
        //

        /**
         * Creates a new instance of the ControllerAction class.
         * @param sbrickAddress is the address of the SBrick.
         * @param channel is the channel.
         * @param invert is true if value has to be inverted.
         * @throws IllegalArgumentException
         */
        public ControllerAction(String sbrickAddress, int channel, boolean invert) {

            if (sbrickAddress == null)
                throw new RuntimeException("SBRick address is null;");

            if (channel < 0 || channel > 3)
                throw new RuntimeException("Channel must be in range [0, 3].");

            this.sbrickAddress = sbrickAddress;
            this.channel = channel;
            this.invert = invert;
        }

        public ControllerAction(Parcel parcel) {

            if (parcel == null)
                throw new RuntimeException("parcel is null.");

            sbrickAddress = parcel.readString();
            channel = parcel.readInt();
            invert = parcel.readInt() != 0;
        }

        //
        // API
        //

        /**
         * Gets the SBrick address.
         * @return The SBrick address.
         */
        public String getSbrickAddress() { return sbrickAddress; }

        /**
         * Gets the channel.
         * @return The channel.
         */
        public int getChannel() { return channel; }

        /**
         * Gets the value indicating if the value has to be inverted.
         * @return True if the value has to be inverted, false otherwise.
         */
        public boolean getInvert() { return invert; }

        //
        // Parcelable methods
        //

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(sbrickAddress);
            dest.writeInt(channel);
            dest.writeInt(invert ? 1 : 0);
        }

        public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {

            @Override
            public Object createFromParcel(Parcel source) {
                return new ControllerAction(source);
            }

            @Override
            public Object[] newArray(int size) {
                return new ControllerAction[size];
            }
        };
    }
}
