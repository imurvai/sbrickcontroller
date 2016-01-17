package com.scn.sbrickmanager;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;
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
    private BluetoothGatt bluetoothGatt;

    private boolean readingCharacteristics = false;
    private LinkedList<SBrickCharacteristicType> characteristicsToRead = new LinkedList<>();
    private SBrickCharacteristics sBrickCharacteristics = null;

    //
    // API
    //

    SBrickImpl(Context context, BluetoothDevice bluetoothDevice) {
        super(context);

        Log.i(TAG, "SBrickImpl...");
        Log.i(TAG, "  address: " + bluetoothDevice.getAddress());
        Log.i(TAG, "  name:    " + bluetoothDevice.getName());

        this.bluetoothDevice = bluetoothDevice;
    }

    @Override
    public String getAddress() {
        return bluetoothDevice.getAddress();
    }

    @Override
    public String getName() {
        return bluetoothDevice.getName();
    }

    @Override
    public boolean connect() {
        Log.i(TAG, "connect - " + getAddress());

        if (isConnected()) {
            Log.i(TAG, "  Already connected.");
            return false;
        }

        bluetoothGatt = bluetoothDevice.connectGatt(context, true, gattCallback);
        if (bluetoothGatt == null)
            throw new RuntimeException("Can't create GATT - " + getAddress());

        return true;
    }

    @Override
    public void disconnect() {
        Log.i(TAG, "disconnect - " + getAddress());

        if (!isConnected()) {
            Log.i(TAG, "  Already disconnected.");

            if (bluetoothGatt != null)
                bluetoothGatt.close();

            return;
        }

        bluetoothGatt.disconnect();
        bluetoothGatt.close();
    }

    @Override
    public boolean getCharacteristicsAsync() {
        Log.i(TAG, "getCharacteristicsAsync - " + getAddress());

        if (!isConnected())
            throw new RuntimeException("SBrick hasn't been connected yet - " + getAddress());

        if (readingCharacteristics) {
            Log.i(TAG, "  Already reading characteristics.");
            return false;
        }

        characteristicsToRead.addLast(SBrickCharacteristicType.DeviceName);
        characteristicsToRead.addLast(SBrickCharacteristicType.Appearance);
        characteristicsToRead.addLast(SBrickCharacteristicType.ModelNumber);
        characteristicsToRead.addLast(SBrickCharacteristicType.FirmwareRevision);
        characteristicsToRead.addLast(SBrickCharacteristicType.HardwareRevision);
        characteristicsToRead.addLast(SBrickCharacteristicType.SoftwareRevision);
        characteristicsToRead.addLast(SBrickCharacteristicType.ManufacturerName);

        sBrickCharacteristics = new SBrickCharacteristics();
        sBrickCharacteristics.setAddress(getAddress());

        readingCharacteristics = readNextCharacteristic();
        return readingCharacteristics;
    }

    @Override
    public boolean sendCommand(int channel, int value, boolean invert) {
        Log.i(TAG, "sendCommand - " + getAddress());
        Log.i(TAG, "  channel: " + channel);
        Log.i(TAG, "  value:   " + value);
        Log.i(TAG, "  invert:  " + (invert ? "true" : "false"));

        if (channel < 0 || 3 < channel)
            throw new RuntimeException("channel must be 0-3.");

        if (value < 0 || 255 < value)
            throw new RuntimeException("value must be 0-255.");

        if (!isConnected())
            return false;
            //throw new RuntimeException("SBrick hasn't been connected yet - " + getAddress());

        BluetoothGattCharacteristic gattCharacteristic = getGattCharacteristic(bluetoothGatt, SERVICE_UUID_REMOTE_CONTROL, CHARACTERISTIC_UUID_REMOTE_CONTROL);
        if (gattCharacteristic == null)
            return false;
            //throw new RuntimeException("No characteristic found - " + CHARACTERISTIC_UUID_REMOTE_CONTROL);

        byte[] packet = new byte[4];
        packet[0] = 0x01;
        packet[1] = (byte)channel;
        packet[2] = invert ? (byte)1 : (byte)0;
        packet[3] = (byte)value;

        if (gattCharacteristic.setValue(packet))
            return bluetoothGatt.writeCharacteristic(gattCharacteristic);

        return false;
    }

    //
    // Private classes and methods
    //

    private boolean isConnected() {
        Log.i(TAG, "isConnected...");

        final BluetoothManager bluetoothManager = (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager == null)
            throw new RuntimeException("Can't find bluetooth manager.");

        if (bluetoothGatt == null || bluetoothManager.getConnectionState(bluetoothDevice, BluetoothProfile.GATT) != BluetoothProfile.STATE_CONNECTED) {
            Log.i(TAG, "  Not connected.");
            return false;
        }

        Log.i(TAG, "  Connected.");
        return true;
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
                    gatt.discoverServices();
                    break;

                case BluetoothProfile.STATE_DISCONNECTING:
                    Log.i(TAG, "  STATE_DISCONNECTING");
                    break;

                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.i(TAG, "  STATE_DISCONNECTED");
                    readingCharacteristics = false;
                    sendLocalBroadcast(ACTION_SBRICK_DISCONNECTED);
                    break;

                default:
                    Log.w(TAG, "  Unknown state.");
                    break;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.i(TAG, "BluetoothGattCallback.onServicesDiscovered - " + getAddress());

            switch (status) {
                case BluetoothGatt.GATT_SUCCESS:
                    Log.i(TAG, "  GATT_SUCCESS");
                    logServices(gatt);
                    sendLocalBroadcast(ACTION_SBRICK_CONNECTED);
                    break;

                case BluetoothGatt.GATT_FAILURE:
                    Log.w(TAG, "  GATT_FAILURE");
                    disconnect();

                default:
                    Log.w(TAG, "  GATT status: " + status);
                    break;
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.i(TAG, "BluetoothGattCallback.onCharacteristicRead - " + getAddress());

            switch (status) {
                case BluetoothGatt.GATT_SUCCESS:
                    Log.i(TAG, "  GATT_SUCCESS");

                    SBrickCharacteristicType sbrickCharacteristic = getCharacteristic(characteristic);
                    switch (sbrickCharacteristic) {
                        case DeviceName:
                            sBrickCharacteristics.setDeviceName(characteristic.getStringValue(0));
                            break;
                        case ModelNumber:
                            sBrickCharacteristics.setModelNumber(characteristic.getStringValue(0));
                            break;
                        case FirmwareRevision:
                            sBrickCharacteristics.setFirmwareRevision(characteristic.getStringValue(0));
                            break;
                        case HardwareRevision:
                            sBrickCharacteristics.setHardwareRevision(characteristic.getStringValue(0));
                            break;
                        case SoftwareRevision:
                            sBrickCharacteristics.setSoftwareRevision(characteristic.getStringValue(0));
                            break;
                        case ManufacturerName:
                            sBrickCharacteristics.setManufacturerName(characteristic.getStringValue(0));
                            break;

                        case Appearance:
                            int value = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0);
                            sBrickCharacteristics.setManufacturerName(String.valueOf(value));
                            break;

                        default:
                            Log.i(TAG, "  Unknown characteristic.");
                            break;
                    }

                    break;

                default:
                    Log.w(TAG, "  GATT status: " + status);
                    break;
            }

            readNextCharacteristic();
        }
    };

    private boolean readNextCharacteristic() {
        Log.i(TAG, "readNextCharacteristic...");

        if (characteristicsToRead.isEmpty()) {
            Log.i(TAG, "  No more characteristics to read, send the broadcast message...");
            sendCharacteristicsBroadcast();
            return false;
        }

        SBrickCharacteristicType nextCharacteristic = characteristicsToRead.pollFirst();
        if (!readCharacteristicAsync(nextCharacteristic)) {
            Log.w(TAG, "  Can't read the next characteristic, send the broadcast message...");
            sendCharacteristicsBroadcast();
            return false;
        }

        return true;
    }

    private void sendCharacteristicsBroadcast() {
        Log.i(TAG, "sendCharacteristicsBroadcast...");

        readingCharacteristics = false;

        Intent intent = new Intent();
        intent.setAction(ACTION_SBRICK_CHARACTERISTIC_READ);
        intent.putExtra(EXTRA_CHARACTERISTICS, sBrickCharacteristics);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private boolean readCharacteristicAsync(SBrickCharacteristicType characteristic) {
        Log.i(TAG, "readCharacteristicAsync - " + getAddress());

        if (bluetoothGatt == null) {
            Log.w(TAG, "  Device is not connected.");
            return false;
        }

        String serviceUUID = "";
        String characteristicUUID = "";
        switch (characteristic) {
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
            throw new RuntimeException("No characteristic found - " + characteristicUUID);

        return bluetoothGatt.readCharacteristic(gattCharacteristic);
    }

    private SBrickCharacteristicType getCharacteristic(BluetoothGattCharacteristic characteristic) {
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
        else if (uuid.equals(CHARACTERISTIC_UUID_QUICK_DRIVE))
            return SBrickCharacteristicType.QuickDrive;
        return SBrickCharacteristicType.Unknown;
    }

    private BluetoothGattService getService(BluetoothGatt gatt, String uuid) {
        Log.i(TAG, "getService...");

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
        Log.i(TAG, "getCharacteristicWithPartialUUIDs...");
        Log.i(TAG, "  Service UUID       : " + serviceUUID);
        Log.i(TAG, "  Characteristic UUID: " + characteristicUUID);

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
