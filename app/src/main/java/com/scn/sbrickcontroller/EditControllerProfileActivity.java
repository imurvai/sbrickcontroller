package com.scn.sbrickcontroller;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
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
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.scn.sbrickcontrollerprofilemanager.SBrickControllerProfile;
import com.scn.sbrickmanager.SBrick;
import com.scn.sbrickmanager.SBrickManagerHolder;

import java.util.ArrayList;
import java.util.List;

public class EditControllerProfileActivity extends BaseActivity {

    //
    // Private members
    //

    private static final String TAG = EditControllerProfileActivity.class.getSimpleName();
    private static final String REQUEST_CODE_KEY = "REQUEST_CODE_KEY";
    private static final String CONTROLLER_PROFILE_INDEX_KEY = "CONTROLLER_PROFILE_INDEX_KEY";
    private static final String CONTROLLER_PROFILE_KEY = "CONTROLLER_PROFILE_KEY";

    private ListView lwControllerActions;
    private ControllerActionListAdapter controllerActionListAdapter;

    int requestCode;
    int profileIndex;
    SBrickControllerProfile profile;

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

            requestCode = savedInstanceState.getInt(REQUEST_CODE_KEY);
            profileIndex = savedInstanceState.getInt(CONTROLLER_PROFILE_INDEX_KEY);
            profile = savedInstanceState.getParcelable(CONTROLLER_PROFILE_KEY);
        }
        else {
            Log.i(TAG, "  new instance...");

            Intent intent = getIntent();
            requestCode = intent.getIntExtra(Constants.EXTRA_REQUEST_CODE, 0);
            if (requestCode == Constants.REQUEST_NEW_CONTROLLER_PROFILE) {
                Log.i(TAG, "  REQUEST_NEW_CONTROLLER_PROFILE");

                profileIndex = 0;
                profile = new SBrickControllerProfile("My profile");
            }
            else if (requestCode == Constants.REQUEST_EDIT_CONTROLLER_PROFILE) {
                Log.i(TAG, "  REQUEST_EDIT_CONTROLLER_PROFILE");

                profileIndex = intent.getIntExtra(Constants.EXTRA_CONTROLLER_PROFILE_INDEX, 0);
                profile = intent.getParcelableExtra(Constants.EXTRA_CONTROLLER_PROFILE);
            }
        }

        lwControllerActions = (ListView)findViewById(R.id.listview_conroller_actions);
        lwControllerActions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "onItemClick...");

                if (position == 0) {
                    Log.i(TAG, "  Click on head item.");
                    return;
                }

                String controllerActionId = controllerActionListAdapter.getControllerActionId(position);
                SBrickControllerProfile.ControllerAction controllerAction = (SBrickControllerProfile.ControllerAction) controllerActionListAdapter.getItem(position);
                List<String> sbrickAddresses = SBrickManagerHolder.getManager().getSBrickAddresses();

                if (controllerAction == null) {
                    Log.i(TAG, "  New controller action.");

                    Intent intent = new Intent(EditControllerProfileActivity.this, EditControllerActionActivity.class);
                    intent.putExtra(Constants.EXTRA_REQUEST_CODE, Constants.REQUEST_NEW_CONTROLLER_ACTION);
                    intent.putExtra(Constants.EXTRA_CONTROLLER_ACTION_ID, controllerActionId);
                    startActivityForResult(intent, Constants.REQUEST_NEW_CONTROLLER_ACTION);
                }
                else {
                    Log.i(TAG, "  Edit controller action.");

                    Intent intent = new Intent(EditControllerProfileActivity.this, EditControllerActionActivity.class);
                    intent.putExtra(Constants.EXTRA_REQUEST_CODE, Constants.REQUEST_EDIT_CONTROLLER_ACTION);
                    intent.putExtra(Constants.EXTRA_CONTROLLER_ACTION_ID, controllerActionId);
                    intent.putExtra(Constants.EXTRA_CONTROLLER_ACTION, controllerAction);
                    startActivityForResult(intent, Constants.REQUEST_EDIT_CONTROLLER_ACTION);
                }
            }
        });
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

        outState.putInt(REQUEST_CODE_KEY, requestCode);
        outState.putInt(CONTROLLER_PROFILE_INDEX_KEY, profileIndex);
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
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {

            case android.R.id.home:
                Log.i(TAG, "  home");

                NavUtils.navigateUpFromSameTask(this);
                return true;

            case R.id.menu_item_done:
                Log.i(TAG, "  menu_item_done");

                if (profile.getName().length() == 0) {
                    Helper.showMessageBox(this, "Profile name can't be empty.", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                }
                else {
                    // Give back the profile according to the request code
                    if (requestCode == Constants.REQUEST_NEW_CONTROLLER_PROFILE) {
                        Log.i(TAG, "  REQUEST_NEW_CONTROLLER_PROFILE");
                        Intent intent = new Intent();
                        intent.putExtra(Constants.EXTRA_CONTROLLER_PROFILE, profile);
                        EditControllerProfileActivity.this.setResult(RESULT_OK, intent);
                    }
                    else if (requestCode == Constants.REQUEST_EDIT_CONTROLLER_PROFILE) {
                        Log.i(TAG, "  REQUEST_EDIT_CONTROLLER_PROFILE");
                        Intent intent = new Intent();
                        intent.putExtra(Constants.EXTRA_CONTROLLER_PROFILE_INDEX, profileIndex);
                        intent.putExtra(Constants.EXTRA_CONTROLLER_PROFILE, profile);
                        EditControllerProfileActivity.this.setResult(RESULT_OK, intent);
                    }

                    EditControllerProfileActivity.this.finish();
                }

                return true;
        }

        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult...");
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Log.i(TAG, "  RESULT_OK");

            String controllerActionId = data.getStringExtra(Constants.EXTRA_CONTROLLER_ACTION_ID);
            SBrickControllerProfile.ControllerAction controllerAction = data.getParcelableExtra(Constants.EXTRA_CONTROLLER_ACTION);
            profile.setControllerAction(controllerActionId, controllerAction);

            controllerActionListAdapter.notifyDataSetChanged();
        }
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

        private Context context;
        private SBrickControllerProfile profile;

        public ControllerActionListAdapter(Context context, SBrickControllerProfile profile) {
            this.context = context;
            this.profile = profile;
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
                SBrickControllerProfile.ControllerAction controllerAction = profile.getControllerAction(controllerActionId);
                return controllerAction;
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View rowView = convertView;

            switch (getItemViewType(position)) {

                case ViewTypeProfileName:

                    if (rowView == null) {
                        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        rowView = inflater.inflate(R.layout.listview_item_controller_action_head, parent, false);
                    }

                    EditText etProfileName = (EditText)rowView.findViewById(R.id.edittext_controller_profile_name);
                    etProfileName.setText(profile.getName());
                    etProfileName.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        }
                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            profile.setName(s.toString());
                        }
                        @Override
                        public void afterTextChanged(Editable s) {
                        }
                    });

                    break;

                case ViewTypeControllerAction:

                    if (rowView == null) {
                        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        rowView = inflater.inflate(R.layout.listview_item_controller_action, parent, false);
                    }

                    final String controllerActionName = SBrickControllerProfile.getControllerActionName(getControllerActionId(position));
                    final SBrickControllerProfile.ControllerAction controllerAction = (SBrickControllerProfile.ControllerAction)getItem(position);

                    TextView twControllerActionName = (TextView) rowView.findViewById(R.id.textview_controller_action_name);
                    TextView twSBrickName = (TextView) rowView.findViewById(R.id.textview_sbrick_name);
                    TextView twChannel = (TextView) rowView.findViewById(R.id.textview_channel);
                    TextView twInvert = (TextView) rowView.findViewById(R.id.textview_invert);

                    if (controllerAction != null) {
                        String sbrickAddress = controllerAction.getSBrickAddress();
                        SBrick sbrick = SBrickManagerHolder.getManager().getSBrick(sbrickAddress);

                        twControllerActionName.setText(controllerActionName);
                        twSBrickName.setText(sbrick != null ? sbrick.getName() : "?????");
                        twChannel.setText(Integer.toString(controllerAction.getChannel() + 1));
                        twInvert.setText(controllerAction.getInvert() ? "Invert" : "Non-invert");
                    }
                    else {
                        twControllerActionName.setText(controllerActionName);
                        twSBrickName.setText("-");
                        twChannel.setText("-");
                        twInvert.setText("-");
                    }

                    ImageButton btnDeleteControllerAction = (ImageButton) rowView.findViewById(R.id.button_delete_controller_action);
                    btnDeleteControllerAction.setVisibility(controllerAction != null ? View.VISIBLE : View.INVISIBLE);
                    btnDeleteControllerAction.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Helper.showQuestionDialog(
                                    context,
                                    "Do you really want to delete this action?",
                                    "Yes",
                                    "No",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Log.i(TAG, "onClick...");
                                            profile.removeControllerAction(getControllerActionId(position));
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

                    break;
            }

            return rowView;
        }

        //
        // API
        //

        public static String getControllerActionId(int position) {
            if (position == 0)
                return "";

            switch (position - 1) {
                case 0: return SBrickControllerProfile.CONTROLLER_ACTION_DPAD_LEFT_RIGHT;
                case 1: return SBrickControllerProfile.CONTROLLER_ACTION_DPAD_UP_DOWN;
                case 2: return SBrickControllerProfile.CONTROLLER_ACTION_AXIS_X;
                case 3: return SBrickControllerProfile.CONTROLLER_ACTION_AXIS_Y;
                case 4: return SBrickControllerProfile.CONTROLLER_ACTION_THUMB_L;
                case 5: return SBrickControllerProfile.CONTROLLER_ACTION_AXIS_Z;
                case 6: return SBrickControllerProfile.CONTROLLER_ACTION_AXIS_RZ;
                case 7: return SBrickControllerProfile.CONTROLLER_ACTION_THUMB_R;
                case 8: return SBrickControllerProfile.CONTROLLER_ACTION_A;
                case 9: return SBrickControllerProfile.CONTROLLER_ACTION_B;
                case 10: return SBrickControllerProfile.CONTROLLER_ACTION_X;
                case 11: return SBrickControllerProfile.CONTROLLER_ACTION_Y;
                case 12: return SBrickControllerProfile.CONTROLLER_ACTION_R1;
                case 13: return SBrickControllerProfile.CONTROLLER_ACTION_R_TRIGGER;
                case 14: return SBrickControllerProfile.CONTROLLER_ACTION_L1;
                case 15: return SBrickControllerProfile.CONTROLLER_ACTION_L_TRIGGER;
                case 16: return SBrickControllerProfile.CONTROLLER_ACTION_START;
                case 17: return SBrickControllerProfile.CONTROLLER_ACTION_SELECT;
            }
            return "";
        }
    }
}
