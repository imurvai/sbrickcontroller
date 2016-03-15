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

import com.scn.sbrickmanager.sbrickcommand.Command;
import com.scn.sbrickmanager.sbrickcommand.CommandMethod;
import com.scn.sbrickmanager.sbrickcommand.WriteQuickDriveCommand;
import com.scn.sbrickmanager.sbrickcommand.WriteRemoteControlCommand;

import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

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

    private BluetoothGattCharacteristic remoteControlCharacteristic = null;
    private BluetoothGattCharacteristic quickDriveCharacteristic = null;

    //
    // Constructor
    //

    SBrickImpl(Context context, SBrickManagerBase sbrickManager, BluetoothDevice bluetoothDevice) {
        super(context, sbrickManager);

        Log.i(TAG, "SBrickImpl...");
        Log.i(TAG, "  address: " + bluetoothDevice.getAddress());
        Log.i(TAG, "  name:    " + bluetoothDevice.getName());

        this.bluetoothDevice = bluetoothDevice;
        setName(bluetoothDevice.getName());
    }

    //
    // SBrick overrides
    //

    @Override
    public String getAddress() {
        return bluetoothDevice.getAddress();
    }

    @Override
    public void disconnect() {

        synchronized (sbrickManager.getLockObject()) {
            Log.i(TAG, "disconnect - " + getAddress());

            if (isConnected && bluetoothGatt != null) {
                bluetoothGatt.disconnect();
                bluetoothGatt.close();
            }

            isConnected = false;
            remoteControlCharacteristic = null;
            quickDriveCharacteristic = null;
        }
    }

    //
    // SBrickBase overrides
    //

    @Override
    protected CommandMethod createConnectCommandMethod() {
        Log.i(TAG, "createConnectCommandMethod - " + getAddress());

        return new CommandMethod() {
            @Override
            public boolean execute() {
                Log.i(TAG, "Connect command method - " + getAddress());

                bluetoothGatt = bluetoothDevice.connectGatt(context, true, gattCallback);
                if (bluetoothGatt == null) {
                    Log.w(TAG, "  Can't connect to GATT for SBrick: " + getAddress());
                    return false;
                }

                return true;
            }
        };
    }

    @Override
    protected CommandMethod createDiscoverServicesCommandMethod() {
        Log.i(TAG, "createDiscoverServicesCommandMethod - " + getAddress());

        return new CommandMethod() {
            @Override
            public boolean execute() {
                Log.i(TAG, "Discover services command method - " + getAddress());

                if (bluetoothGatt == null) {
                    Log.w(TAG, "  bluetoothGatt is null.");
                    return false;
                }

                return bluetoothGatt.discoverServices();
            }
        };
    }

    @Override
    protected CommandMethod createReadCharacteristicCommandMethod(final SBrickCharacteristicType characteristicType) {
        Log.i(TAG, "createReadCharacteristicCommandMethod - " + getAddress());

        return new CommandMethod() {
            @Override
            public boolean execute() {
                Log.i(TAG, "Read characteristic command method - " + getAddress());

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
            }
        };
    }

    @Override
    protected CommandMethod createWriteRemoteControlCommandMethod(final int channel, final int value) {
        //Log.i(TAG, "createWriteRemoteControlCommandMethod - " + getAddress());

        return new CommandMethod() {
            @Override
            public boolean execute() {
                //Log.i(TAG, "Write remote control command method - " + getAddress());

                byte invert = (byte) ((0 <= value) ? 0 : 1);
                byte byteValue = (byte) (Math.min(255, Math.abs(value)));

                byte[] commandBuffer = new byte[]{0x01, (byte) channel, invert, byteValue};
                return remoteControlCharacteristic.setValue(commandBuffer) &&
                        bluetoothGatt.writeCharacteristic(remoteControlCharacteristic);
            }
        };
    }

    @Override
    protected CommandMethod createWriteQuickDriveCommandMethod(final int v0, final int v1, final int v2, final int v3) {
        //Log.i(TAG, "createWriteQuickDriveCommandMethod - " + getAddress());

        return new CommandMethod() {
            @Override
            public boolean execute() {
                //Log.i(TAG, "Write quick drive command method - " + getAddress());

                // 0 doesn't stop the watchdog on quick drive, let's set the second bit to 1
                byte bv0 = (byte) ((Math.min(255, Math.abs(v0)) & 0xfe) | 0x02 | (0 <= v0 ? 0 : 1));
                byte bv1 = (byte) ((Math.min(255, Math.abs(v1)) & 0xfe) | 0x02 | (0 <= v1 ? 0 : 1));
                byte bv2 = (byte) ((Math.min(255, Math.abs(v2)) & 0xfe) | 0x02 | (0 <= v2 ? 0 : 1));
                byte bv3 = (byte) ((Math.min(255, Math.abs(v3)) & 0xfe) | 0x02 | (0 <= v3 ? 0 : 1));

                byte[] commandBuffer = new byte[]{bv0, bv1, bv2, bv3};
                return quickDriveCharacteristic.setValue(commandBuffer) &&
                        bluetoothGatt.writeCharacteristic(quickDriveCharacteristic);
            }
        };
    }

    //
    // Private classes and methods
    //

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            synchronized (sbrickManager.getLockObject()) {
                Log.i(TAG, "BluetoothGattCallback.onConnectionStateChange - " + getAddress());

                if (status == BluetoothGatt.GATT_SUCCESS) {

                    switch (newState) {
                        case BluetoothProfile.STATE_CONNECTED:
                            Log.i(TAG, "  STATE_CONNECTED");

                            bluetoothGatt = gatt;

                            // Discover services
                            CommandMethod commandMethod = createDiscoverServicesCommandMethod();
                            sbrickManager.sendCommand(Command.newDiscoverServicesCommand(SBrickImpl.this, commandMethod));
                            break;

                        case BluetoothProfile.STATE_DISCONNECTED:
                            Log.i(TAG, "  STATE_DISCONNECTED");

                            isConnected = false;
                            remoteControlCharacteristic = null;
                            quickDriveCharacteristic = null;

                            sendLocalBroadcast(ACTION_SBRICK_DISCONNECTED);
                            break;

                        default:
                            Log.i(TAG, "  State: " + newState);
                            break;
                    }
                } else {
                    Log.w(TAG, "  GATT not success.");
                    sendLocalBroadcast(ACTION_SBRICK_CONNECT_FAILED);
                }

                // Release the semaphore to let the command process thread to proceed.
                sbrickManager.releaseCommandSemaphore();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

            synchronized (sbrickManager.getLockObject()) {
                Log.i(TAG, "BluetoothGattCallback.onServicesDiscovered - " + getAddress());

                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.i(TAG, "  GATT_SUCCESS");
                    //logServices(gatt);

                    remoteControlCharacteristic = getGattCharacteristic(gatt, SERVICE_UUID_REMOTE_CONTROL, CHARACTERISTIC_UUID_REMOTE_CONTROL);
                    quickDriveCharacteristic = getGattCharacteristic(gatt, SERVICE_UUID_REMOTE_CONTROL, CHARACTERISTIC_UUID_QUICK_DRIVE);
                    isConnected = true;

                    sendLocalBroadcast(ACTION_SBRICK_CONNECTED);
                } else {
                    Log.w(TAG, "  GATT not success.");
                    disconnect();
                    sendLocalBroadcast(ACTION_SBRICK_CONNECT_FAILED);
                }

                // Release the semaphore to let the command process thread to proceed.
                sbrickManager.releaseCommandSemaphore();
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

            synchronized (sbrickManager.getLockObject()) {
                Log.i(TAG, "BluetoothGattCallback.onCharacteristicRead - " + getAddress());

                if (status == BluetoothGatt.GATT_SUCCESS) {
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
                } else {
                    Log.w(TAG, "  GATT not success.");
                    sendLocalBroadcast(ACTION_SBRICK_READ_CHARACTERISTIC_FAILED);
                }

                // Release the semaphore to let the command process thread to proceed.
                sbrickManager.releaseCommandSemaphore();
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

            synchronized (sbrickManager.getLockObject()) {
                //Log.i(TAG, "BluetoothGattCallback.onCharacteristicWrite...");

                if (status == BluetoothGatt.GATT_SUCCESS) {
                    //Log.i(TAG, "  GATT_SUCCESS");

                    if (lastWriteCommand != null) {

                        // Update channel values
                        if (lastWriteCommand instanceof WriteRemoteControlCommand) {
                            //Log.i(TAG, "  Last write command is remote control.");

                            WriteRemoteControlCommand command = (WriteRemoteControlCommand) lastWriteCommand;
                            channelValues[command.getChannel()] = command.getValue();
                        } else if (lastWriteCommand instanceof WriteQuickDriveCommand) {
                            //Log.i(TAG, "  Last write command is quick drive.");

                            WriteQuickDriveCommand command = (WriteQuickDriveCommand) lastWriteCommand;
                            channelValues[0] = command.getV0();
                            channelValues[1] = command.getV1();
                            channelValues[2] = command.getV2();
                            channelValues[3] = command.getV3();
                        }

                        // Start the watchdog timer if any of the channels is not 0
                        boolean needWatchdog = channelValues[0] != 0 || channelValues[1] != 0 || channelValues[2] != 0 || channelValues[3] != 0;
                        if (needWatchdog)
                            startWatchdogTimer();
                        else
                            stopWatchdogTimer();
                    }
                } else {
                    Log.w(TAG, "  GATT not success.");
                    sendLocalBroadcast(ACTION_SBRICK_WRITE_CHARACTERISTIC_FAILED);
                }

                // Release the semaphore to let the command process thread to proceed.
                sbrickManager.releaseCommandSemaphore();
            }
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
}
