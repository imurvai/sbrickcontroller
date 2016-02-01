package com.scn.sbrickmanager;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * SBrick real implementation.
 */
class SBrickImpl extends SBrickBase {

    //
    // Private members
    //

    private static final String TAG = SBrickImpl.class.getSimpleName();

    // Service UUIDs
    private static final String SERVICE_PARTIAL_UUID_GENERIC_GAP = "1800";
    private static final String SERVICE_PARTIAL_UUID_DEVICE_INFORMATION = "180a";
    private static final String SERVICE_UUID_REMOTE_CONTROL = "4dc591b0-857c-41de-b5f1-15abda665b0c";

    // Characteristic UUIDs
    private static final String CHARACTERISTIC_PARTIAL_UUID_DEVICE_NAME = "2a00";
    private static final String CHARACTERISTIC_PARTIAL_UUID_APPEARANCE = "2a01";
    private static final String CHARACTERISTIC_PARTIAL_UUID_MODEL_NUMBER = "2a24";
    private static final String CHARACTERISTIC_PARTIAL_UUID_FIRMWARE_REVISION = "2a26";
    private static final String CHARACTERISTIC_PARTIAL_UUID_HARDWARE_REVISION = "2a27";
    private static final String CHARACTERISTIC_PARTIAL_UUID_SOFTWARE_REVISION = "2a28";
    private static final String CHARACTERISTIC_PARTIAL_UUID_MANUFACTURER_NAME = "2a29";
    private static final String CHARACTERISTIC_UUID_REMOTE_CONTROL = "2b8cbcc-0e25-4bda-8790-a15f53e6010f";
    private static final String CHARACTERISTIC_UUID_QUICK_DRIVE = "489a6ae0-c1ab-4c9c-bdb2-11d373c1b7fb";

    private final BluetoothDevice bluetoothDevice;
    private BluetoothGatt bluetoothGatt = null;
    private boolean isConnected = false;

    private BluetoothGattCharacteristic remoteControlCharacteristic = null;
    private BluetoothGattCharacteristic quickDriveCharacteristic = null;

    private LinkedBlockingDeque<Command> commandQueue = new LinkedBlockingDeque<>(100);
    private Semaphore commandSendingSemaphore = new Semaphore(1);
    private Timer watchdogTimer = null;

    private int[] channelValues = new int[] { 0, 0, 0, 0 };

    //
    // API
    //

    SBrickImpl(Context context, BluetoothDevice bluetoothDevice) {
        super(context);

        Log.i(TAG, "SBrickImpl...");
        Log.i(TAG, "  address: " + bluetoothDevice.getAddress());
        Log.i(TAG, "  name:    " + bluetoothDevice.getName());

        this.bluetoothDevice = bluetoothDevice;
        setName(bluetoothDevice.getName());
    }

    @Override
    public String getAddress() {
        return bluetoothDevice.getAddress();
    }

    @Override
    public synchronized boolean connect() {
        Log.i(TAG, "connect - " + getAddress());

        if (isConnected) {
            Log.i(TAG, "  Already connected.");
            return false;
        }

        bluetoothGatt = bluetoothDevice.connectGatt(context, true, gattCallback);
        if (bluetoothGatt == null)
            throw new RuntimeException("Can't create GATT - " + getAddress());

        return true;
    }

    @Override
    public synchronized void disconnect() {
        Log.i(TAG, "disconnect - " + getAddress());

        if (!isConnected) {
            Log.i(TAG, "  Already disconnected.");

            if (bluetoothGatt != null)
                bluetoothGatt.close();

            return;
        }

        stopCommandProcessing();
        bluetoothGatt.disconnect();
        bluetoothGatt.close();
        isConnected = false;
        remoteControlCharacteristic = null;
        quickDriveCharacteristic = null;
    }

    @Override
    public boolean isConnected() {
        return isConnected;
    }

    @Override
    public boolean readCharacteristic(SBrickCharacteristicType characteristicType) {
        Log.i(TAG, "readCharacteristic - " + getAddress());

        if (!isConnected) {
            Log.w(TAG, "  Not connected.");
            return false;
        }

        Command command = Command.initReadCharacteristic(characteristicType);
        return commandQueue.offer(command);
    }

    @Override
    public synchronized boolean sendCommand(int channel, int value) {
        Log.i(TAG, "sendCommand - " + getAddress());
        Log.i(TAG, "  channel: " + channel);
        Log.i(TAG, "  value: " + value);

        if (!isConnected) {
            Log.i(TAG, "  Not connected.");
            return false;
        }

        byte invert = (byte)((0 <= value) ? 0 : 1);
        byte byteValue = (byte)(Math.min(255, Math.abs(value)));
        Command command = Command.initRemoteControl((byte)channel, invert, byteValue);
        return commandQueue.offer(command);
    }

    @Override
    public synchronized boolean sendCommand(int v1, int v2, int v3, int v4) {
        Log.i(TAG, "sendCommand - " + getAddress());
        Log.i(TAG, "  value1: " + v1);
        Log.i(TAG, "  value2: " + v2);
        Log.i(TAG, "  value3: " + v3);
        Log.i(TAG, "  value4: " + v4);

        if (!isConnected) {
            Log.i(TAG, "  Not connected.");
            return false;
        }

        byte bv1 = (byte)((Math.min(255, Math.abs(v1)) & 0xfe) | (0 <= v1 ? 0 : 1));
        byte bv2 = (byte)((Math.min(255, Math.abs(v2)) & 0xfe) | (0 <= v2 ? 0 : 1));
        byte bv3 = (byte)((Math.min(255, Math.abs(v3)) & 0xfe) | (0 <= v3 ? 0 : 1));
        byte bv4 = (byte)((Math.min(255, Math.abs(v4)) & 0xfe) | (0 <= v4 ? 0 : 1));

        Command command = Command.initQuickDrive(bv1, bv2, bv3, bv4);
        return commandQueue.offer(command);
    }

    //
    // Private classes and methods
    //

    private void startCommandProcessing() {
        Log.i(TAG, "startCommandProcessing...");

        Thread commandProcessThread = new Thread() {

            @Override
            public void run() {

                try {

                    while (true) {
                        // Wait for the GATT callback to release the semaphore.
                        commandSendingSemaphore.acquire();

                        // Get the next command to process.
                        Command command = commandQueue.take();

                        if (command.getCommandType() == Command.CommandType.QUIT) {
                            Log.i(TAG, "Command process thread quits.");
                            break;
                        }

                        if (!processCommand(command)) {
                            // Command wasn't sent, no need to wait for the GATT callback.
                            commandSendingSemaphore.release();
                        }
                    }

                }
                catch (Exception ex) {
                    Log.e(TAG, "Command process thread has thrown an exception.");
                }
            }
        };

        commandProcessThread.start();
    }

    private void stopCommandProcessing() {
        Log.i(TAG, "stopCommandProcessing...");

        Command quitCommand = Command.initQuit();
        if (!commandQueue.offerFirst(quitCommand)) {
            Log.e(TAG, "  Could not send quit command to queue.");
            return;
        }

        // Just to be sure the semaphore doesn't block the thread.
        commandSendingSemaphore.release();
    }

    private boolean processCommand(Command command) {
        Log.i(TAG, "processCommand...");

        try {
            switch (command.getCommandType()) {

                case READ_CHARACTERISTIC:
                    Log.i(TAG, "  READ_CAHRACTERISTIC");

                    SBrickCharacteristicType characteristicType = (SBrickCharacteristicType) command.getCommandParameter();
                    String serviceUUID = "";
                    String characteristicUUID = "";

                    switch (characteristicType) {
                        case DeviceName:
                            serviceUUID = SERVICE_PARTIAL_UUID_GENERIC_GAP;
                            characteristicUUID = CHARACTERISTIC_PARTIAL_UUID_DEVICE_NAME;
                            break;

                        case Appearance:
                            serviceUUID = SERVICE_PARTIAL_UUID_GENERIC_GAP;
                            characteristicUUID = CHARACTERISTIC_PARTIAL_UUID_APPEARANCE;
                            break;

                        case ModelNumber:
                            serviceUUID = SERVICE_PARTIAL_UUID_DEVICE_INFORMATION;
                            characteristicUUID = CHARACTERISTIC_PARTIAL_UUID_MODEL_NUMBER;
                            break;

                        case FirmwareRevision:
                            serviceUUID = SERVICE_PARTIAL_UUID_DEVICE_INFORMATION;
                            characteristicUUID = CHARACTERISTIC_PARTIAL_UUID_FIRMWARE_REVISION;
                            break;

                        case HardwareRevision:
                            serviceUUID = SERVICE_PARTIAL_UUID_DEVICE_INFORMATION;
                            characteristicUUID = CHARACTERISTIC_PARTIAL_UUID_HARDWARE_REVISION;
                            break;

                        case SoftwareRevision:
                            serviceUUID = SERVICE_PARTIAL_UUID_DEVICE_INFORMATION;
                            characteristicUUID = CHARACTERISTIC_PARTIAL_UUID_SOFTWARE_REVISION;
                            break;

                        case ManufacturerName:
                            serviceUUID = SERVICE_PARTIAL_UUID_DEVICE_INFORMATION;
                            characteristicUUID = CHARACTERISTIC_PARTIAL_UUID_MANUFACTURER_NAME;
                            break;
                    }

                    BluetoothGattCharacteristic gattCharacteristic = getGattCharacteristic(bluetoothGatt, serviceUUID, characteristicUUID);
                    if (gattCharacteristic == null)
                        return false;

                    return bluetoothGatt.readCharacteristic(gattCharacteristic);

                case SEND_REMOTE_CONTROL:
                    Log.i(TAG, "  SEND_REMOTE_CONTROL");

                    byte[] commandBuffer = (byte[]) command.getCommandParameter();
                    return remoteControlCharacteristic.setValue(commandBuffer) &&
                           bluetoothGatt.writeCharacteristic(remoteControlCharacteristic);

                case SEND_QUICK_DRIVE:
                    Log.i(TAG, "  SEND_QUICK_DRIVE");

                    byte[] commandBuffer2 = (byte[]) command.getCommandParameter();
                    return quickDriveCharacteristic.setValue(commandBuffer2) &&
                           bluetoothGatt.writeCharacteristic(quickDriveCharacteristic);

                case RESET_WATCHDOG:
                    Log.i(TAG, "  RESET_WATCHDOG");

                    byte[] commandBuffer3 = (byte[]) command.getCommandParameter();
                    return remoteControlCharacteristic.setValue(commandBuffer3) &&
                           bluetoothGatt.writeCharacteristic(remoteControlCharacteristic);

                case QUIT:
                    Log.i(TAG, "  QUIT");
                    return true;
            }
        }
        catch (Exception ex) {
            Log.e(TAG, "Error duing processing the next command.");
        }

        return false;
    }

    private synchronized void startWatchdogTimer() {
        Log.i(TAG, "startWatchdogTimer...");

        if (watchdogTimer != null) {
            Log.w(TAG, "  Watchdog timer already started.");
            return;
        }

        watchdogTimer = new Timer();
        watchdogTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Log.i(TAG, "watchdogScheduler.run...");
                Command command = Command.initWatchdog();
                commandQueue.offerFirst(command);
            }
        }, 0, 200);
    }

    private synchronized void stopWatchdogTimer() {
        Log.i(TAG, "stopWatchdogTimer...");

        if (watchdogTimer == null) {
            Log.i(TAG, " Warchdog timer already stopped.");
            return;
        }

        watchdogTimer.cancel();
        watchdogTimer = null;
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i(TAG, "BluetoothGattCallback.onConnectionStateChange - " + getAddress());

            switch (newState) {
                case BluetoothProfile.STATE_CONNECTING:
                    Log.i(TAG, "  STATE_CONNECTING");
                    break;

                case BluetoothProfile.STATE_CONNECTED:
                    Log.i(TAG, "  STATE_CONNECTED");

                    bluetoothGatt = gatt;

                    Log.i(TAG, " Discovering GATT services...");
                    if (!gatt.discoverServices()) {
                        Log.w(TAG, "  Failed to start discovering GATT services.");
                        disconnect();
                    }
                    break;

                case BluetoothProfile.STATE_DISCONNECTING:
                    Log.i(TAG, "  STATE_DISCONNECTING");
                    break;

                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.i(TAG, "  STATE_DISCONNECTED");

                    stopCommandProcessing();
                    isConnected = false;
                    remoteControlCharacteristic = null;
                    quickDriveCharacteristic = null;

                    sendLocalBroadcast(ACTION_SBRICK_DISCONNECTED);
                    break;

                default:
                    Log.w(TAG, "  Unknown state.");
                    break;
            }

            super.onConnectionStateChange(gatt, status, newState);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.i(TAG, "BluetoothGattCallback.onServicesDiscovered - " + getAddress());

            switch (status) {

                case BluetoothGatt.GATT_SUCCESS:
                    Log.i(TAG, "  GATT_SUCCESS");
                    logServices(gatt);

                    remoteControlCharacteristic = getGattCharacteristic(gatt, SERVICE_UUID_REMOTE_CONTROL, CHARACTERISTIC_UUID_REMOTE_CONTROL);
                    quickDriveCharacteristic = getGattCharacteristic(gatt, SERVICE_UUID_REMOTE_CONTROL, CHARACTERISTIC_UUID_QUICK_DRIVE);
                    isConnected = true;
                    startCommandProcessing();

                    sendLocalBroadcast(ACTION_SBRICK_CONNECTED);
                    break;

                case BluetoothGatt.GATT_FAILURE:
                    Log.w(TAG, "  GATT_FAILURE");
                    disconnect();
                    break;

                default:
                    Log.w(TAG, "  GATT status: " + status);
                    break;
            }

            super.onServicesDiscovered(gatt, status);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.i(TAG, "BluetoothGattCallback.onCharacteristicRead - " + getAddress());

            switch (status) {
                case BluetoothGatt.GATT_SUCCESS:
                    Log.i(TAG, "  GATT_SUCCESS");

                    String value = "N/A";
                    SBrickCharacteristicType characteristicType = getSBrickCharacteristicType(characteristic);

                    switch (characteristicType) {
                        case DeviceName:
                        case ModelNumber:
                        case FirmwareRevision:
                        case HardwareRevision:
                        case SoftwareRevision:
                        case ManufacturerName:
                            value = characteristic.getStringValue(0);
                            break;

                        case Appearance:
                            value = String.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0));
                            break;

                        default:
                            Log.i(TAG, "  Unknown characteristic.");
                            break;
                    }

                    Intent intent = new Intent();
                    intent.setAction(ACTION_SBRICK_CHARACTERISTIC_READ);
                    intent.putExtra(EXTRA_SBRICK_ADDRESS, getAddress());
                    intent.putExtra(EXTRA_CHARACTERISTIC_TYPE, characteristicType.name());
                    intent.putExtra(EXTRA_CHARACTERISTIC_VALUE, value);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                    break;

                default:
                    Log.w(TAG, "  GATT status: " + status);
                    break;
            }

            // Release the semaphore to let the command process thread to proceed.
            commandSendingSemaphore.release();

            super.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.i(TAG, "onCharacteristicWrite...");

            // Release the semaphore to let the command process thread to proceed.
            commandSendingSemaphore.release();
        }
    };

    private SBrickCharacteristicType getSBrickCharacteristicType(BluetoothGattCharacteristic characteristic) {
        String uuid = characteristic.getUuid().toString();
        final int PARTIAL_UUID_POSITION = 4;
        if (uuid.indexOf(CHARACTERISTIC_PARTIAL_UUID_APPEARANCE) == PARTIAL_UUID_POSITION)
            return SBrickCharacteristicType.Appearance;
        else if (uuid.indexOf(CHARACTERISTIC_PARTIAL_UUID_DEVICE_NAME) == PARTIAL_UUID_POSITION)
            return SBrickCharacteristicType.DeviceName;
        else if (uuid.indexOf(CHARACTERISTIC_PARTIAL_UUID_FIRMWARE_REVISION) == PARTIAL_UUID_POSITION)
            return SBrickCharacteristicType.FirmwareRevision;
        else if (uuid.indexOf(CHARACTERISTIC_PARTIAL_UUID_HARDWARE_REVISION) == PARTIAL_UUID_POSITION)
            return SBrickCharacteristicType.HardwareRevision;
        else if (uuid.indexOf(CHARACTERISTIC_PARTIAL_UUID_SOFTWARE_REVISION) == PARTIAL_UUID_POSITION)
            return SBrickCharacteristicType.SoftwareRevision;
        else if (uuid.indexOf(CHARACTERISTIC_PARTIAL_UUID_MODEL_NUMBER) == PARTIAL_UUID_POSITION)
            return SBrickCharacteristicType.ModelNumber;
        else if (uuid.indexOf(CHARACTERISTIC_PARTIAL_UUID_MANUFACTURER_NAME) == PARTIAL_UUID_POSITION)
            return SBrickCharacteristicType.ManufacturerName;
        return SBrickCharacteristicType.Unknown;
    }

    private BluetoothGattService getService(BluetoothGatt gatt, String uuid) {
        Log.i(TAG, "getService...");

        if (gatt == null) {
            Log.w(TAG, "  No GATT yet.");
            return null;
        }

        if (uuid.length() == 4) {
            Log.i(TAG, "  Partial service UUID");
            String partialUUID = "0000" + uuid;
            for (BluetoothGattService service : gatt.getServices()) {
                if (service.getUuid().toString().startsWith(partialUUID))
                    return service;
            }
        }
        else {
            Log.i(TAG, "  Full service UUID");
            BluetoothGattService service = gatt.getService(UUID.fromString(uuid));
            if (service != null)
                return service;
        }

        Log.i(TAG, "  No such service found.");
        return null;
    }

    private BluetoothGattCharacteristic getGattCharacteristic(BluetoothGatt gatt, String serviceUUID, String characteristicUUID) {
        Log.i(TAG, "getGattCharacteristic...");
        Log.i(TAG, "  Service UUID       : " + serviceUUID);
        Log.i(TAG, "  Characteristic UUID: " + characteristicUUID);

        if (gatt == null)
            return null;

        BluetoothGattService service = getService(gatt, serviceUUID);
        if (service == null)
            return null;

        if (characteristicUUID.length() == 4) {
            Log.i(TAG, "  Partial characteristic UUID");
            String partialUUID = "0000" + characteristicUUID;
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                if (characteristic.getUuid().toString().startsWith(partialUUID))
                    return characteristic;
            }
        }
        else {
            Log.i(TAG, "  Full characteristic UUID");
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(characteristicUUID));
            if (characteristic != null)
                return characteristic;
        }

        Log.w(TAG, "  No such characteristic found.");
        return null;
    }

    private void logServices(BluetoothGatt gatt) {
        Log.i(TAG, "logServices...");
        List<BluetoothGattService> services = gatt.getServices();
        for (BluetoothGattService service : services) {
            Log.i(TAG, "  Service UUID:" + service.getUuid().toString());

            List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
            for (BluetoothGattCharacteristic characteristic : characteristics) {
                Log.i(TAG, "  Characteristic UUID: " + characteristic.getUuid().toString());
                logCharacteristicPermissions(characteristic.getPermissions());
                logCharacteristicProperties(characteristic.getProperties());

                List<BluetoothGattDescriptor> descriptors = characteristic.getDescriptors();
                for (BluetoothGattDescriptor descriptor : descriptors) {
                    Log.i(TAG, "  Descriptor UUID: " + descriptor.getUuid().toString());
                }
            }
        }
    }

    private void logCharacteristicPermissions(int permissions) {
        Log.i(TAG, "logCharacteristicPermissions...");
        if ((permissions & BluetoothGattCharacteristic.PERMISSION_READ) != 0)
            Log.i(TAG, "  PERMISSION_READ");
        if ((permissions & BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED) != 0)
            Log.i(TAG, "  PERMISSION_READ_ENCRYPTED");
        if ((permissions & BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED_MITM) != 0)
            Log.i(TAG, "  PERMISSION_READ_ENCRYPTED_MITM");
        if ((permissions & BluetoothGattCharacteristic.PERMISSION_WRITE) != 0)
            Log.i(TAG, "  PERMISSION_WRITE");
        if ((permissions & BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED) != 0)
            Log.i(TAG, "  PERMISSION_WRITE_ENCRYPTED");
        if ((permissions & BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED_MITM) != 0)
            Log.i(TAG, "  PERMISSION_WRITE_ENCRYPTED_MITM");
        if ((permissions & BluetoothGattCharacteristic.PERMISSION_WRITE_SIGNED) != 0)
            Log.i(TAG, "  PERMISSION_WRITE_SIGNED");
        if ((permissions & BluetoothGattCharacteristic.PERMISSION_WRITE_SIGNED_MITM) != 0)
            Log.i(TAG, "  PERMISSION_WRITE_SIGNED_MITM");
    }

    private void logCharacteristicProperties(int properties) {
        Log.i(TAG, "logCharacteristicProperties...");
        if ((properties & BluetoothGattCharacteristic.PROPERTY_BROADCAST) != 0)
            Log.i(TAG, "  PROPERTY_BROADCAST");
        if ((properties & BluetoothGattCharacteristic.PROPERTY_EXTENDED_PROPS) != 0)
            Log.i(TAG, "  PROPERTY_EXTENDED_PROPS");
        if ((properties & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0)
            Log.i(TAG, "  PROPERTY_INDICATE");
        if ((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0)
            Log.i(TAG, "  PROPERTY_NOTIFY");
        if ((properties & BluetoothGattCharacteristic.PROPERTY_READ) != 0)
            Log.i(TAG, "  PROPERTY_READ");
        if ((properties & BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE) != 0)
            Log.i(TAG, "  PROPERTY_SIGNED_WRITE");
        if ((properties & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0)
            Log.i(TAG, "  PROPERTY_WRITE");
        if ((properties & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0)
            Log.i(TAG, "  PROPERTY_WRITE_NO_RESPONSE");
    }

    //

    private static class Command {

        public enum CommandType {
            SEND_QUICK_DRIVE,
            SEND_REMOTE_CONTROL,
            RESET_WATCHDOG,
            READ_CHARACTERISTIC,
            QUIT
        };

        private CommandType commandType;
        private Object commandParameter;

        private Command(CommandType commandType, Object commandParameter) {
            this.commandType = commandType;
            this.commandParameter = commandParameter;
        }

        public static Command initWatchdog() {
            return new Command(CommandType.RESET_WATCHDOG, new byte[] { 0x0d, 0x00 });
        }

        public static Command initRemoteControl(byte channel, byte invert, byte value) {
            return new Command(CommandType.SEND_REMOTE_CONTROL, new byte[] { 0x01, channel, invert, value });
        }

        public static Command initQuickDrive(byte v1, byte v2, byte v3, byte v4) {
            return new Command(CommandType.SEND_QUICK_DRIVE, new byte[] { v1, v2, v3, v4 });
        }

        public static Command initReadCharacteristic(SBrickCharacteristicType characteristicType) {
            return new Command(CommandType.READ_CHARACTERISTIC, characteristicType);
        }

        public static Command initQuit() {
            return new Command(CommandType.QUIT, null);
        }

        public CommandType getCommandType() { return commandType; }
        public Object getCommandParameter() { return commandParameter; }
    }
}
