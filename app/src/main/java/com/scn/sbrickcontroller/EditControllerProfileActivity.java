package com.scn.sbrickcontroller;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.scn.sbrickcontrollerprofilemanager.ControllerAction;
import com.scn.sbrickcontrollerprofilemanager.ControllerProfile;
import com.scn.sbrickcontrollerprofilemanager.ControllerProfileManagerHolder;

import java.util.Set;

public class EditControllerProfileActivity extends BaseActivity {

    //
    // Private members
    //

    private static final String TAG = EditControllerProfileActivity.class.getSimpleName();
    private static final String CONTROLLER_PROFILE_KEY = "CONTROLLER_PROFILE_KEY";

    private ListView lwControllerActions;
    private ControllerActionListAdapter controllerActionListAdapter;

    private ControllerProfile profile;

    //
    // Activity overrides
    //

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate...");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_controller_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState != null) {
            Log.i(TAG, "  saved instance...");

            profile = savedInstanceState.getParcelable(CONTROLLER_PROFILE_KEY);
        }
        else {
            Log.i(TAG, "  new instance...");

            Intent intent = getIntent();
            profile = intent.getParcelableExtra(Constants.EXTRA_CONTROLLER_PROFILE);

            if (profile == null) {
                Log.i(TAG, "  new profile.");
                profile = new ControllerProfile();
            }
        }

        lwControllerActions = (ListView)findViewById(R.id.listview_conroller_actions);
        controllerActionListAdapter = new ControllerActionListAdapter(this, profile);
        lwControllerActions.setAdapter(controllerActionListAdapter);
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

        outState.putParcelable(CONTROLLER_PROFILE_KEY, profile);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "onCreateOptionsMenu...");

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_edit_controller_profile, menu);

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

                boolean newProfile = !ControllerProfileManagerHolder.getManager().isProfileNameUsed(profile.getName());
                String newProfileName = controllerActionListAdapter.getNewProfileName();

                if (newProfileName.length() == 0) {
                    Helper.showMessageBox(this, "Profile name can't be empty.", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                }
                else if (newProfile && ControllerProfileManagerHolder.getManager().isProfileNameUsed(newProfileName)) {
                    Helper.showMessageBox(this, "Profile name already exists.", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                }
                else {
                    Log.i(TAG, "  add or update the profile");

                    ControllerProfileManagerHolder.getManager().addOrUpdateProfile(profile, newProfileName);

                    EditControllerProfileActivity.this.finish();
                }

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult...");
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Log.i(TAG, "  RESULT_OK");

            String controllerActionId = data.getStringExtra(Constants.EXTRA_CONTROLLER_ACTION_ID);
            ControllerAction newControllerAction = data.getParcelableExtra(Constants.EXTRA_CONTROLLER_ACTION);
            ControllerAction originalControllerAction = data.getParcelableExtra(Constants.EXTRA_ORIGINAL_CONTROLLER_ACTION);

            if (originalControllerAction != null)
                profile.updateControllerAction(controllerActionId, originalControllerAction, newControllerAction);
            else
                profile.addControllerAction(controllerActionId, newControllerAction);

            controllerActionListAdapter.notifyDataSetChanged();
        }
    }

    //
    // API
    //

    public void startEditControllerActionActivity(String controllerActionId, ControllerAction controllerAction) {
        Log.i(TAG, "startEditControllerActionActivity...");

        Intent intent = new Intent(this, EditControllerActionActivity.class);
        intent.putExtra(Constants.EXTRA_CONTROLLER_ACTION_ID, controllerActionId);
        if (controllerAction != null)
            intent.putExtra(Constants.EXTRA_ORIGINAL_CONTROLLER_ACTION, controllerAction);
        startActivityForResult(intent, Constants.REQUEST_EDIT_CONTROLLER_ACTION);
    }

    //
    // Private methods and classes
    //

    private static class ControllerActionListAdapter extends BaseAdapter {

        //
        // Private members
        //

        private static final int ViewTypeProfileName = 0;
        private static final int ViewTypeControllerAction = 1;

        private EditControllerProfileActivity context;
        private ControllerProfile profile;
        private String newProfileName;

        //
        // Constructor
        //

        public ControllerActionListAdapter(EditControllerProfileActivity context, ControllerProfile profile) {
            this.context = context;
            this.profile = profile;
            this.newProfileName = profile.getName();
        }

        //
        // BaseAdapter overrides
        //

        @Override
        public int getViewTypeCount() {
            // profile name and controller actions
            return 2;
        }

        @Override
        public int getCount() {
            // profile name + 18 controller action
            return 19;
        }

        @Override
        public int getItemViewType(int position) {
            // profile name: 0
            // controller actions: 1
            return position == 0 ? ViewTypeProfileName : ViewTypeControllerAction;
        }

        @Override
        public Object getItem(int position) {
            if (position == 0) {
                // Profile name
                return profile.getName();
            }
            else {
                // Controller action
                String controllerActionId = getControllerActionId(position);
                Set<ControllerAction> controllerActions = profile.getControllerActions(controllerActionId);
                return controllerActions;
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            View rowView = convertView;
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            switch (getItemViewType(position)) {

                case ViewTypeProfileName:

                    if (rowView == null)
                        rowView = inflater.inflate(R.layout.listview_item_controller_action_head, parent, false);

                    // Edit profile name
                    EditText etProfileName = (EditText)rowView.findViewById(R.id.edittext_controller_profile_name);
                    etProfileName.setText(profile.getName());
                    etProfileName.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        }
                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            newProfileName = s.toString();
                        }
                        @Override
                        public void afterTextChanged(Editable s) {
                        }
                    });

                    break;

                case ViewTypeControllerAction:

                    if (rowView == null)
                        rowView = inflater.inflate(R.layout.listview_item_controller_actions, parent, false);

                    final String controllerActionName = ControllerProfile.getControllerActionName(getControllerActionId(position));
                    final Set<ControllerAction> controllerActions = (Set<ControllerAction>)getItem(position);

                    // Set controller action name
                    TextView twControllerActionName = (TextView)rowView.findViewById(R.id.textview_controller_action_name);
                    twControllerActionName.setText(controllerActionName);

                    // Add controller action button
                    ImageButton btnAddControllerAction = (ImageButton)rowView.findViewById(R.id.button_add_controller_action);
                    btnAddControllerAction.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.i(TAG, "btnAddControllerAction.onClick...");
                            context.startEditControllerActionActivity(getControllerActionId(position), null);
                        }
                    });

                    // Setup the controller actions
                    LinearLayout llControllerActions = (LinearLayout)rowView.findViewById(R.id.linearlayout_controller_actions);
                    llControllerActions.removeAllViews();

                    for (final ControllerAction controllerAction : controllerActions) {

                        View controllerActionView = inflater.inflate(R.layout.item_controller_action, llControllerActions, false);
                        llControllerActions.addView(controllerActionView);

                        // Controller action text
                        TextView twControllerAction = (TextView)controllerActionView.findViewById(R.id.textview_controller_action);
                        twControllerAction.setText(controllerAction.toString());

                        // Edit controller action
                        ImageButton btnEditControllerAction = (ImageButton)controllerActionView.findViewById(R.id.button_edit_controller_action);
                        btnEditControllerAction.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Log.i(TAG, "btnEditControllerAction.onClick...");
                                context.startEditControllerActionActivity(getControllerActionId(position), controllerAction);
                            }
                        });

                        // Remove controller action
                        ImageButton btnRemoveControllerAction = (ImageButton)controllerActionView.findViewById(R.id.button_remove_controller_action);
                        btnRemoveControllerAction.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Log.i(TAG, "btnRemoveControllerAction.onClick...");

                                Helper.showQuestionDialog(
                                        context,
                                        "Do you really want to delete this action?",
                                        "Yes",
                                        "No",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Log.i(TAG, "onClick...");
                                                profile.removeControllerAction(getControllerActionId(position), controllerAction);
                                                ControllerActionListAdapter.this.notifyDataSetChanged();
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
                    }

                    break;
            }

            return rowView;
        }

        //
        // API
        //

        public String getNewProfileName() { return newProfileName; }

        public static String getControllerActionId(int position) {
            if (position == 0)
                return "";

            switch (position - 1) {
                case 0: return ControllerProfile.CONTROLLER_ACTION_DPAD_LEFT_RIGHT;
                case 1: return ControllerProfile.CONTROLLER_ACTION_DPAD_UP_DOWN;
                case 2: return ControllerProfile.CONTROLLER_ACTION_AXIS_X;
                case 3: return ControllerProfile.CONTROLLER_ACTION_AXIS_Y;
                case 4: return ControllerProfile.CONTROLLER_ACTION_THUMB_L;
                case 5: return ControllerProfile.CONTROLLER_ACTION_AXIS_Z;
                case 6: return ControllerProfile.CONTROLLER_ACTION_AXIS_RZ;
                case 7: return ControllerProfile.CONTROLLER_ACTION_THUMB_R;
                case 8: return ControllerProfile.CONTROLLER_ACTION_A;
                case 9: return ControllerProfile.CONTROLLER_ACTION_B;
                case 10: return ControllerProfile.CONTROLLER_ACTION_X;
                case 11: return ControllerProfile.CONTROLLER_ACTION_Y;
                case 12: return ControllerProfile.CONTROLLER_ACTION_R1;
                case 13: return ControllerProfile.CONTROLLER_ACTION_R_TRIGGER;
                case 14: return ControllerProfile.CONTROLLER_ACTION_L1;
                case 15: return ControllerProfile.CONTROLLER_ACTION_L_TRIGGER;
                case 16: return ControllerProfile.CONTROLLER_ACTION_START;
                case 17: return ControllerProfile.CONTROLLER_ACTION_SELECT;
            }
            return "";
        }
    }
}
