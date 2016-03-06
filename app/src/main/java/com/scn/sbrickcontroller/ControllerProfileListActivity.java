package com.scn.sbrickcontroller;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.scn.sbrickcontrollerprofilemanager.ControllerProfile;
import com.scn.sbrickcontrollerprofilemanager.ControllerProfileManagerHolder;
import com.scn.sbrickmanager.SBrickManagerHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ControllerProfileListActivity extends BaseActivity {

    //
    // Private members
    //

    private static final String TAG = ControllerProfileListActivity.class.getSimpleName();

    private ControllerProfileListAdapter controllerProfileListAdapter;
    private ListView listViewControllerProfiles;
    private MenuItem miPlay;

    //
    // Fragment overrides
    //

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate...");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_controller_profile_list);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        listViewControllerProfiles = (ListView)findViewById(R.id.listView_controller_profiles);
        listViewControllerProfiles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "onItemClick...");

                ControllerProfile profile = (ControllerProfile)controllerProfileListAdapter.getItem(position);

                // Check if the profile contains controller actions
                if (profile.getSBrickAddresses().size() == 0) {
                    Helper.showMessageBox(ControllerProfileListActivity.this, "Please add controller actions to the profile first.", null);
                    return;
                }

                // Check if the SBricks are known in the profile
                if (!validateProfile(profile)) {
                    Helper.showMessageBox(ControllerProfileListActivity.this, "Some of the SBricks in this profile is unknown. Please do a scan and edit the profile.", null);
                    return;
                }

                // Open the controller activity
                Intent intent = new Intent(ControllerProfileListActivity.this, ControllerActivity.class);
                intent.putParcelableArrayListExtra(Constants.EXTRA_CONTROLLER_PROFILES, new ArrayList(Arrays.asList(profile)));
                startActivity(intent);
            }
        });
        controllerProfileListAdapter = new ControllerProfileListAdapter(this);
        listViewControllerProfiles.setAdapter(controllerProfileListAdapter);
        listViewControllerProfiles.setEmptyView(findViewById(R.id.textview_empty_profiles));
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume...");
        super.onResume();

        controllerProfileListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause...");
        super.onPause();

        if (!ControllerProfileManagerHolder.getManager().saveProfiles()) {
            Helper.showMessageBox(this, "Could not save controller profiles.", null);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "onCreateOptionsMenu...");

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_controller_profile_list, menu);

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

            case R.id.menu_item_add:
                Log.i(TAG, "  menu_item_add");

                Intent intent = new Intent(this, EditControllerProfileActivity.class);
                startActivity(intent);
                return true;

            case R.id.menu_item_play:
                Log.i(TAG, "  menu_item_play");

                ArrayList<ControllerProfile> selectedProfiles = controllerProfileListAdapter.getSelectedProfiles();
                if (selectedProfiles.size() > 0) {

                    // Check if the selected profiles have controller actions and all are valid
                    boolean profilesHaveSBricks = false;
                    boolean profilesValid = true;
                    for (ControllerProfile profile : selectedProfiles) {

                        if (profile.getSBrickAddresses().size() > 0)
                            profilesHaveSBricks = true;

                        if (!validateProfile(profile))
                            profilesValid = false;
                    }

                    if (!profilesHaveSBricks) {
                        Helper.showMessageBox(this, "Please add controller actions to the profile(s) first.", null);
                    }
                    else if (!profilesValid) {
                        Helper.showMessageBox(this, "Some of the profiles contain unknown SBrick(s), please do a scan.", null);
                    }
                    else {
                        Intent intent2 = new Intent(this, ControllerActivity.class);
                        intent2.putParcelableArrayListExtra(Constants.EXTRA_CONTROLLER_PROFILES, controllerProfileListAdapter.getSelectedProfiles());
                        startActivity(intent2);
                    }
                }
                else {
                    Helper.showMessageBox(this, "Please select profiles first to play with.", null);
                }

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //
    // Private methods and classes
    //

    private boolean validateProfile(ControllerProfile profile) {
        Log.i(TAG, "validateProfile - " + profile.getName());

        boolean allSBrickOk = true;
        for (String sbrickAddress : profile.getSBrickAddresses()) {
            if (SBrickManagerHolder.getManager().getSBrick(sbrickAddress) == null) {
                Log.i(TAG, "  SBrick (" + sbrickAddress + ") is unknown.");
                allSBrickOk = false;
            }
        }

        return allSBrickOk;
    }

    //

    private static class ControllerProfileListAdapter extends BaseAdapter {

        private ControllerProfileListActivity activity;
        private Map<String, Boolean> profileSelectionMap = new HashMap<>();

        //
        // Constructor
        //

        ControllerProfileListAdapter(ControllerProfileListActivity context) {
            this.activity = context;
        }

        //
        // BaseAdapter overrides
        //

        @Override
        public int getCount() {
            return ControllerProfileManagerHolder.getManager().getProfiles().size();
        }

        @Override
        public Object getItem(int position) {
            return ControllerProfileManagerHolder.getManager().getProfiles().get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView = convertView;

            if (rowView == null) {
                LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowView = inflater.inflate(R.layout.listview_item_controller_profile, parent, false);
            }

            final ControllerProfile profile = (ControllerProfile)getItem(position);

            CheckBox cbProfileSelection = (CheckBox)rowView.findViewById(R.id.checkbox_select_controller_profile);
            cbProfileSelection.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Log.i(TAG, "cbProfileSelection.onCheckedChanged...");
                    profileSelectionMap.put(profile.getName(), isChecked);
                }
            });

            TextView twControllerProfileName = (TextView)rowView.findViewById(R.id.textview_controller_profile_name);
            twControllerProfileName.setText(profile.getName());

            ImageButton btnEditProfile = (ImageButton)rowView.findViewById(R.id.button_edit_controller_profile);
            btnEditProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "btnEditProfile.onClick...");

                    Intent intent = new Intent(activity, EditControllerProfileActivity.class);
                    intent.putExtra(Constants.EXTRA_CONTROLLER_PROFILE, profile);
                    activity.startActivity(intent);
                }
            });

            ImageButton btnDeleteProfile = (ImageButton)rowView.findViewById(R.id.button_delete_controller_profile);
            btnDeleteProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "btnDeleteProfile.onClick...");

                    Helper.showQuestionDialog(
                            activity,
                            "Do you really want to remove this profile?",
                            "Yes",
                            "No",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Log.i(TAG, "onClick...");
                                    ControllerProfileManagerHolder.getManager().removeProfile(profile.getName());
                                    ControllerProfileListAdapter.this.notifyDataSetChanged();
                                }
                            },
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Do nothing here
                                }
                            });
                }
            });

            return rowView;
        }

        //
        // API
        //

        public ArrayList<ControllerProfile> getSelectedProfiles() {
            Log.i(TAG, "getSelectedProfiles...");

            ArrayList<ControllerProfile> selectedProfiles = new ArrayList<>();

            for (ControllerProfile profile : ControllerProfileManagerHolder.getManager().getProfiles()) {
                String profileName = profile.getName();
                if (profileSelectionMap.containsKey(profileName) && profileSelectionMap.get(profileName)) {
                    selectedProfiles.add(profile);
                }
            }

            return selectedProfiles;
        }
    }
}
