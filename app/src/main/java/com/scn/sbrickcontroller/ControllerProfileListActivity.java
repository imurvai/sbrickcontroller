package com.scn.sbrickcontroller;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
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
    private Button buttonAddControllerProfile;

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

                // Open the controller fragment
                List<SBrickControllerProfile> profiles = new ArrayList<SBrickControllerProfile>(SBrickControllerProfileManagerHolder.getManager().getProfiles());
                SBrickControllerProfile profile = profiles.get(position);

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

        buttonAddControllerProfile = (Button)findViewById(R.id.button_add_controller_profile);
        buttonAddControllerProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick...");

                SBrickControllerProfile profile = SBrickControllerProfileManagerHolder.getManager().addProfile("My profile");
                int profileIndex = SBrickControllerProfileManagerHolder.getManager().getProfiles().indexOf(profile);

                Intent intent = new Intent(ControllerProfileListActivity.this, EditControllerProfileActivity.class);
                intent.putExtra(Constants.EXTRA_CONTROLLER_PROFILE_INDEX, profileIndex);
                intent.putExtra(Constants.EXTRA_CONTROLLER_PROFILE, profile);
                startActivity(intent);
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

        private Context context;

        ControllerProfileListAdapter(Context context) {
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

            Button btnEditProfile = (Button)rowView.findViewById(R.id.button_edit_controller_profile);
            btnEditProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "onClick...");

                    int profileIndex = SBrickControllerProfileManagerHolder.getManager().getProfiles().indexOf(profile);

                    Intent intent = new Intent(context, EditControllerProfileActivity.class);
                    intent.putExtra(Constants.EXTRA_CONTROLLER_PROFILE_INDEX, profileIndex);
                    intent.putExtra(Constants.EXTRA_CONTROLLER_PROFILE, profile);
                    context.startActivity(intent);
                }
            });

            Button btnDeleteProfile = (Button)rowView.findViewById(R.id.button_delete_controller_profile);
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
                                    Log.i(TAG, "onClick...");
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
