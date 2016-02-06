package com.scn.sbrickcontroller;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.scn.sbrickcontrollerprofilemanager.SBrickControllerProfile;
import com.scn.sbrickcontrollerprofilemanager.SBrickControllerProfileManagerHolder;

import java.util.List;

public class EditControllerActionActivity extends BaseActivity {

    //
    // Private members
    //

    private static final String TAG = EditControllerActionActivity.class.getSimpleName();

    private SBrickControllerProfile profile;
    private String controllerActionId;
    private List<String> sbrickAddresses;

    private String selectedSBrickAddress;
    private int selectedChannel;
    private boolean selectedInvert;

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
        int profileIndex = intent.getIntExtra(Constants.EXTRA_CONTROLLER_PROFILE_INDEX, 0);
        profile = SBrickControllerProfileManagerHolder.getManager().getProfileAt(profileIndex);
        controllerActionId = intent.getStringExtra(Constants.EXTRA_CONTROLLER_ACTION_ID);
        sbrickAddresses = intent.getStringArrayListExtra(Constants.EXTRA_SBRICK_ADDRESS_LIST);

        TextView twControllerActionName = (TextView)findViewById(R.id.textview_controller_action_name);
        twControllerActionName.setText(SBrickControllerProfile.getControllerActionName(controllerActionId));

        SBrickControllerProfile.ControllerAction controllerAction = profile.getControllerAction(controllerActionId);
        if (controllerAction != null) {
            selectedSBrickAddress = controllerAction.getSbrickAddress();
            selectedChannel = controllerAction.getChannel();
            selectedInvert = controllerAction.getInvert();
        }
        else {
            selectedSBrickAddress = sbrickAddresses.get(0);
            selectedChannel = 0;
            selectedInvert = false;
        }

        Spinner spSelectSBrick = (Spinner)findViewById(R.id.spinner_select_sbrick);
        spSelectSBrick.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, sbrickAddresses));
        spSelectSBrick.setSelection(sbrickAddresses.indexOf(selectedSBrickAddress));
        spSelectSBrick.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "sbSelectSBrick.onItemClick...");
                selectedSBrickAddress = sbrickAddresses.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.i(TAG, "sbSelectSBrick.onNothingSelected...");
                // Do nothing here
            }
        });

        Spinner spSelectChannel = (Spinner)findViewById(R.id.spinnel_select_channel);
        spSelectChannel.setAdapter(new ArrayAdapter<Integer>(this, android.R.layout.simple_list_item_1, new Integer[] { 1, 2, 3, 4 }));
        spSelectChannel.setSelection(selectedChannel);
        spSelectChannel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "spSelectChannel.onItemSelected...");
                selectedChannel = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.i(TAG, "spSelectChannel.onNothingSelected...");
                // Do nothing here
            }
        });

        Switch swInvertChannel = (Switch)findViewById(R.id.switch_invert_channel);
        swInvertChannel.setChecked(selectedInvert);
        swInvertChannel.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i(TAG, "onCheckedChanged...");
                selectedInvert = isChecked;
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

                SBrickControllerProfile.ControllerAction newControllerAction = new SBrickControllerProfile.ControllerAction(selectedSBrickAddress, selectedChannel, selectedInvert);
                profile.setControllerAction(controllerActionId, newControllerAction);

                EditControllerActionActivity.this.finish();
                return true;
        }

        return false;
    }
}
