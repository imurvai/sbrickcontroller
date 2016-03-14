package com.scn.sbrickmanager.sbrickcommand;

import com.scn.sbrickmanager.SBrick;

/**
 * Command subclass for connecting to the SBrick.
 */
public class ConnectCommand extends SBrickCommand {

    //
    // Constructor
    //

    ConnectCommand(SBrick sbrick, CommandMethod commandMethod) {
        super(sbrick, commandMethod);
    }

    //
    // Object overrides
    //

    @Override
    public String toString() {
        return "ConnectCommand, SBrick: " + getSbrick().getAddress();
    }
}
