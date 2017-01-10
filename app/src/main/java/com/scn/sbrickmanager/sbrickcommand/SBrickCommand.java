package com.scn.sbrickmanager.sbrickcommand;

import com.scn.sbrickmanager.SBrick;

/**
 * SBrick specific Command subclass.
 */
public class SBrickCommand extends Command {

    //
    // Private members
    //

    private String sbrickAddress;
    private CommandMethod commandMethod = null;

    //
    // Constructor
    //

    SBrickCommand(String sbrickAddress, CommandMethod commandMethod) {

        if (sbrickAddress == null)
            throw new IllegalArgumentException("sbrickAddress is null.");

        if (commandMethod == null)
            throw new IllegalArgumentException("commandMethod is null.");

        this.sbrickAddress = sbrickAddress;
        this.commandMethod = commandMethod;
    }

    //
    // API
    //

    public String getSbrickAddress() { return sbrickAddress; }
    public CommandMethod getCommandMethod() { return commandMethod; }

    //
    // Object overrides
    //

    @Override
    public String toString() {
        return "SBrickCommand, SBrick address: " + sbrickAddress;
    }
}
