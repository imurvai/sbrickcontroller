package com.scn.sbrickcontroller;

/**
 * Constants.
 */
public final class Constants {

    //
    // Constructor
    //

    private Constants() {}

    //
    // Intent extra keys
    //

    public static final String EXTRA_SBRICK_ADDRESS = "EXTRA_SBRICK_ADDRESS";
    public static final String EXTRA_CONTROLLER_PROFILE = "EXTRA_CONTROLLER_PROFILE";
    public static final String EXTRA_CONTROLLER_PROFILES = "EXTRA_CONTROLLER_PROFILES";
    public static final String EXTRA_CONTROLLER_PROFILE_NAME = "EXTRA_CONTROLLER_PROFILE_NAME";
    public static final String EXTRA_CONTROLLER_ACTION_ID = "EXTRA_CONTROLLER_ACTION_ID";
    public static final String EXTRA_CONTROLLER_ACTION = "EXTRA_CONTROLLER_ACTION";
    public static final String EXTRA_ORIGINAL_CONTROLLER_ACTION = "EXTRA_ORIGINAL_CONTROLLER_ACTION";

    //
    // Activity request codes
    //

    public static final int REQUEST_ENABLE_BLUETOOTH = 0x1000;
    public static final int REQUEST_EDIT_CONTROLLER_ACTION = 0x1001;
}
