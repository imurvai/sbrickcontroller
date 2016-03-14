package com.scn.sbrickmanager.sbrickcommand;

import com.scn.sbrickmanager.SBrick;

/**
 * WriteCharacteristicCommand subclass for writing the remote control characteristic.
 */
public class WriteRemoteControlCommand extends WriteCharacteristicCommand {

    //
    // Private members
    //

    private int channel;
    private int value;

    //
    // Constructor
    //

    WriteRemoteControlCommand(SBrick sbrick, CommandMethod commandMethod, int channel, int value) {
        super(sbrick, commandMethod);

        this.channel = channel;
        this.value = value;
    }

    //
    // API
    //

    public int getChannel() { return channel; }
    public int getValue() { return value; }

    //
    // Object overrides
    //

    @Override
    public String toString() {
        return "WriteRemoteControlCommand, SBrick address: " + getSbrick().getAddress() + " - channel: " + channel + ", value: " + value;
    }
}
