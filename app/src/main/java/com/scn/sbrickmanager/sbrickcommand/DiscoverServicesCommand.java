package com.scn.sbrickmanager.sbrickcommand;

import com.scn.sbrickmanager.SBrick;

/**
 * SbrickCommand subclass for discovering GATT services.
 */
public class DiscoverServicesCommand extends SBrickCommand {

    //
    // Constructor
    //

    public DiscoverServicesCommand(SBrick sbrick, CommandMethod commandMethod) {
        super(sbrick, commandMethod);
    }

    //
    // Object overrides
    //

    @Override
    public String toString() {
        return "DiscoverServicesCommand, SBrick: " + getSbrick().getAddress();
    }
}
