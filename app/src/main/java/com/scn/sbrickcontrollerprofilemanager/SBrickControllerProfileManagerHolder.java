package com.scn.sbrickcontrollerprofilemanager;

/**
 * SBrick controller profile manager holder
 */
public final class SBrickControllerProfileManagerHolder {

    //
    //
    //

    private SBrickControllerProfileManagerHolder() {
    }

    //
    // API
    //

    /**
     * Gets the SBRick controller profile manager instance.
     * @return The SBrick controller profile manager.
     */
    public static SBrickControllerProfileManager getManager() {
        return SBrickControllerProfileManagerImpl.getInstance();
    }
}
