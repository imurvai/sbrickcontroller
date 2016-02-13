package com.scn.sbrickmanager;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.Iterator;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
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
    protected Command lastWriteCommand = null;

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
    public synchronized boolean readCharacteristic(SBrickCharacteristicType characteristicType) {
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
        //Log.i(TAG, "sendCommand - " + getAddress());
        //Log.i(TAG, "  channel: " + channel);
        //Log.i(TAG, "  value: " + value);

        if (!isConnected) {
            Log.i(TAG, "  Not connected.");
            return false;
        }

        // Filter out the lower bits (they don't take any effect)
        value = value & 0xfffffff8;

        // If value hasn't changed no need to resend it.
        if (value == channelValues[channel])
            return true;

        Command command = Command.newRemoteControl(channel, value);
        return commandQueue.offer(command);
    }

    @Override
    public synchronized boolean sendCommand(int v0, int v1, int v2, int v3) {
        //Log.i(TAG, "sendCommand - " + getAddress());
        //Log.i(TAG, "  value1: " + v0);
        //Log.i(TAG, "  value2: " + v1);
        //Log.i(TAG, "  value3: " + v2);
        //Log.i(TAG, "  value4: " + v3);

        if (!isConnected) {
            Log.i(TAG, "  Not connected.");
            return false;
        }

        // Filter out the lower bits (they don't take any effect)
        v0 = v0 & 0xfffffff8;
        v1 = v1 & 0xfffffff8;
        v2 = v2 & 0xfffffff8;
        v3 = v3 & 0xfffffff8;

        // If values haven't changed no need to resend them.
        if (v0 == channelValues[0] && v1 == channelValues[1] && v2 == channelValues[2] && v3 == channelValues[3])
            return true;

        Command command = Command.newQuickDrive(v0, v1, v2, v3);
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

    protected synchronized void startCommandProcessing() {
        Log.i(TAG, "startCommandProcessing...");

        Thread commandProcessThread = new Thread() {

            @Override
            public void run() {

                try {
                    commandQueue.clear();
                    lastWriteCommand = null;
                    channelValues[0] = 0;
                    channelValues[1] = 0;
                    channelValues[2] = 0;
                    channelValues[3] = 0;

                    while (true) {
                        try {
                            // Wait for the GATT callback to release the semaphore.
                            commandSendingSemaphore.acquire();

                            synchronized (this) {
                                // Get the next command to process.
                                Command command = commandQueue.take();
                                if (command.getCommandType() == Command.CommandType.QUIT) {
                                    Log.i(TAG, "Command process thread quits.");
                                    Log.i(TAG, "Empty the command queue...");
                                    commandQueue.clear();
                                    break;
                                }

                                if (!processCommand(command)) {
                                    Log.i(TAG, "Command send failed.");
                                    // Command wasn't sent, no need to wait for the GATT callback.
                                    commandSendingSemaphore.release();
                                }
                            }
                        }
                        catch (Exception ex) {
                            Log.e(TAG, "Command process thread has thrown an exception.", ex);
                            commandSendingSemaphore.release();
                        }
                    }
                }
                catch (Exception ex) {
                    Log.e(TAG, "Command process thread has thrown an exception.", ex);
                }
            }
        };

        commandProcessThread.start();
    }

    protected synchronized void stopCommandProcessing() {
        Log.i(TAG, "stopCommandProcessing...");

        Command quitCommand = Command.newQuit();
        commandQueue.clear();
        commandQueue.offerFirst(quitCommand);

        // Just to be sure the semaphore doesn't block the thread.
        commandSendingSemaphore.release();

        // Stop the watchdog as well
        stopWatchdogTimer();
    }

    protected abstract boolean processCommand(Command command);

    protected synchronized void startWatchdogTimer() {
        //Log.i(TAG, "startWatchdogTimer...");

        // Stop watchdog timer if already running
        if (watchdogTimer != null) {
            watchdogTimer.cancel();
            watchdogTimer.purge();
        }

        watchdogTimer = new Timer();
        watchdogTimer.schedule(new TimerTask() {
            @Override
            public synchronized void run() {
                //Log.i(TAG, "watchdogTimer.schedule...");

                // Check if there is at least one write command in the queue
                boolean hasWriteCommand = false;
                Iterator<Command> commandIterator = commandQueue.iterator();

                while (commandIterator.hasNext()) {
                    Command command = commandIterator.next();
                    if (command.getCommandType() == Command.CommandType.SEND_QUICK_DRIVE ||
                        command.getCommandType() == Command.CommandType.SEND_REMOTE_CONTROL) {

                        hasWriteCommand = true;
                        break;
                    }
                }

                // If there is no write command in the queue put back the last command
                if (!hasWriteCommand && lastWriteCommand != null) {
                    commandQueue.offer(lastWriteCommand);
                }

                watchdogTimer = null;
            }
        }, 200);
    }

    protected synchronized void stopWatchdogTimer() {
        //Log.i(TAG, "stopWatchdogTimer...");

        if (watchdogTimer != null) {
            watchdogTimer.cancel();
            watchdogTimer.purge();
            watchdogTimer = null;
        }
    }

    //

    protected static class Command {

        public enum CommandType {
            SEND_QUICK_DRIVE,
            SEND_REMOTE_CONTROL,
            READ_CHARACTERISTIC,
            QUIT
        };

        private CommandType commandType;
        private byte[] commandBuffer;
        private SBrickCharacteristicType characteristicType;
        private int channel;
        private int[] channelValues;

        private Command () {

            this.commandType = CommandType.QUIT;
            this.commandBuffer = null;

            this.characteristicType = SBrickCharacteristicType.Unknown;
            this.channel = -1;
            this.channelValues = null;
        }

        private Command(int channel, int value) {

            byte invert = (byte)((0 <= value) ? 0 : 1);
            byte byteValue = (byte)(Math.min(255, Math.abs(value)));

            this.commandType = CommandType.SEND_REMOTE_CONTROL;
            this.commandBuffer = new byte[] { 0x01, (byte)channel, invert, byteValue};

            this.characteristicType = SBrickCharacteristicType.Unknown;
            this.channel = channel;
            this.channelValues = new int[] { value };
        }

        private Command(int v1, int v2, int v3, int v4) {

            // 0 doesn't stop the watchdog on quick drive, let's set the second bit to 1
            byte bv1 = (byte)((Math.min(255, Math.abs(v1)) & 0xfe) | 0x02 | (0 <= v1 ? 0 : 1));
            byte bv2 = (byte)((Math.min(255, Math.abs(v2)) & 0xfe) | 0x02 | (0 <= v2 ? 0 : 1));
            byte bv3 = (byte)((Math.min(255, Math.abs(v3)) & 0xfe) | 0x02 | (0 <= v3 ? 0 : 1));
            byte bv4 = (byte)((Math.min(255, Math.abs(v4)) & 0xfe) | 0x02 | (0 <= v4 ? 0 : 1));

            this.commandType = CommandType.SEND_QUICK_DRIVE;
            this.commandBuffer = new byte[] { bv1, bv2, bv3, bv4 };

            this.characteristicType = SBrickCharacteristicType.Unknown;
            this.channel = -1;
            this.channelValues = new int[] { v1, v2, v3, v4 };
        }

        private Command(SBrickCharacteristicType characteristicType) {

            this.commandType = CommandType.READ_CHARACTERISTIC;
            this.commandBuffer = null;

            this.characteristicType = characteristicType;
            this.channel = -1;
            this.channelValues = null;
        }

        public static Command newRemoteControl(int channel, int value) {
            return new Command(channel, value);
        }

        public static Command newQuickDrive(int v1, int v2, int v3, int v4) {
            return new Command(v1, v2, v3, v4);
        }

        public static Command newReadCharacteristic(SBrickCharacteristicType characteristicType) {
            return new Command(characteristicType);
        }

        public static Command newQuit() {
            return new Command();
        }

        public CommandType getCommandType() { return commandType; }
        public byte[] getCommandBuffer() { return commandBuffer; }
        public SBrickCharacteristicType getCharacteristicType() { return characteristicType; }
        public int getChannel() { return channel; }
        public int[] getChannelValues() { return channelValues; }

        @Override
        public String toString() {
            switch (commandType) {

                case SEND_QUICK_DRIVE:
                    return "SEND_QUICK_DRIVE : " + channelValues[0] + " - " + channelValues[1] + " - " + channelValues[2] + " - " + channelValues[3];

                case SEND_REMOTE_CONTROL:
                    return "SEND_REMOTE_CONTROL : " + channel + " - " + channelValues[0];

                case READ_CHARACTERISTIC:
                    return "READ_CHARACTERISTIC";

                case QUIT:
                    return "QUIT";
            }

            return "Unknown command";
        }
    }
}
