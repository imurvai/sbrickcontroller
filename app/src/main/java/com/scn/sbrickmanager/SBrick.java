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
     * @return The display name of the SBrick.
     */
    String getDisplayName();

    /**
     * Sets the display name of the SBrick.
     * @param displayName is the name to display.
     */
    void setDisplayName(String displayName);

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
     * Sends the command to the SBrick
     * @param channel is the channel (0-3).
     * @param value is the value (0-255).
     * @param invert is true if to send the invert value to the channel.
     * @return True if the command has been sent ok, false otherwise.
     */
    boolean sendCommand(int channel, int value, boolean invert);

    /**
     * Sends the command tot the SBrick with the max value.
     * @param channel is the channel (0-3)
     * @param invert is true if to send the invert value to the channel.
     * @return True if the command has been sent ok, false otherwise.
     */
    boolean sendCommand(int channel, boolean invert);
}
