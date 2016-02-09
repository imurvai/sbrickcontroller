package com.scn.sbrickcontroller;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.scn.sbrickcontrollerprofilemanager.SBrickControllerProfile;
import com.scn.sbrickcontrollerprofilemanager.SBrickControllerProfileManagerHolder;
import com.scn.sbrickmanager.SBrick;
import com.scn.sbrickmanager.SBrickManagerHolder;

import java.util.ArrayList;
import java.util.List;

public class EditControllerActionActivity extends BaseActivity {

    //
    // Private members
    //

    private static final String TAG = EditControllerActionActivity.class.getSimpleName();
    private static final String REQUEST_CODE_KEY = "REQUEST_CODE_KEY";
    private static final String CONTROLLER_ACTION_ID_KEY = "CONTROLLER_ACTION_ID_KEY";
    private static final String CONTROLLER_ACTION_KEY = "CONTROLLER_ACTION_KEY";

    private int requestCode;
    private String controllerActionId;
    private SBrickControllerProfile.ControllerAction controllerAction;

    //
    // Activity overrides
    //

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate...");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_controller_action);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final List<SBrick> sbricks = SBrickManagerHolder.getManager().getSBricks();

        if (savedInstanceState != null) {
            Log.i(TAG, "  saved instance...");

            requestCode = savedInstanceState.getInt(REQUEST_CODE_KEY);
            controllerActionId = savedInstanceState.getString(CONTROLLER_ACTION_ID_KEY);
            controllerAction = savedInstanceState.getParcelable(CONTROLLER_ACTION_KEY);
        }
        else {
            Log.i(TAG, "  new instance...");

            Intent intent = getIntent();
            requestCode = intent.getIntExtra(Constants.EXTRA_REQUEST_CODE, 0);
            controllerActionId = intent.getStringExtra(Constants.EXTRA_CONTROLLER_ACTION_ID);
            if (requestCode == Constants.REQUEST_NEW_CONTROLLER_ACTION) {
                Log.i(TAG, "  REQUEST_NEW_CONTROLLER_ACTION");
                controllerAction = new SBrickControllerProfile.ControllerAction(sbricks.get(0).getAddress(), 0, false);
            }
            else if (requestCode == Constants.REQUEST_EDIT_CONTROLLER_ACTION) {
                Log.i(TAG, "  REQUEST_EDIT_CONTROLLER_ACTION");
                controllerAction = intent.getParcelableExtra(Constants.EXTRA_CONTROLLER_ACTION);
            }
        }

        TextView twControllerActionName = (TextView)findViewById(R.id.textview_controller_action_name);
        twControllerActionName.setText(SBrickControllerProfile.getControllerActionName(controllerActionId));

        Spinner spSelectSBrick = (Spinner)findViewById(R.id.spinner_select_sbrick);
        spSelectSBrick.setAdapter(new ArrayAdapter<SBrick>(this, android.R.layout.simple_list_item_1, sbricks));
        spSelectSBrick.setSelection(sbricks.indexOf(SBrickManagerHolder.getManager().getSBrick(controllerAction.getSBrickAddress())));
        spSelectSBrick.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "sbSelectSBrick.onItemClick...");
                controllerAction.setSBrickAddress(sbricks.get(position).getAddress());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.i(TAG, "sbSelectSBrick.onNothingSelected...");
                // Do nothing here
            }
        });

        Spinner spSelectChannel = (Spinner)findViewById(R.id.spinnel_select_channel);
        spSelectChannel.setAdapter(new ArrayAdapter<Integer>(this, android.R.layout.simple_list_item_1, new Integer[] { 1, 2, 3, 4 }));
        spSelectChannel.setSelection(controllerAction.getChannel());
        spSelectChannel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "spSelectChannel.onItemSelected...");
                controllerAction.setChannel(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.i(TAG, "spSelectChannel.onNothingSelected...");
                // Do nothing here
            }
        });

        Switch swInvertChannel = (Switch)findViewById(R.id.switch_invert_channel);
        swInvertChannel.setChecked(controllerAction.getInvert());
        swInvertChannel.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i(TAG, "onCheckedChanged...");
                controllerAction.setInvert(isChecked);
            }
        });
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume...");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause...");
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.i(TAG, "onSaveInstanceState...");
        super.onSaveInstanceState(outState);

        outState.putInt(REQUEST_CODE_KEY, requestCode);
        outState.putString(CONTROLLER_ACTION_ID_KEY, controllerActionId);
        outState.putParcelable(CONTROLLER_ACTION_KEY, controllerAction);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "onCreateOptionsMenu...");

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_edit_controller_action, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "onOptionsItemSelected...");
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {

            case android.R.id.home:
                Log.i(TAG, "  home");

                NavUtils.navigateUpFromSameTask(this);
                return true;

            case R.id.menu_item_done:
                Log.i(TAG, "  menu_item_done");

                Intent intent = new Intent();
                intent.putExtra(Constants.EXTRA_CONTROLLER_ACTION_ID, controllerActionId);
                intent.putExtra(Constants.EXTRA_CONTROLLER_ACTION, controllerAction);
                EditControllerActionActivity.this.setResult(RESULT_OK, intent);

                EditControllerActionActivity.this.finish();
                return true;
        }

        return false;
    }
}
