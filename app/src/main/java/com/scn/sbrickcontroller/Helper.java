package com.scn.sbrickcontroller;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * Created by Istvan_Murvai on 2015-02-27.
 */
class Helper {

    private static final String TAG = Helper.class.getSimpleName();

    /**
     * Checks if Bluetooth is on.
     * @return true if on, false otherwise.
     */
    public static boolean isBluetoothOn(Context context) {
        Log.i(TAG, "isBluetoothOn...");

        final BluetoothManager bluetoothManager = (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager == null)
            throw new RuntimeException("Can't find bluetooth manager.");

        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null)
            throw new RuntimeException("Can't find bluetooth adapter.");

        return bluetoothAdapter.isEnabled();
    }

    /**
     * Checks if Bluetooth low energy profile is supported by the device.
     * @return true if supported, false otherwise.
     */
    public static boolean isBleSupported(Context context) {
        Log.i(TAG, "isBleSupported...");

        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    /**
     * Shows a progress dialog with the given message and a Cancel button.
     * @param context The context the dialog is shown in.
     * @param message The message shown on the dialog.
     * @param onClickListener Listener to handle the Cancel button event.
     * @return The ProgressDialog instance.
     */
    public static ProgressDialog showProgressDialog(Context context, String message, final DialogInterface.OnClickListener onClickListener) {
        Log.i(TAG, "showProgressDialog");

        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage(message);
        dialog.setCancelable(false);
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", onClickListener);
        dialog.show();
        return dialog;
    }
}
