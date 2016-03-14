package com.scn.sbrickmanager.sbrickcommand;

import com.scn.sbrickmanager.SBrick;
import com.scn.sbrickmanager.SBrickCharacteristicType;

/**
 * Command subclass for reading a characteristic.
 */
public class ReadCharacteristicCommand extends SBrickCommand {

    //
    // Private members
    //

    private SBrickCharacteristicType characteristicType;

    //
    // Constructor
    //

    ReadCharacteristicCommand(SBrick sbrick, CommandMethod commandMethod, SBrickCharacteristicType characteristicType) {
        super(sbrick, commandMethod);

        this.characteristicType = characteristicType;
    }

    //
    // API
    //

    SBrickCharacteristicType getCharacteristicType() { return characteristicType; }

    //
    // Object overrides
    //

    @Override
    public String toString() {
        return "ReadCharacteristicCommand, SBrick: " + getSbrick().getAddress() + ", characteristic type: " + characteristicType.toString();
    }
}
