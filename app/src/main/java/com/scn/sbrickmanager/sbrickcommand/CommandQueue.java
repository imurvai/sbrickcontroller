package com.scn.sbrickmanager.sbrickcommand;

import android.util.Log;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Semaphore;

/**
 * Created by steve on 2017. 01. 08..
 */
public class CommandQueue {

    //
    // Private members
    //

    private static final String TAG = CommandQueue.class.getSimpleName();

    private Object lockObject = new Object();
    private ArrayDeque<Command> queue;
    private Semaphore semaphore;

    //
    // Constructor
    //

    public CommandQueue(int minNumberOfCommands) {
        Log.i(TAG, "CommandQueue - " + minNumberOfCommands);

        try {
            queue = new ArrayDeque<>(minNumberOfCommands);

            semaphore = new Semaphore(1);
            semaphore.acquire();
        }
        catch (InterruptedException e) {
            Log.e(TAG, "  interrupted.");
        }
    }

    //
    // API
    //

    public void clear() {
        Log.i(TAG, "clear...");

        synchronized (lockObject) {
            queue.clear();
        }
    }

    public boolean offer(Command command) {
        //Log.i(TAG, "offer...");

        synchronized (lockObject) {
            if (command instanceof WriteQuickDriveCommand) {
                WriteQuickDriveCommand wqdc = (WriteQuickDriveCommand)command;

                ArrayList<WriteQuickDriveCommand> commandsToRemove = new ArrayList<>();

                // There must be only one quick drive command to avoid flooding
                Iterator<Command> iterator = queue.iterator();
                while (iterator.hasNext()) {
                    // If the command in queue is a quick drive command and for the same SBrick then remove it
                    Command queueCommand = iterator.next();
                    if (queueCommand instanceof WriteQuickDriveCommand) {
                        WriteQuickDriveCommand qwqdc = (WriteQuickDriveCommand)queueCommand;
                        if (wqdc.getSbrickAddress() == qwqdc.getSbrickAddress()) {
                            commandsToRemove.add(qwqdc);
                            Log.i(TAG, "  Command removed.");
                        }
                    }
                }

                for (WriteQuickDriveCommand commandToRemove : commandsToRemove) {
                    queue.remove(commandToRemove);
                }
            }

            queue.offer(command);
            semaphore.release();

            return true;
        }
    }

    public Command take() {
        //Log.i(TAG, "take...");

        try {
            semaphore.acquire();
            synchronized (lockObject) {
                return queue.pop();
            }
        }
        catch (InterruptedException e) {
            Log.e(TAG, "  queue interrupted.");
        }

        return null;
    }
}
