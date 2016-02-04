package com.scn.sbrickmanager;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.Timer;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;

/**
 * SBrick base abstract class.
 */
abstract class SBrickBase implements SBrick {

    //
    // Private members
    //

    private static final String TAG = "SBrickBase";

    private String name = null;

    //
    // Protected members
    //

    protected final Context context;
    protected boolean isConnected = false;

    protected LinkedBlockingDeque<Command> commandQueue = new LinkedBlockingDeque<>(100);
    protected Semaphore commandSendingSemaphore = new Semaphore(1);
    protected Timer watchdogTimer = null;

    protected int[] channelValues = new int[] { 0, 0, 0, 0 };

    //
    // Object overrides
    //

    @Override
    public String toString() {
        return getName();
    }

    //
    // SBrick overrides
    //

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
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

        Command command = Command.newReadCharacteristic(characteristicType);
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
        Command command = Command.newRemoteControl((byte) channel, invert, byteValue);
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

        Command command = Command.newQuickDrive(bv1, bv2, bv3, bv4);
        return commandQueue.offer(command);
    }

    //
    // Internal API
    //

    protected SBrickBase(Context context) {
        Log.i(TAG, "SBrickBase...");

        this.context = context;
    }

    protected Intent buildBroadcastIntent(String action) {
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra(EXTRA_SBRICK_ADDRESS, getAddress());
        return intent;
    }

    protected void sendLocalBroadcast(String action) {
        Log.i(TAG, "sendLocalBroadcast...");
        LocalBroadcastManager.getInstance(context).sendBroadcast(buildBroadcastIntent(action));
    }

    protected void startCommandProcessing() {
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
                            Log.i(TAG, "Empty the command queue...");
                            commandQueue.clear();
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

    protected void stopCommandProcessing() {
        Log.i(TAG, "stopCommandProcessing...");

        Command quitCommand = Command.newQuit();
        if (!commandQueue.offerFirst(quitCommand)) {
            Log.e(TAG, "  Could not send quit command to queue.");
            return;
        }

        // Just to be sure the semaphore doesn't block the thread.
        commandSendingSemaphore.release();
    }

    protected abstract boolean processCommand(Command command);

    //

    protected static class Command {

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

        public static Command newWatchdog() {
            return new Command(CommandType.RESET_WATCHDOG, new byte[] { 0x0d, 0x00 });
        }

        public static Command newRemoteControl(byte channel, byte invert, byte value) {
            return new Command(CommandType.SEND_REMOTE_CONTROL, new byte[] { 0x01, channel, invert, value });
        }

        public static Command newQuickDrive(byte v1, byte v2, byte v3, byte v4) {
            return new Command(CommandType.SEND_QUICK_DRIVE, new byte[] { v1, v2, v3, v4 });
        }

        public static Command newReadCharacteristic(SBrickCharacteristicType characteristicType) {
            return new Command(CommandType.READ_CHARACTERISTIC, characteristicType);
        }

        public static Command newQuit() {
            return new Command(CommandType.QUIT, null);
        }

        public CommandType getCommandType() { return commandType; }
        public Object getCommandParameter() { return commandParameter; }
    }
}
