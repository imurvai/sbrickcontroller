package com.scn.sbrickmanager;

public interface SBrick {

    /**
     * SBrick characteristic types.
     */
    enum SBrickCharacteristicType {
        DeviceName,
        Appearance,
        ModelNumber,
        FirmwareRevision,
        HardwareRevision,
        SoftwareRevision,
        ManufacturerName,
        Unknown
    };

    // Broadcast actions
    String ACTION_SBRICK_CONNECTED = "com.scn.sbrick.ACTION_SBRICK_CONNECTED";
    String ACTION_SBRICK_DISCONNECTED = "com.scn.sbrick.ACTION_SBRICK_DISCONNECTED";
    String ACTION_SBRICK_CHARACTERISTIC_READ = "com.scn.sbrick.ACTION_SBRICK_CHARACTERISTIC_READ";

    // Broadcast extras
    String EXTRA_SBRICK_ADDRESS = "com.scn.sbrick.EXTRA_SBRICK_ADDRESS";
    String EXTRA_CHARACTERISTIC_TYPE = "com.scn.sbrick.EXTRA_CHARACTERISTIC_TYPE";
    String EXTRA_CHARACTERISTIC_VALUE = "com.scn.sbrick.EXTRA_CHARACTERISTIC_VALUE";

    /**
     * @return The address of the SBrick.
     */
    String getAddress();

    /**
     * @return The Name of the SBrick.
     */
    String getName();

    /**
     * Sets the name of the SBrick.
     * @param name is the name.
     */
    void setName(String name);

    /**
     * Starts connecting to the SBrick.
     * When connected the appropriate broadcast message is sent.
     * @return True if the connection has been started, false otherwise.
     */
    boolean connect();

    /**
     * Disconnects from the SBrick.
     */
    void disconnect();

    /**
     * Checks if the SBrick is connected.
     * @return true if connected, false otherwise.
     */
    boolean isConnected();

    /**
     * Starts reading the specified characteristic.
     * A broadcast message will be sent with the value.
     * @param characteristicType identifies the characteristic to read.
     * @return True if the reading started ok, false otherwise.
     */
    boolean readCharacteristic(SBrickCharacteristicType characteristicType);

    /**
     * Sends command to the specified channel.
     * @param channel - channel (0-3)
     * @param value - value (-255-22)
     * @return True if the command has been sent ok, false otherwise.
     */
    boolean sendCommand(int channel, int value);

    /**
     * Sends command to all 4 channels.
     * Valid values are -255 to 255.
     * @param v1 - value for channel 1.
     * @param v2 - value for channel 2.
     * @param v3 - value for channel 3.
     * @param v4 - value for channel 4.
     * @return True if the command has been sent ok, false otherwise.
     */
    boolean sendCommand(int v1, int v2, int v3, int v4);
}
