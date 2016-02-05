package com.scn.sbrickmanager;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

class SBrickManagerMock extends SBrickManagerBase {

    //
    // Private members
    //

    private static final String TAG = SBrickManagerMock.class.getSimpleName();

    private static final String SBrickName = "SCNBrick";

    private AsyncTask<Void, Void, Void> scanAsyncTask = null;

    private String[] sbrickAddresses = {
            "00:11:22:33:44",
            "11:22:33:44:55",
            "22:33:44:55:66"
    };

    //
    // Constructor
    //

    SBrickManagerMock(Context context) {
        super(context);

        Log.i(TAG, "SBrickManagerMock...");
    }

    //
    // SBrickManagerBase overrides
    //

    @Override
    protected SBrick createSBrick(String sbrickAddress) {
        Log.i(TAG, "createSBrick - " + sbrickAddress);

        if (sbrickMap.containsKey(sbrickAddress)) {
            Log.i(TAG, "  SBrick has already been created.");
            return sbrickMap.get(sbrickAddress);
        }

        SBrick sbrick = new SBrickMock(context, sbrickAddress, "SCNBrick");
        sbrickMap.put(sbrickAddress, sbrick);
        return sbrick;
    }

    //
    // SBrickManager overrides
    //

    @Override
    public boolean isBLESupported() {
        return true;
    }

    @Override
    public boolean isBluetoothOn() {
        return true;
    }

    @Override
    public boolean startSBrickScan() {
        Log.i(TAG, "startSBrickScan...");

        if (scanAsyncTask != null) {
            Log.w(TAG, "  Already scanning.");
            return false;
        }

        scanAsyncTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {

                try {
                    for (int i = 0; i < sbrickAddresses.length; i++) {

                        // Wait a bit before 'finding' an SBrick
                        for (int j = 0; j < 30; j++) {
                            if (isCancelled())
                                return null;

                            Thread.sleep(10, 0);
                        }

                        // 'Found' an SBrick
                        String address = sbrickAddresses[i];
                        if (!sbrickMap.containsKey(address)) {
                            Log.i(TAG, "  Storing SBrick.");
                            Log.i(TAG, "    Device address: " + address);

                            SBrick sbrick = createSBrick(address);

                            Intent sendIntent = new Intent();
                            sendIntent.setAction(ACTION_FOUND_AN_SBRICK);
                            sendIntent.putExtra(EXTRA_SBRICK_ADDRESS, address);
                            sendIntent.putExtra(EXTRA_SBRICK_NAME, sbrick.getName());
                            LocalBroadcastManager.getInstance(context).sendBroadcast(sendIntent);
                        } else {
                            Log.i(TAG, "  Sbrick has already been discovered.");
                        }
                    }
                }
                catch (Exception ex) {
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                scanAsyncTask = null;
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                scanAsyncTask = null;
            }
        }.execute();

        return true;
    }

    @Override
    public void stopSBrickScan() {
        Log.i(TAG, "stopSBrickScan...");

        if (scanAsyncTask == null) {
            Log.i(TAG, "  Not scanning.");
            return;
        }

        scanAsyncTask.cancel(true);
    }

    @Override
    public SBrick getSBrick(String sbrickAddress) {
        Log.i(TAG, "getSBrick - " + sbrickAddress);

        if (sbrickMap.containsKey(sbrickAddress))
            return sbrickMap.get(sbrickAddress);

        Log.i(TAG, "  BSrick not found.");
        return null;
    }
}
