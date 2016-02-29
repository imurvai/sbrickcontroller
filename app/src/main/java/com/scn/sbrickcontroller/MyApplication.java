package com.scn.sbrickcontroller;

import android.app.Application;
import android.util.Log;

import com.scn.sbrickcontrollerprofilemanager.ControllerConfigManagerHolder;
import com.scn.sbrickcontrollerprofilemanager.ControllerProfileManagerHolder;
import com.scn.sbrickmanager.SBrickManagerHolder;

/**
 * Created by Istvan_Murvai on 2015-02-17.
 */
public class MyApplication extends Application {

    //
    // Private members
    //

    public static final String TAG = MyApplication.class.getSimpleName();

    //
    // Application overrides
    //

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate...");
        super.onCreate();

        SBrickManagerHolder.CreateSBrickManagerSingleton(this);
        SBrickManagerHolder.getManager().loadSBricks();

        ControllerProfileManagerHolder.createControllerProfileManager(this);
        ControllerProfileManagerHolder.getManager().loadProfiles();

        ControllerConfigManagerHolder.createControllerConfigManager(this);
        ControllerConfigManagerHolder.getManager().loadConfig();
    }
}
