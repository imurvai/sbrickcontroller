package com.scn.sbrickcontrollerprofilemanager;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * SBrick controller profile manager interface.
 */
public interface SBrickControllerProfileManager {

    /**
     * Loads the SBrick controller profiles.
     */
    boolean loadProfiles();

    /**
     * Saves the SBrick controller profiles.
     */
    boolean saveProfiles();

    /**
     * Gets the SBrick controller profiles.
     * @return List of SBrick controller profiles.
     */
    List<SBrickControllerProfile> getProfiles();

    /**
     * Gets the SBrick controller profile at the specified position.
     * @param position is the position of the profile to get.
     * @return The SBrick controller profile at the specified position.
     */
    SBrickControllerProfile getProfileAt(int position);

    /**
     * Adds a new SBrick controller profile.
     * @param profile is the profile to add.
     */
    void addProfile(SBrickControllerProfile profile);

    /**
     * Updates the profile at the given position with the given profile.
     * @param position is the position where to update the profile.
     * @param profile is the profile to update with.
     */
    void updateProfileAt(int position, SBrickControllerProfile profile);

    /**
     * Removes the SBrick controller profile.
     * @param profile is the profile to remove.
     */
    void removeProfile(SBrickControllerProfile profile);
}
