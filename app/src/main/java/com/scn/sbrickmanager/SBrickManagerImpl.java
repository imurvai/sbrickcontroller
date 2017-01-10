package com.scn.sbrickmanager;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.scn.sbrickmanager.sbrickcommand.Command;
import com.scn.sbrickmanager.sbrickcommand.CommandMethod;
import com.scn.sbrickmanager.sbrickcommand.CommandQueue;
import com.scn.sbrickmanager.sbrickcommand.QuitCommand;
import com.scn.sbrickmanager.sbrickcommand.SBrickCommand;
import com.scn.sbrickmanager.sbrickcommand.WriteCharacteristicCommand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * SBrickManager implementation.
 */
class SBrickManagerImpl implements SBrickManager {

    //
    // Private members
    //

    private static final String TAG = SBrickManagerImpl.class.getSimpleName();

    private static final String SBrickMapPreferencesName = "SBrickMapPrefs";

    private CommandQueue commandQueue = new CommandQueue(20);
    private Thread commandProcessThread = null;
    private Semaphore commandSemaphore = new Semaphore(1);
    private Object lockObject = new Object();

    private BluetoothAdapter bluetoothAdapter;

    private final Context context;
    private final Map<String, SBrickImpl> sbrickMap = new HashMap<>();

    private boolean isScanning = false;

    //
    // Constructor
    //

    SBrickManagerImpl(Context context) {

        Log.i(TAG, "SBrickManagerImpl...");

        this.context = context;

        if (!isBLESupported()) {
            Log.i(TAG, "BLE is not supported.");
            return;
        }

        Log.i(TAG, "  Retrieving the bluetooth adapter...");
        final BluetoothManager bluetoothManager = (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager == null)
            throw new RuntimeException("Can't find bluetooth manager.");

        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null)
            throw new RuntimeException("Can't find bluetooth adapter.");
    }

    //
    // SBrickManager overrides
    //

    @Override
    public boolean loadSBricks() {
        Log.i(TAG, "loadSBricks...");

        try {
            SharedPreferences prefs = context.getSharedPreferences(SBrickMapPreferencesName, Context.MODE_PRIVATE);

            sbrickMap.clear();

            HashMap<String, String> sbrickAddressAndNameMap = (HashMap<String, String>)prefs.getAll();
            for (String sbrickAddress : sbrickAddressAndNameMap.keySet()) {
                SBrick sbrick = createSBrick(sbrickAddress, sbrickAddressAndNameMap.get(sbrickAddress));
            }
        }
        catch (Exception ex) {
            Log.e(TAG, "Error during loading SBricks.", ex);
            return false;
        }

        return true;
    }

    @Override
    public boolean saveSBricks() {
        Log.i(TAG, "saveSBricks...");

        try {
            SharedPreferences prefs = context.getSharedPreferences(SBrickMapPreferencesName, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            // Clear first
            editor.clear();

            // Write the sbricks
            for (String sbrickAddress : sbrickMap.keySet()) {
                editor.putString(sbrickAddress, sbrickMap.get(sbrickAddress).getName());
            }

            editor.commit();
        }
        catch (Exception ex) {
            Log.e(TAG, "Error during saving SBricks.", ex);
            return false;
        }

        return true;
    }

    @Override
    public List<SBrick> getSBricks() {
        Log.i(TAG, "getSBricks...");

        return new ArrayList<SBrick>(sbrickMap.values());
    }

    @Override
    public List<String> getSBrickAddresses() {
        Log.i(TAG, "getSBrickAddresses...");

        List<String> sbrickAddresses = new ArrayList<>();
        for (SBrick sBrick : sbrickMap.values()) {
            sbrickAddresses.add(sBrick.getAddress());
        }

        return sbrickAddresses;
    }

    @Override
    public void forgetSBrick(String sbrickAddress) {
        Log.i(TAG, "forgetSBrick - " + sbrickAddress);

        if (sbrickMap.containsKey(sbrickAddress))
            sbrickMap.remove(sbrickAddress);
    }

    @Override
    public boolean startCommandProcessing() {

        synchronized (lockObject) {
            Log.i(TAG, "startCommandProcessing...");

            if (commandProcessThread != null) {
                Log.w(TAG, "  Command processing has already been started.");
                return false;
            }

            try {
                final Semaphore processThreadStartedSemaphore = new Semaphore(1);
                processThreadStartedSemaphore.acquire();

                commandProcessThread = new Thread() {

                    @Override
                    public void run() {

                        try {
                            commandQueue.clear();
                            processThreadStartedSemaphore.release();

                            while (true) {
                                try {
                                    // Wait for the GATT callback to release the semaphore.
                                    commandSemaphore.acquire();

                                    // Get the next command to process.
                                    Command command = commandQueue.take();

                                    synchronized (lockObject) {
                                        if (command instanceof QuitCommand) {

                                            // Quit command
                                            Log.i(TAG, "Quit command.");
                                            Log.i(TAG, "Empty the command queue...");
                                            commandQueue.clear();
                                            break;
                                        } else if (command instanceof SBrickCommand) {

                                            SBrickCommand sbrickCommand = (SBrickCommand) command;
                                            CommandMethod commandMethod = sbrickCommand.getCommandMethod();
                                            String sbrickAddress = sbrickCommand.getSbrickAddress();
                                            SBrickImpl sbrick = sbrickMap.get(sbrickAddress);

                                            // Execute the command method
                                            if (commandMethod != null && commandMethod.execute()) {

                                                // Set the last write command and its time on the SBrick
                                                if (command instanceof WriteCharacteristicCommand)
                                                    sbrick.setLastWriteCommand((WriteCharacteristicCommand) command);
                                            } else {
                                                Log.w(TAG, "Command method execution failed.");
                                                // Command wasn't sent, no need to wait for the GATT callback.
                                                commandSemaphore.release();
                                            }
                                        }
                                    }
                                } catch (Exception ex) {
                                    Log.e(TAG, "Command process thread has thrown an exception.", ex);
                                    commandSemaphore.release();
                                }
                            }

                            Log.i(TAG, "Command process thread exits...");
                        } catch (Exception ex) {
                            Log.e(TAG, "Command process thread has thrown an exception.", ex);
                        }

                        commandProcessThread = null;
                    }
                };

                commandProcessThread.start();

                // Waiting for the process thread to start.
                processThreadStartedSemaphore.acquire();

                return true;
            } catch (Exception ex) {
                Log.e(TAG, "Faild to start command processing thread.", ex);
                return false;
            }
        }
    }

    public void stopCommandProcessing() {

        synchronized (lockObject) {
            Log.i(TAG, "stopCommandProcessing...");

            if (commandProcessThread == null) {
                Log.w(TAG, "  Command processing has not been started.");
                return;
            }

            Command quitCommand = Command.newQuitCommand();
            commandQueue.clear();
            commandQueue.offer(quitCommand);

            // Just to be sure the semaphore doesn't block the thread.
            commandSemaphore.release();
        }
    }

    @Override
    public boolean isBLESupported() {
        Log.i(TAG, "isBleSupported...");

        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    @Override
    public boolean isBluetoothOn() {
        Log.i(TAG, "isBluetoothOn...");

        final BluetoothManager bluetoothManager = (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager == null)
            throw new RuntimeException("Can't find bluetooth manager.");

        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null)
            throw new RuntimeException("Can't find bluetooth adapter.");

        return bluetoothAdapter.isEnabled();
    }

    @Override
    public boolean startSBrickScan() {

        synchronized (getLockObject()) {
            Log.i(TAG, "startSBrickScan...");

            if (isScanning) {
                Log.w(TAG, "  Already scanning.");
                return false;
            }

            if (bluetoothAdapter.startLeScan(leScanCallback)) {
                isScanning = true;
                return true;
            }

            Log.w(TAG, "  Failed to start scanning.");
            return false;
        }
    }

    @Override
    public void stopSBrickScan() {

        synchronized (getLockObject()) {
            Log.i(TAG, "stopSBrickScan...");

            bluetoothAdapter.stopLeScan(leScanCallback);
            isScanning = false;
        }
    }

    @Override
    public SBrick getSBrick(String sbrickAddress) {

        synchronized (getLockObject()) {
            Log.i(TAG, "getSBrick - " + sbrickAddress);

            if (sbrickMap.containsKey(sbrickAddress)) {
                return sbrickMap.get(sbrickAddress);
            }

            Log.w(TAG, "  SBrick not found.");
            return null;
        }
    }

    //
    // Internal API
    //

    Object getLockObject() { return lockObject; }

    boolean sendCommand(Command command) {
        //Log.i(TAG, "sendCommand...");
        //Log.i(TAG, "  " + command);

        return commandQueue.offer(command);
    }

    void releaseCommandSemaphore() {
        commandSemaphore.release();
    }

    //
    // Private methods
    //

    private SBrick createSBrick(String sbrickAddress, String sbrickName) {

        synchronized (getLockObject()) {
            Log.i(TAG, "createSBrick - " + sbrickAddress);

            if (sbrickMap.containsKey(sbrickAddress)) {
                Log.i(TAG, "  SBrick is already in the map.");
                return sbrickMap.get(sbrickAddress);
            }

            BluetoothDevice sbrickDevice = bluetoothAdapter.getRemoteDevice(sbrickAddress);
            SBrickImpl sbrick = new SBrickImpl(context, this, sbrickDevice);
            sbrickMap.put(sbrickAddress, sbrick);
            return sbrick;
        }
    }

    private final BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {

            synchronized (getLockObject()) {
                Log.i(TAG, "onScanResult...");

                if (device != null && device.getName() != null) {
                    if (isSBrick(scanRecord)) {
                        if (!sbrickMap.containsKey(device.getAddress())) {
                            Log.i(TAG, "  Storing SBrick.");
                            Log.i(TAG, "    Device address    : " + device.getAddress());
                            Log.i(TAG, "    Device name       : " + device.getName());

                            SBrickImpl sbrick = new SBrickImpl(context, SBrickManagerImpl.this, device);
                            sbrickMap.put(device.getAddress(), sbrick);

                            Intent sendIntent = new Intent();
                            sendIntent.setAction(ACTION_FOUND_AN_SBRICK);
                            sendIntent.putExtra(EXTRA_SBRICK_NAME, device.getName());
                            sendIntent.putExtra(EXTRA_SBRICK_ADDRESS, device.getAddress());
                            LocalBroadcastManager.getInstance(context).sendBroadcast(sendIntent);
                        } else {
                            Log.i(TAG, "  Sbrick has already been discovered.");
                        }
                    } else {
                        Log.i(TAG, "  Device is not an SBrick.");
                        Log.i(TAG, "    Name: " + device.getName());
                    }
                } else {
                    Log.i(TAG, "  Device is null.");
                }
            }
        }

        private boolean isSBrick(byte[] scanRecord) {
            try {
                int index = 0;

                while (index < scanRecord.length) {
                    byte packetLength = scanRecord[index];
                    if (packetLength == 0) {
                        return false;
                    }

                    if (packetLength < 3) {
                        index += packetLength + 1;
                        continue;
                    }

                    int packetType = scanRecord[index + 1];
                    int manufacturer1 = scanRecord[index + 2];
                    int manufacturer2 = scanRecord[index + 3];

                    if (packetType == -1 && // Manufacturer specific packet
                        manufacturer1 == -104 && manufacturer2 == 1) {
                        // Vengit manufacturer ID 0x0198
                        return true;
                    }

                    index += packetLength + 1;
                }
            }
            catch (Exception ex) {
            }

            return false;
        }
    };
}
