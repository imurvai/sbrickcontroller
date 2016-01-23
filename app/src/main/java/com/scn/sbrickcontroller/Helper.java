package com.scn.sbrickcontroller;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * Helper class.
 */
class Helper {

    //
    // Private members
    //

    private static final String TAG = Helper.class.getSimpleName();

    //
    // Constructor
    //

    private Helper() {}

    //
    // API
    //

    /**
     * Pops up a meessage box.
     * @param context is the current context.
     * @param message is the text to show.
     * @param onClickListener is the listener when the one and only button is clicked.
     * @return the Dialog instance.
     */
    public static Dialog showMessageBox(Context context, String message, final DialogInterface.OnClickListener onClickListener) {
        Dialog dialog = new AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton("Ok", onClickListener)
                .create();

        dialog.show();
        return dialog;
    }

    /**
     * Pops up a question dialog.
     * @param context is the context.
     * @param question is the question text.
     * @param positiveButtonText is the text of the positive button.
     * @param negativeButtonText is the text of the negative button.
     * @param onPositiveListener is the listener for positive button.
     * @param onNegativeListener is the listener for negative button.
     * @return
     */
    public static Dialog showQuestionDialog(Context context, String question, String positiveButtonText, String negativeButtonText, final DialogInterface.OnClickListener onPositiveListener, final DialogInterface.OnClickListener onNegativeListener) {
        Dialog dialog = new AlertDialog.Builder(context)
                .setMessage(question)
                .setPositiveButton(positiveButtonText, onPositiveListener)
                .setNegativeButton(negativeButtonText, onNegativeListener)
                .create();

        dialog.show();
        return dialog;
    }

    /**
     * Shows a progress dialog with the given message and a Cancel button.
     * @param context The context the dialog is shown in.
     * @param message The message shown on the dialog.
     * @param onClickListener Listener to handle the Cancel button event.
     * @return The ProgressDialog instance.
     */
    public static ProgressDialog showProgressDialog(Context context, String message, final DialogInterface.OnClickListener onClickListener) {
        Log.i(TAG, "showProgressDialog - " + message);

        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage(message);
        dialog.setCancelable(false);
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", onClickListener);
        dialog.show();
        return dialog;
    }

    /**
     * Shows a progress dialog with the given message and a Cancel button.
     * @param context The context the dialog is shown in.
     * @param message The message shown on the dialog.
     * @param onClickListener Listener to handle the Cancel button event.
     * @param onDismissListener Listener to handle the dismiss event.
     * @return The ProgressDialog instance.
     */
    public static ProgressDialog showProgressDialog(Context context, String message, final DialogInterface.OnClickListener onClickListener, final DialogInterface.OnDismissListener onDismissListener) {
        Log.i(TAG, "showProgressDialog - " + message);

        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage(message);
        dialog.setCancelable(false);
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", onClickListener);
        dialog.setOnDismissListener(onDismissListener);
        dialog.show();
        return dialog;
    }
}
