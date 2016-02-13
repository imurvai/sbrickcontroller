package com.scn.sbrickcontroller;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.RadioButton;
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
    private static final String CONTROLLER_ACTION_ID_KEY = "CONTROLLER_ACTION_ID_KEY";
    private static final String CONTROLLER_ACTION_KEY = "CONTROLLER_ACTION_KEY";

    private RadioButton rbChannel0;
    private RadioButton rbChannel1;
    private RadioButton rbChannel2;
    private RadioButton rbChannel3;

    private String controllerActionId;

    private String selectedSBrickAddress;
    private int selectedChannel;
    private boolean selecedInvert;

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
        SBrickControllerProfile.ControllerAction controllerAction;

        if (savedInstanceState != null) {
            Log.i(TAG, "  saved instance...");

            controllerActionId = savedInstanceState.getString(CONTROLLER_ACTION_ID_KEY);
            controllerAction = savedInstanceState.getParcelable(CONTROLLER_ACTION_KEY);
        }
        else {
            Log.i(TAG, "  new instance...");

            Intent intent = getIntent();
            controllerActionId = intent.getStringExtra(Constants.EXTRA_CONTROLLER_ACTION_ID);
            controllerAction = intent.getParcelableExtra(Constants.EXTRA_CONTROLLER_ACTION);

            if (controllerAction == null) {
                Log.i(TAG, "  new controller action.");
                controllerAction = new SBrickControllerProfile.ControllerAction(sbricks.get(0).getAddress(), 0, false);
            }
        }

        selectedSBrickAddress = controllerAction.getSBrickAddress();
        selectedChannel = controllerAction.getChannel();
        selecedInvert = controllerAction.getInvert();

        TextView twControllerActionName = (TextView)findViewById(R.id.textview_controller_action_name);
        twControllerActionName.setText(SBrickControllerProfile.getControllerActionName(controllerActionId));

        Spinner spSelectSBrick = (Spinner)findViewById(R.id.spinner_select_sbrick);
        spSelectSBrick.setAdapter(new ArrayAdapter<SBrick>(this, android.R.layout.simple_list_item_1, sbricks));
        spSelectSBrick.setSelection(sbricks.indexOf(SBrickManagerHolder.getManager().getSBrick(selectedSBrickAddress)));
        spSelectSBrick.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "sbSelectSBrick.onItemClick...");
                selectedSBrickAddress = sbricks.get(position).getAddress();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.i(TAG, "sbSelectSBrick.onNothingSelected...");
                // Do nothing here
            }
        });

        Switch swInvertChannel = (Switch)findViewById(R.id.switch_invert_channel);
        swInvertChannel.setChecked(selecedInvert);
        swInvertChannel.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i(TAG, "onCheckedChanged...");
                selecedInvert = isChecked;
            }
        });

        rbChannel0 = (RadioButton)findViewById(R.id.radiobutton_channel_0);
        rbChannel1 = (RadioButton)findViewById(R.id.radiobutton_channel_1);
        rbChannel2 = (RadioButton)findViewById(R.id.radiobutton_channel_2);
        rbChannel3 = (RadioButton)findViewById(R.id.radiobutton_channel_3);

        rbChannel0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "rbChannel0.onClick...");
                setSelectedChannel(0);
            }
        });

        rbChannel1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "rbChannel1.onClick...");
                setSelectedChannel(1);
            }
        });

        rbChannel2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "rbChannel2.onClick...");
                setSelectedChannel(2);
            }
        });

        rbChannel3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "rbChannel3.onClick...");
                setSelectedChannel(3);
            }
        });

        setSelectedChannel(selectedChannel);
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

        SBrickControllerProfile.ControllerAction controllerAction = new SBrickControllerProfile.ControllerAction(selectedSBrickAddress, selectedChannel, selecedInvert);

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

        switch (item.getItemId()) {

            case android.R.id.home:
                Log.i(TAG, "  home");

                NavUtils.navigateUpFromSameTask(this);
                return true;

            case R.id.menu_item_done:
                Log.i(TAG, "  menu_item_done");

                SBrickControllerProfile.ControllerAction controllerAction = new SBrickControllerProfile.ControllerAction(selectedSBrickAddress, selectedChannel, selecedInvert);

                Intent intent = new Intent();
                intent.putExtra(Constants.EXTRA_CONTROLLER_ACTION_ID, controllerActionId);
                intent.putExtra(Constants.EXTRA_CONTROLLER_ACTION, controllerAction);
                EditControllerActionActivity.this.setResult(RESULT_OK, intent);

                EditControllerActionActivity.this.finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //
    // Private methods
    //

    private void setSelectedChannel(int selectedChannel) {
        Log.i(TAG, "setSelectedChannel - " + selectedChannel);

        rbChannel0.setChecked(selectedChannel == 0);
        rbChannel1.setChecked(selectedChannel == 1);
        rbChannel2.setChecked(selectedChannel == 2);
        rbChannel3.setChecked(selectedChannel == 3);
        this.selectedChannel = selectedChannel;
    }
}
