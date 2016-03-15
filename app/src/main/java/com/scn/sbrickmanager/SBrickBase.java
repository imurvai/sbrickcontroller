package com.scn.sbrickmanager;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.scn.sbrickmanager.sbrickcommand.Command;
import com.scn.sbrickmanager.sbrickcommand.CommandMethod;
import com.scn.sbrickmanager.sbrickcommand.WriteCharacteristicCommand;

import java.util.Date;
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

    private long lastSendCommandTime = System.currentTimeMillis();

    //
    // Protected members
    //

    protected final Context context;
    protected final SBrickManagerBase sbrickManager;
    protected boolean isConnected = false;

    protected Timer watchdogTimer = null;
    protected int[] channelValues = new int[] { 0, 0, 0, 0 };
    protected WriteCharacteristicCommand lastWriteCommand = null;

    //
    // Constructor
    //

    protected SBrickBase(Context context, SBrickManagerBase sbrickManager) {
        Log.i(TAG, "SBrickBase...");

        this.context = context;
        this.sbrickManager = sbrickManager;
    }

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
    public boolean connect() {

        synchronized (sbrickManager.getLockObject()) {
            Log.i(TAG, "connect - " + getAddress());

            if (isConnected) {
                Log.i(TAG, "  Already connected.");
                return false;
            }

            CommandMethod commandMethod = createConnectCommandMethod();
            return sbrickManager.sendCommand(Command.newConnectCommand(this, commandMethod));
        }
    }

    @Override
    public boolean readCharacteristic(SBrickCharacteristicType characteristicType) {

        synchronized (sbrickManager.getLockObject()) {
            Log.i(TAG, "readCharacteristic - " + getAddress());

            if (!isConnected) {
                Log.w(TAG, "  Not connected.");
                return false;
            }

            CommandMethod commandMethod = createReadCharacteristicCommandMethod(characteristicType);
            return sbrickManager.sendCommand(Command.newReadCharacteristicCommand(this, commandMethod, characteristicType));
        }
    }

    @Override
    public boolean sendCommand(int channel, int value) {

        synchronized (sbrickManager.getLockObject()) {
            //Log.i(TAG, "sendCommand - " + getAddress());
            //Log.i(TAG, "  channel: " + channel);
            //Log.i(TAG, "  value: " + value);

            if (channel < 0 || 3 < channel)
                throw new IllegalArgumentException("channel must be in [0-3].");

            if (!isConnected) {
                Log.i(TAG, "  Not connected.");
                return false;
            }

            // Filter out the lower bits (they don't take any effect)
            value = Math.max(-255, Math.min(255, value & 0xfffffff8));

            // Prevent flooding the command queue
//            long timeSinceLastSendCommand = System.currentTimeMillis() - lastSendCommandTime;
//            if (timeSinceLastSendCommand < 20 && value != 0) {
//                Log.i(TAG, "  Too fast sendCommand, skip.");
//                return false;
//            }

            // If value hasn't changed no need to resend it.
            if (value == channelValues[channel]) {
                //Log.i(TAG, "  Same value, skip.");
                return true;
            }

            CommandMethod commandMethod = createWriteRemoteControlCommandMethod(channel, value);
            WriteCharacteristicCommand command = Command.newWriteRemoteControlCommand(this, commandMethod, channel, value);
            return sbrickManager.sendCommand(command);
        }
    }

    @Override
    public boolean sendCommand(int v0, int v1, int v2, int v3) {

        synchronized (sbrickManager.getLockObject()) {
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
            v0 = Math.max(-255, Math.min(255, v0 & 0xfffffff8));
            v1 = Math.max(-255, Math.min(255, v1 & 0xfffffff8));
            v2 = Math.max(-255, Math.min(255, v2 & 0xfffffff8));
            v3 = Math.max(-255, Math.min(255, v3 & 0xfffffff8));

            // Prevent flooding the command queue
//            long timeSinceLastSendCommand = System.currentTimeMillis() - lastSendCommandTime;
//            if (timeSinceLastSendCommand < 20 && (v0 != 0 || v1 != 0 || v2 != 0 || v3 != 0)) {
//                Log.i(TAG, "  Too fast sendCommand, skip.");
//                return false;
//            }

            // If values haven't changed no need to resend them.
            if (v0 == channelValues[0] && v1 == channelValues[1] && v2 == channelValues[2] && v3 == channelValues[3]) {
                //Log.i(TAG, "  same values, skip.");
                return true;
            }

            CommandMethod commandMethod = createWriteQuickDriveCommandMethod(v0, v1, v2, v3);
            WriteCharacteristicCommand command = Command.newWriteQuickDriveCommand(this, commandMethod, v0, v1, v2, v3);
            return sbrickManager.sendCommand(command);
        }
    }

    //
    // Internal API
    //

    void setLastWriteCommand(WriteCharacteristicCommand lastWriteCommand) {

        this.lastWriteCommand = lastWriteCommand;
        this.lastSendCommandTime = System.currentTimeMillis();
    }

    //
    // Protected methods
    //

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

    protected void startWatchdogTimer() {

        synchronized (sbrickManager.getLockObject()) {
            //Log.i(TAG, "startWatchdogTimer...");

            // Stop watchdog timer if already running
            if (watchdogTimer != null) {
                watchdogTimer.cancel();
                watchdogTimer.purge();
            }

            watchdogTimer = new Timer();
            watchdogTimer.schedule(new TimerTask() {
                @Override
                public void run() {

                    synchronized (sbrickManager.getLockObject()) {
                        //Log.i(TAG, "watchdogTimer.schedule...");

                        //// Check if there is at least one write command in the queue
                        //boolean hasWriteCommand = sbrickManager.hasWriteCommandForSBrick(SBrickBase.this);

                        // If there is no write command in the queue put back the last command
                        if (lastWriteCommand != null)
                            sbrickManager.sendPriorityCommand(lastWriteCommand);

                        watchdogTimer = null;
                    }
                }
            }, 200);
        }
    }

    protected void stopWatchdogTimer() {

        synchronized (sbrickManager.getLockObject()) {
            //Log.i(TAG, "stopWatchdogTimer...");

            if (watchdogTimer != null) {
                watchdogTimer.cancel();
                watchdogTimer.purge();
                watchdogTimer = null;
            }
        }
    }

    //
    // Abstract protected methods
    //

    protected abstract CommandMethod createConnectCommandMethod();

    protected abstract CommandMethod createDiscoverServicesCommandMethod();

    protected abstract CommandMethod createReadCharacteristicCommandMethod(SBrickCharacteristicType characteristicType);

    protected abstract CommandMethod createWriteRemoteControlCommandMethod(int channel, int value);

    protected abstract CommandMethod createWriteQuickDriveCommandMethod(int v0, int v1, int v2, int v3);
}
