package com.scn.sbrickcontroller;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.scn.sbrickcontrollerprofilemanager.SBrickControllerProfile;
import com.scn.sbrickmanager.SBrick;
import com.scn.sbrickmanager.SBrickManagerHolder;

import java.util.ArrayList;
import java.util.List;


/**
 * Edit controller action fragment.
 */
public class EditControllerActionFragment extends Fragment {

    //
    // Private members
    //

    private static final String TAG = EditControllerActionFragment.class.getSimpleName();

    private static final String ARG_CONTROLLER_PROFILE = "arg_controller_profile";
    private static final String ARG_CONTROLLER_ACTION_ID = "arg_controller_action_id";

    private SBrickControllerProfile profile;
    private String controllerActionId;
    private List<SBrick> sbricks;

    //
    // Constructors
    //

    public EditControllerActionFragment() {
    }

    public static EditControllerActionFragment newInstance(SBrickControllerProfile profile, String controllerActionId) {

        EditControllerActionFragment fragment = new EditControllerActionFragment();

        Bundle args = new Bundle();
        args.putParcelable(ARG_CONTROLLER_PROFILE, profile);
        args.putString(ARG_CONTROLLER_ACTION_ID, controllerActionId);
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
            controllerActionId = getArguments().getString(ARG_CONTROLLER_ACTION_ID);
        }

        sbricks = new ArrayList<>(SBrickManagerHolder.getSBrickManager().getSBricks());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_controller_action, container, false);

        TextView twControllerActionName = (TextView)view.findViewById(R.id.textview_controller_action_name);
        twControllerActionName.setText(SBrickControllerProfile.getControllerActionName(controllerActionId));

        Spinner spSelectSBrick = (Spinner)view.findViewById(R.id.spinner_select_sbrick);
        spSelectSBrick.setAdapter(new ArrayAdapter<SBrick>(getActivity(), android.R.layout.simple_list_item_1, sbricks));
        spSelectSBrick.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "sbSelectSBrick.onItemClick...");

                // TODO: set the SBrick on the action
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.i(TAG, "sbSelectSBrick.onNothingSelected...");

                // TODO: set the SBrick on the action
            }
        });

        Spinner spSelectChannel = (Spinner)view.findViewById(R.id.spinnel_select_channel);
        spSelectChannel.setAdapter(new ArrayAdapter<Integer>(getActivity(), android.R.layout.simple_list_item_1, new Integer[] { 1, 2, 3, 4 }));
        spSelectChannel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "spSelectChannel.onItemSelected...");

                // TODO: set the channel on the action
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.i(TAG, "spSelectChannel.onNothingSelected...");

                // TODO: set the channel on the action
            }
        });

        Switch swInvertChannel = (Switch)view.findViewById(R.id.switch_invert_channel);
        swInvertChannel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "swInvertChannel.onClick...");

                // TODO: set the invert on the action
            }
        });

        Button btnOk = (Button)view.findViewById(R.id.button_ok);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "btnOk.onClick...");

                // TODO: set action in profile

                MainActivity activity = (MainActivity)getActivity();
                activity.goBackFromFragment();
            }
        });

        Button btnCancel = (Button)view.findViewById(R.id.button_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "btnCancel.onClick...");
                MainActivity activity = (MainActivity)getActivity();
                activity.goBackFromFragment();
            }
        });

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
}
