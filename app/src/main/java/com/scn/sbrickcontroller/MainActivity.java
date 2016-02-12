package com.scn.sbrickcontroller;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.scn.sbrickcontrollerprofilemanager.SBrickControllerProfile;
import com.scn.sbrickcontrollerprofilemanager.SBrickControllerProfileManagerHolder;
import com.scn.sbrickmanager.SBrickManagerHolder;

import java.util.List;

/**
 * The one and only activity.
 */
public class MainActivity extends BaseActivity {

    //
    // Private members
    //

    private final String TAG = MainActivity.class.getSimpleName();

    private final int REQUEST_ENABLE_BLUETOOTH = 0x1234;

    //
    // Activity overrides
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate...");
        super.onCreate(savedInstanceState);

        if (!SBrickManagerHolder.getManager().isBLESupported()) {
            Helper.showMessageBox(this, "Your device doesn't support bluetooth low energy profile.", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    MainActivity.this.finish();
                }
            });
            return;
        }

        setContentView(R.layout.activity_main);

        Button btnManageSBricks = (Button)findViewById(R.id.button_scan_sbricks);
        btnManageSBricks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "btnManageSBricks.onClick...");
                startActivity(new Intent(MainActivity.this, SBrickListActivity.class));
            }
        });

        Button btnControllerProfiles = (Button)findViewById(R.id.button_controller_profiles);
        btnControllerProfiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "btnControllerControllerProfiles.onClick...");
                startActivity(new Intent(MainActivity.this, ControllerProfileListActivity.class));
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult...");

        switch (requestCode) {
            case REQUEST_ENABLE_BLUETOOTH:
                Log.i(TAG, "  REQUEST_ENABLE_BLUETOOTH");
                if (resultCode == RESULT_OK)
                    Log.i(TAG, "  RESULT_OK");
                else {
                    Log.i(TAG, "  RESULT_CANCEL");
                    finish();
                }
                break;

            default:
                Log.i(TAG, "  Unknown request");
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "onCreateOptionsMenu...");

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "onOptionsItemSelected...");

        switch (item.getItemId()) {

            case R.id.menu_item_about:

                String versionName = "";
                try {
                    versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
                } catch (Exception ex) { }

                String message = "Version " + versionName + "\n\n2016 SCN";

                Helper.showMessageBox(this, "SBrickController", message, R.drawable.ic_launcher, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //
    // Private methods
    //

}
