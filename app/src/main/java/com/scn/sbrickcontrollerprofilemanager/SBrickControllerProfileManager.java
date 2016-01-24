package com.scn.sbrickcontrollerprofilemanager;

import java.io.IOException;
import java.util.Collection;

/**
 * SBrick controller profile manager interface.
 */
public interface SBrickControllerProfileManager {

    /**
     * Loads the SBrick controller profiles.
     */
    void loadProfiles() throws IOException;

    /**
     * Saves the SBrick controller profiles.
     */
    void saveProfiles() throws IOException;

    /**
     * Gets the previously loaded SBrick controller profiles.
     * @return Collection of SBrick controller profiles.
     */
    Collection<SBrickControllerProfile> getProfiles();

    /**
     * Gets the SBrick controller profile at the specified position.
     * @param position is the position of the profile to get.
     * @return The SBrick controller profile at the specified position.
     */
    SBrickControllerProfile getProfileAt(int position);

    /**
     * Gets the SBrick controller profile specified by its name.
     * @param name is the name of the profile to get.
     * @return The SBrick controller profile or null if not found.
     */
    SBrickControllerProfile getProfile(String name);

    /**
     * Adds a new blank SBrick controller profile.
     */
    SBrickControllerProfile addNewProfile();

    /**
     * Renames the profile.
     * @param profile to rename.
     * @param newName is the new name.
     * @return True if renameing was successful, false otherwise.
     */
    boolean renameProfile(SBrickControllerProfile profile, String newName);

    /**
     * Removes the SBrick controller profile.
     * @param profile is the profile to remove.
     */
    void removeProfile(SBrickControllerProfile profile);
}
