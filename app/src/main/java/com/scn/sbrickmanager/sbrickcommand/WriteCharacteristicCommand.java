package com.scn.sbrickmanager.sbrickcommand;

import com.scn.sbrickmanager.SBrick;

/**
 * Command subclass for writing a characteristic.
 */
public abstract class WriteCharacteristicCommand extends SBrickCommand {

    //
    // Constructor
    //

    WriteCharacteristicCommand(String sbrickAddress, CommandMethod commandMethod) {
        super(sbrickAddress, commandMethod);
    }

    //
    // Object overrides
    //

    @Override
    public String toString() {
        return "WriteCharacteristicCommand, SBrick: " + getSbrickAddress();
    }
}
