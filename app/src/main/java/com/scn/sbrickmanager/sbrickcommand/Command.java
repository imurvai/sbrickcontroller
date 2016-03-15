package com.scn.sbrickmanager.sbrickcommand;

import com.scn.sbrickmanager.SBrick;
import com.scn.sbrickmanager.SBrickCharacteristicType;

/**
 * The Command abstract base class.
 */
public abstract class Command {

    //
    // API
    //

    public static ConnectCommand newConnectCommand(SBrick sbrick, CommandMethod commandMethod) {
        return new ConnectCommand(sbrick, commandMethod);
    }

    public static DiscoverServicesCommand newDiscoverServicesCommand(SBrick sbrick, CommandMethod commandMethod) {
        return new DiscoverServicesCommand(sbrick, commandMethod);
    }

    public static ReadCharacteristicCommand newReadCharacteristicCommand(SBrick sbrick, CommandMethod commandMethod, SBrickCharacteristicType characteristicType) {
        return new ReadCharacteristicCommand(sbrick, commandMethod, characteristicType);
    }

    public static WriteRemoteControlCommand newWriteRemoteControlCommand(SBrick sbrick, CommandMethod commandMethod, int channel, int value) {
        return new WriteRemoteControlCommand(sbrick, commandMethod, channel, value);
    }

    public static WriteQuickDriveCommand newWriteQuickDriveCommand(SBrick sbrick, CommandMethod commandMethod, int v0, int v1, int v2, int v3) {
        return new WriteQuickDriveCommand(sbrick, commandMethod, v0, v1, v2, v3);
    }

    public static QuitCommand newQuitCommand() {
        return new QuitCommand();
    }

    //
    // Object overrides
    //

    @Override
    public String toString() {
        return "Command";
    }
}
