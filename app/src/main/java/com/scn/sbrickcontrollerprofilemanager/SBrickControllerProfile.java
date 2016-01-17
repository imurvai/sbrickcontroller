package com.scn.sbrickcontrollerprofilemanager;

/**
 * SBrick controller profile.
 */
public class SBrickControllerProfile {

    //
    // Public constants
    //

    public static final int CONTROLLER_ACTION_DPAD_LEFT_RIGHT = 1;
    public static final int CONTROLLER_ACTION_DPAD_UP_DOWN = 2;
    public static final int CONTROLLER_ACTION_AXIS_X = 3;
    public static final int CONTROLLER_ACTION_AXIS_Y = 4;
    public static final int CONTROLLER_ACTION_THUMB_L = 5;
    public static final int CONTROLLER_ACTION_AXIS_Z = 6;
    public static final int CONTROLLER_ACTION_AXIS_RZ = 7;
    public static final int CONTROLLER_ACTION_THUMB_R = 8;
    public static final int CONTROLLER_ACTION_A = 9;
    public static final int CONTROLLER_ACTION_B = 10;
    public static final int CONTROLLER_ACTION_X = 11;
    public static final int CONTROLLER_ACTION_Y = 12;
    public static final int CONTROLLER_ACTION_R1 = 13;
    public static final int CONTROLLER_ACTION_R_TRIGGER = 14;
    public static final int CONTROLLER_ACTION_L1 = 15;
    public static final int CONTROLLER_ACTION_L_TRIGGER = 16;
    public static final int CONTROLLER_ACTION_START = 17;
    public static final int CONTROLLER_ACTION_SELECT = 18;

    //
    // Private members
    //

    private String name;

    private ControllerAction dpadLeftRightControllerAction;
    private ControllerAction dpadUpDownControllerAction;
    private ControllerAction axisXControllerAction;
    private ControllerAction axisYControllerAction;
    private ControllerAction thumbLControllerAction;
    private ControllerAction axisZControllerAction;
    private ControllerAction axisRZControllerAction;
    private ControllerAction thumbRControllerAction;
    private ControllerAction aControllerAction;
    private ControllerAction bControllerAction;
    private ControllerAction xControllerAction;
    private ControllerAction yControllerAction;
    private ControllerAction l1ControllerAction;
    private ControllerAction lTriggerControllerAction;
    private ControllerAction r1ControllerAction;
    private ControllerAction rTriggerControllerAction;
    private ControllerAction startControllerAction;
    private ControllerAction selectTriggerControllerAction;

    //
    // Constructor
    //

    public SBrickControllerProfile(String name) {
        this.name = name;
    }

    //
    // API
    //

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public ControllerAction getControllerAction(int controllerActionId) {
        switch (controllerActionId) {
            case CONTROLLER_ACTION_DPAD_LEFT_RIGHT:
                return dpadLeftRightControllerAction;
            case CONTROLLER_ACTION_DPAD_UP_DOWN:
                return dpadUpDownControllerAction;
            case CONTROLLER_ACTION_AXIS_X:
                return axisXControllerAction;
            case CONTROLLER_ACTION_AXIS_Y:
                return axisYControllerAction;
            case CONTROLLER_ACTION_THUMB_L:
                return thumbLControllerAction;
            case CONTROLLER_ACTION_AXIS_Z:
                return axisZControllerAction;
            case CONTROLLER_ACTION_AXIS_RZ:
                return axisRZControllerAction;
            case CONTROLLER_ACTION_THUMB_R:
                return thumbRControllerAction;
            case CONTROLLER_ACTION_A:
                return aControllerAction;
            case CONTROLLER_ACTION_B:
                return bControllerAction;
            case CONTROLLER_ACTION_X:
                return xControllerAction;
            case CONTROLLER_ACTION_Y:
                return yControllerAction;
            case CONTROLLER_ACTION_L1:
                return l1ControllerAction;
            case CONTROLLER_ACTION_L_TRIGGER:
                return lTriggerControllerAction;
            case CONTROLLER_ACTION_R1:
                return r1ControllerAction;
            case CONTROLLER_ACTION_R_TRIGGER:
                return rTriggerControllerAction;
            case CONTROLLER_ACTION_START:
                return startControllerAction;
            case CONTROLLER_ACTION_SELECT:
                return selectTriggerControllerAction;
        }

        return null;
    }

    public void setControllerAction(int controllerActionId, ControllerAction controllerAction) {
        switch (controllerActionId) {
            case CONTROLLER_ACTION_DPAD_LEFT_RIGHT:
                dpadLeftRightControllerAction = controllerAction;
            case CONTROLLER_ACTION_DPAD_UP_DOWN:
                dpadUpDownControllerAction = controllerAction;
            case CONTROLLER_ACTION_AXIS_X:
                axisXControllerAction = controllerAction;
            case CONTROLLER_ACTION_AXIS_Y:
                axisYControllerAction = controllerAction;
            case CONTROLLER_ACTION_THUMB_L:
                thumbLControllerAction = controllerAction;
            case CONTROLLER_ACTION_AXIS_Z:
                axisZControllerAction = controllerAction;
            case CONTROLLER_ACTION_AXIS_RZ:
                axisRZControllerAction = controllerAction;
            case CONTROLLER_ACTION_THUMB_R:
                thumbRControllerAction = controllerAction;
            case CONTROLLER_ACTION_A:
                aControllerAction = controllerAction;
            case CONTROLLER_ACTION_B:
                bControllerAction = controllerAction;
            case CONTROLLER_ACTION_X:
                xControllerAction = controllerAction;
            case CONTROLLER_ACTION_Y:
                yControllerAction = controllerAction;
            case CONTROLLER_ACTION_L1:
                l1ControllerAction = controllerAction;
            case CONTROLLER_ACTION_L_TRIGGER:
                lTriggerControllerAction = controllerAction;
            case CONTROLLER_ACTION_R1:
                r1ControllerAction = controllerAction;
            case CONTROLLER_ACTION_R_TRIGGER:
                rTriggerControllerAction = controllerAction;
            case CONTROLLER_ACTION_START:
                startControllerAction = controllerAction;
            case CONTROLLER_ACTION_SELECT:
                selectTriggerControllerAction = controllerAction;
        }
    }

    //
    //
    //

    /**
     * Controller action class.
     */
    public static class ControllerAction {

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
    }
}
