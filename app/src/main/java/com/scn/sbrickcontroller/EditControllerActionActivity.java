package com.scn.sbrickcontroller;

import android.content.Intent;
import android.os.Bundle;
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

import java.util.List;

public class EditControllerActionActivity extends BaseActivity {

    //
    // Private members
    //

    private static final String TAG = EditControllerActionActivity.class.getSimpleName();

    private int requestCode;
    private String controllerActionId;
    private SBrickControllerProfile.ControllerAction controllerAction;
    private List<String> sbrickAddresses;

    //
    // Activity overrides
    //

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate...");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_controller_action);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        requestCode = intent.getIntExtra(Constants.EXTRA_REQUEST_CODE, 0);
        controllerActionId = intent.getStringExtra(Constants.EXTRA_CONTROLLER_ACTION_ID);
        sbrickAddresses = intent.getStringArrayListExtra(Constants.EXTRA_SBRICK_ADDRESS_LIST);
        if (requestCode == Constants.REQUEST_NEW_CONTROLLER_ACTION) {
            Log.i(TAG, "  REQUEST_NEW_CONTROLLER_ACTION");
            controllerAction = new SBrickControllerProfile.ControllerAction(sbrickAddresses.get(0), 0, false);
        }
        else if (requestCode == Constants.REQUEST_EDIT_CONTROLLER_ACTION) {
            Log.i(TAG, "  REQUEST_EDIT_CONTROLLER_ACTION");
            controllerAction = intent.getParcelableExtra(Constants.EXTRA_CONTROLLER_ACTION);
        }

        TextView twControllerActionName = (TextView)findViewById(R.id.textview_controller_action_name);
        twControllerActionName.setText(SBrickControllerProfile.getControllerActionName(controllerActionId));

        Spinner spSelectSBrick = (Spinner)findViewById(R.id.spinner_select_sbrick);
        spSelectSBrick.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, sbrickAddresses));
        spSelectSBrick.setSelection(sbrickAddresses.indexOf(controllerAction.getSBrickAddress()));
        spSelectSBrick.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "sbSelectSBrick.onItemClick...");
                controllerAction.setSBrickAddress(sbrickAddresses.get(position));
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
