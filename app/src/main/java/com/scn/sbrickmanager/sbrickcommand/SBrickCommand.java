package com.scn.sbrickmanager.sbrickcommand;

import com.scn.sbrickmanager.SBrick;

/**
 * SBrick specific Command subclass.
 */
public class SBrickCommand extends Command {

    //
    // Private members
    //

    private SBrick sbrick;
    private CommandMethod commandMethod = null;

    //
    // Constructor
    //

    SBrickCommand(SBrick sbrick, CommandMethod commandMethod) {

        if (sbrick == null)
            throw new IllegalArgumentException("sbrick is null.");

        if (commandMethod == null)
            throw new IllegalArgumentException("commandMethod is null.");

        this.sbrick = sbrick;
        this.commandMethod = commandMethod;
    }

    //
    // API
    //

    public SBrick getSbrick() { return sbrick; }
    public CommandMethod getCommandMethod() { return commandMethod; }

    //
    // Object overrides
    //

    @Override
    public String toString() {
        return "SBrickCommand, SBrick address: " + sbrick.getAddress();
    }
}
