package com.scn.sbrickmanager.sbrickcommand;

import com.scn.sbrickmanager.SBrick;

/**
 * Command subclass for connecting to the SBrick.
 */
public class ConnectCommand extends SBrickCommand {

    //
    // Constructor
    //

    ConnectCommand(String sbrickAddress, CommandMethod commandMethod) {
        super(sbrickAddress, commandMethod);
    }

    //
    // Object overrides
    //

    @Override
    public String toString() {
        return "ConnectCommand, SBrick: " + getSbrickAddress();
    }
}
