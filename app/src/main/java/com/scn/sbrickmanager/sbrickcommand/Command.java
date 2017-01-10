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

    public static ConnectCommand newConnectCommand(String sbrickAddress, CommandMethod commandMethod) {
        return new ConnectCommand(sbrickAddress, commandMethod);
    }

    public static DiscoverServicesCommand newDiscoverServicesCommand(String sbrickAddress, CommandMethod commandMethod) {
        return new DiscoverServicesCommand(sbrickAddress, commandMethod);
    }

    public static ReadCharacteristicCommand newReadCharacteristicCommand(String sbrickAddress, CommandMethod commandMethod, SBrickCharacteristicType characteristicType) {
        return new ReadCharacteristicCommand(sbrickAddress, commandMethod, characteristicType);
    }

    public static WriteRemoteControlCommand newWriteRemoteControlCommand(String sbrickAddress, CommandMethod commandMethod, int channel, int value) {
        return new WriteRemoteControlCommand(sbrickAddress, commandMethod, channel, value);
    }

    public static WriteQuickDriveCommand newWriteQuickDriveCommand(String sbrickAddress, CommandMethod commandMethod, int v0, int v1, int v2, int v3) {
        return new WriteQuickDriveCommand(sbrickAddress, commandMethod, v0, v1, v2, v3);
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
