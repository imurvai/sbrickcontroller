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

    public static final String EXTRA_REQUEST_CODE = "EXTRA_REQUEST_CODE";
    public static final String EXTRA_SBRICK_ADDRESS = "EXTRA_SBRICK_ADDRESS";
    public static final String EXTRA_CONTROLLER_PROFILE = "EXTRA_CONTROLLER_PROFILE";
    public static final String EXTRA_CONTROLLER_PROFILE_INDEX = "EXTRA_CONTROLLER_PROFILE_INDEX";
    public static final String EXTRA_CONTROLLER_ACTION_ID = "EXTRA_CONTROLLER_ACTION_ID";
    public static final String EXTRA_CONTROLLER_ACTION = "EXTRA_CONTROLLER_ACTION";

    //
    // Activity request codes
    //

    public static final int REQUEST_ENABLE_BLUETOOTH = 0x1000;
    public static final int REQUEST_EDIT_CONTROLLER_PROFILE = 0x1001;
    public static final int REQUEST_NEW_CONTROLLER_PROFILE = 0x1002;
    public static final int REQUEST_EDIT_CONTROLLER_ACTION = 0x1003;
    public static final int REQUEST_NEW_CONTROLLER_ACTION = 0x1004;
}
