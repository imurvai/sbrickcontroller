package com.scn.sbrickmanager.sbrickcommand;

import com.scn.sbrickmanager.SBrick;

/**
 * WriteCharacteristicCommand subclass for writing to quick drive characteristic.
 */
public class WriteQuickDriveCommand extends WriteCharacteristicCommand {

    //
    // Private members
    //

    private int v0, v1, v2, v3;

    //
    // Constructor
    //

    WriteQuickDriveCommand(String sbrickAddress, CommandMethod commandMethod, int v0, int v1, int v2, int v3) {
        super(sbrickAddress, commandMethod);

        this.v0 = v0;
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
    }

    //
    // API
    //

    public int getV0() { return v0; }
    public int getV1() { return v1; }
    public int getV2() { return v2; }
    public int getV3() { return v3; }

    //
    // Object overrides
    //

    @Override
    public String toString() {
        return "WriteQuickDriveCommand, SBrick address: " + getSbrickAddress() + " - v0: " + v0 + ", v1 = " + v1 + ", v2: " + v2 + ", v3: " + v3;
    }
}
