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

    //
    // Constructor
    //

    SBrickCommand(SBrick sbrick, CommandMethod commandMethod) {
        super(commandMethod);

        if (sbrick == null)
            throw new IllegalArgumentException("sbrick is null.");

        this.sbrick = sbrick;
    }

    //
    // API
    //

    public SBrick getSbrick() { return sbrick; }

    //
    // Object overrides
    //

    @Override
    public String toString() {
        return "SBrickCommand, SBrick address: " + sbrick.getAddress();
    }
}
