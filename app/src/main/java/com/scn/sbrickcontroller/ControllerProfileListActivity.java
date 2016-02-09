package com.scn.sbrickcontroller;

import android.app.Activity;
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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.scn.sbrickcontrollerprofilemanager.SBrickControllerProfile;
import com.scn.sbrickcontrollerprofilemanager.SBrickControllerProfileManagerHolder;
import com.scn.sbrickmanager.SBrickManagerHolder;

import java.util.ArrayList;
import java.util.List;

public class ControllerProfileListActivity extends BaseActivity {

    //
    // Private members
    //

    private static final String TAG = ControllerProfileListActivity.class.getSimpleName();

    private ControllerProfileListAdapter controllerProfileListAdapter;
    private ListView listViewControllerProfiles;

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

                // Open the controller activity
                SBrickControllerProfile profile = (SBrickControllerProfile)controllerProfileListAdapter.getItem(position);
                int profileIndex = SBrickControllerProfileManagerHolder.getManager().getProfiles().indexOf(profile);

                if (profile.getSBrickAddresses().size() == 0) {
                    Helper.showMessageBox(
                            ControllerProfileListActivity.this,
                            "Please add controller actions to the profile first.",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Do nothing here
                                }
                            });
                    return;
                }

                if (!validateProfile(profile)) {
                    Helper.showMessageBox(
                            ControllerProfileListActivity.this,
                            "Some of the SBricks in this profile is unknown. Please do a scan and edit the profile.",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Log.i(TAG, "onClick...");
                                    // Do nothing here.
                                }
                            });
                    return;
                }

                Intent intent = new Intent(ControllerProfileListActivity.this, ControllerActivity.class);
                intent.putExtra(Constants.EXTRA_CONTROLLER_PROFILE, profile);
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
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause...");
        super.onPause();

        if (!SBrickControllerProfileManagerHolder.getManager().saveProfiles()) {
            Helper.showMessageBox(
                    this,
                    "Could not save controller profiles.",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
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
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {

            case android.R.id.home:
                Log.i(TAG, "  home");

                NavUtils.navigateUpFromSameTask(this);
                return true;

            case R.id.menu_item_add:
                Log.i(TAG, "  menu_item_add");

                Intent intent = new Intent(ControllerProfileListActivity.this, EditControllerProfileActivity.class);
                intent.putExtra(Constants.EXTRA_REQUEST_CODE, Constants.REQUEST_NEW_CONTROLLER_PROFILE);
                startActivityForResult(intent, Constants.REQUEST_NEW_CONTROLLER_PROFILE);
                return true;
        }

        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onOptionsItemSelected...");
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Log.i(TAG, "  RESULT_OK");

            switch (requestCode) {
                case Constants.REQUEST_NEW_CONTROLLER_PROFILE:
                    Log.i(TAG, "  REQUEST_NEW_CONTROLLER_PROFILE");

                    SBrickControllerProfile profile = data.getParcelableExtra(Constants.EXTRA_CONTROLLER_PROFILE);
                    SBrickControllerProfileManagerHolder.getManager().addProfile(profile);

                    break;

                case Constants.REQUEST_EDIT_CONTROLLER_PROFILE:
                    Log.i(TAG, "  REQUEST_EDIT_CONTROLLER_PROFILE");

                    int profileIndex = data.getIntExtra(Constants.EXTRA_CONTROLLER_PROFILE_INDEX, 0);
                    SBrickControllerProfile profile2 = data.getParcelableExtra(Constants.EXTRA_CONTROLLER_PROFILE);
                    SBrickControllerProfileManagerHolder.getManager().updateProfileAt(profileIndex, profile2);

                    break;
            }

            controllerProfileListAdapter.notifyDataSetChanged();
        }
    }

    //
    // Private methods and classes
    //

    private boolean validateProfile(SBrickControllerProfile profile) {
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

        private Activity context;

        ControllerProfileListAdapter(Activity context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return SBrickControllerProfileManagerHolder.getManager().getProfiles().size();
        }

        @Override
        public Object getItem(int position) {
            return SBrickControllerProfileManagerHolder.getManager().getProfileAt(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView = convertView;

            if (rowView == null) {
                LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowView = inflater.inflate(R.layout.listview_item_controller_profile, parent, false);
            }

            final SBrickControllerProfile profile = (SBrickControllerProfile)getItem(position);

            TextView twControllerProfileName = (TextView)rowView.findViewById(R.id.textview_controller_profile_name);
            twControllerProfileName.setText(profile.getName());

            ImageButton btnEditProfile = (ImageButton)rowView.findViewById(R.id.button_edit_controller_profile);
            btnEditProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "onClick...");

                    int profileIndex = SBrickControllerProfileManagerHolder.getManager().getProfiles().indexOf(profile);

                    Intent intent = new Intent(context, EditControllerProfileActivity.class);
                    intent.putExtra(Constants.EXTRA_REQUEST_CODE, Constants.REQUEST_EDIT_CONTROLLER_PROFILE);
                    intent.putExtra(Constants.EXTRA_CONTROLLER_PROFILE_INDEX, profileIndex);
                    intent.putExtra(Constants.EXTRA_CONTROLLER_PROFILE, profile);
                    context.startActivityForResult(intent, Constants.REQUEST_EDIT_CONTROLLER_PROFILE);
                }
            });

            ImageButton btnDeleteProfile = (ImageButton)rowView.findViewById(R.id.button_delete_controller_profile);
            btnDeleteProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Helper.showQuestionDialog(
                            context,
                            "Do you really want to remove this profile?",
                            "Yes",
                            "No",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Log.i(TAG, "btnDeleteProfile.onClick...");
                                    SBrickControllerProfileManagerHolder.getManager().removeProfile(profile);
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
    }
}
