package com.scn.sbrickmanager.sbrickcommand;

import com.scn.sbrickmanager.SBrick;

/**
 * Command subclass for quit.
 */
public class QuitCommand extends Command {

    //
    // Constructor
    //

    QuitCommand(CommandMethod commandMethod) {
        super(commandMethod);
    }

    //
    // Object overrides
    //

    @Override
    public String toString() {
        return "QuitCommand";
    }
}
