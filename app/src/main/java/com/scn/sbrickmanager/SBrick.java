package com.scn.sbrickmanager;

public interface SBrick {

    // Broadcast actions
    String ACTION_SBRICK_CONNECTED = "com.scn.sbrick.ACTION_SBRICK_CONNECTED";
    String ACTION_SBRICK_DISCONNECTED = "com.scn.sbrick.ACTION_SBRICK_DISCONNECTED";
    String ACTION_SBRICK_CHARACTERISTIC_READ = "com.scn.sbrick.ACTION_SBRICK_CHARACTERISTIC_READ";

    // Broadcast extras
    String EXTRA_SBRICK_NAME = "com.scn.sbrick.EXTRA_SBRICK_NAME";
    String EXTRA_SBRICK_ADDRESS = "com.scn.sbrick.EXTRA_SBRICK_ADDRESS";
    String EXTRA_CHARACTERISTICS = "com.scn.sbrick.EXTRA_CHARACTERISTICS";

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
     * Starts reading all relevant characteristics of the SBrick.
     * When read the appropriate broadcast message is sent.
     * @return True if the reading has been started ok, false otherwise.
     */
    boolean getCharacteristicsAsync();

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
