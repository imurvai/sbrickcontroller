package com.scn.sbrickcontroller;

import android.app.Application;

import com.scn.sbrickcontrollerprofilemanager.SBrickControllerProfileManagerHolder;
import com.scn.sbrickmanager.SBrickManager;
import com.scn.sbrickmanager.SBrickManagerHolder;

/**
 * Created by Istvan_Murvai on 2015-02-17.
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        SBrickManagerHolder.CreateSBrickManagerSingleton(this);
        SBrickManagerHolder.getSBrickManager().loadSBricks();
        SBrickControllerProfileManagerHolder.getManager().loadProfiles();
    }
}
