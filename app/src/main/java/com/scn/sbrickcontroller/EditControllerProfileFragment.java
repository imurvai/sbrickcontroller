package com.scn.sbrickcontroller;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.scn.sbrickcontrollerprofilemanager.SBrickControllerProfile;
import com.scn.sbrickmanager.SBrick;
import com.scn.sbrickmanager.SBrickManagerHolder;

import org.w3c.dom.Text;


public class EditControllerProfileFragment extends Fragment {

    //
    // Private members
    //

    private static final String TAG = EditControllerProfileFragment.class.getSimpleName();

    private static final String ARG_CONTROLLER_PROFILE = "arg_controller_profile";

    private EditText etProfileName;
    private ListView lwControllerActions;
    private ControllerActionListAdapter conrollerActionListAdapter;

    SBrickControllerProfile profile;

    //
    // Constructors
    //

    public EditControllerProfileFragment() {
    }

    public static EditControllerProfileFragment newInstance(SBrickControllerProfile controllerProfile) {
        EditControllerProfileFragment fragment = new EditControllerProfileFragment();

        Bundle args = new Bundle();
        args.putParcelable(ARG_CONTROLLER_PROFILE, controllerProfile);
        fragment.setArguments(args);

        return fragment;
    }

    //
    // Fragment overrides
    //

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            profile = getArguments().getParcelable(ARG_CONTROLLER_PROFILE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_controller_profile, container, false);

        lwControllerActions = (ListView)view.findViewById(R.id.listview_conroller_actions);
        lwControllerActions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "onItemClick...");

                String controllerActionId = ControllerActionListAdapter.getControllerActionId(position);

                MainActivity activity = (MainActivity)EditControllerProfileFragment.this.getActivity();
                activity.startEditControllerActionFragment(profile, controllerActionId);
            }
        });
        conrollerActionListAdapter = new ControllerActionListAdapter(getActivity(), profile);
        lwControllerActions.setAdapter(conrollerActionListAdapter);

        return view;
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
                String controllerActionId = getControllerActionId(position - 1);
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
                        rowView = inflater.inflate(R.layout.controller_action_head_item, parent, false);
                    }

                    EditText etProfileName = (EditText)rowView.findViewById(R.id.edittext_controller_profile_name);
                    etProfileName.setText(profile.getName());

                    break;

                case ViewTypeControllerAction:

                    if (rowView == null) {
                        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        rowView = inflater.inflate(R.layout.controller_action_item, parent, false);
                    }

                    final String controllerActionName = SBrickControllerProfile.getControllerActionName(getControllerActionId(position - 1));
                    final SBrickControllerProfile.ControllerAction controllerAction = (SBrickControllerProfile.ControllerAction)getItem(position);

                    TextView twControllerActionName = (TextView) rowView.findViewById(R.id.textview_controller_action_name);
                    TextView twSBrickName = (TextView) rowView.findViewById(R.id.textview_sbrick_name);
                    TextView twChannel = (TextView) rowView.findViewById(R.id.textview_channel);
                    TextView twInvert = (TextView) rowView.findViewById(R.id.textview_invert);

                    if (controllerAction != null) {
                        String sbrickAddress = controllerAction.getSbrickAddress();
                        SBrick sbrick = SBrickManagerHolder.getSBrickManager().getSBrick(sbrickAddress);

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

                    Button btnDeleteControllerAction = (Button) rowView.findViewById(R.id.button_delete_controller_action);
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
        // Private methods
        //

        public static String getControllerActionId(int position) {
            switch (position) {
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
