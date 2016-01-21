package com.scn.sbrickmanager;

import java.util.Collection;

/**
 * SBrickManager interface.
 */
public interface SBrickManager {

    //
    // Public constants
    //

    // Broadcast actions
    String ACTION_START_SBRICK_SCAN = "com.scn.sbrickmanager.ACTION_START_SBRICK_SCAN";
    String ACTION_STOP_SBRICK_SCAN = "com.scn.sbrickmanager.ACTION_STOP_SBRICK_SCAN";
    String ACTION_FOUND_AN_SBRICK = "com.scn.sbrickmanager.ACTION_FOUND_AN_SBRICK";

    // Broadcast extras
    String EXTRA_SBRICK_NAME = "com.scn.sbrickmanager.EXTRA_SBRICK_NAME";
    String EXTRA_SBRICK_ADDRESS = "com.scn.sbrickmanager.EXTRA_SBRICK_ADDRESS";

    //
    // API
    //

    /**
     * Checks if the bluetooth low energy profile is supported on this device.
     * @return True if BLE is supported, false otherwise.
     */
    boolean isBLESupported();

    /**
     * Checks if the bluetooth is on.
     * @return True if the bluetooth is on, false otherwise.
     */
    boolean isBluetoothOn();

    /**
     * Loads the previously scanned SBrick device list.
     */
    void loadSBricks();

    /**
     * Saves the previously scanned SBrick device list.
     */
    void saveSBricks();

    /**
     * Starts the SBrick scanning.
     * @return True if the scanning has been started OK, false otherwise.
     */
    boolean startSBrickScan();

    /**
     * Stops the SBrick scanning.
     */
    void stopSBrickScan();

    /**
     * Gets the SBricks have been scanned previously.
     * @return Collection of the SBricks.
     */
    Collection<SBrick> getSBricks();

    /**
     * Gets (or creates) the SBrick specified by its address.
     * @param sbrickAddress The SBrick address.
     * @return The SBrick.
     */
    SBrick getSBrick(String sbrickAddress);

    /**
     * Forgets the SBrick specified by its address.
     * @param sbrickAddress The SBrick address.
     */
    void forgetSBrick(String sbrickAddress);
}
