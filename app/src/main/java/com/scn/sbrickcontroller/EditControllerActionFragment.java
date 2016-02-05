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
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.scn.sbrickcontrollerprofilemanager.SBrickControllerProfile;
import com.scn.sbrickcontrollerprofilemanager.SBrickControllerProfileManagerHolder;
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

    private static final String ARG_CONTROLLER_PROFILE_INDEX = "arg_controller_profile_index";
    private static final String ARG_CONTROLLER_ACTION_ID = "arg_controller_action_id";
    private static final String ARG_SBRICK_ADDRESS_LIST = "arg_sbrick_address_list";

    private SBrickControllerProfile profile;
    private String controllerActionId;
    private List<String> sbrickAddresses;

    private String selectedSBrickAddress;
    private int selectedChannel;
    private boolean selectedInvert;

    //
    // Constructors
    //

    public EditControllerActionFragment() {
    }

    public static EditControllerActionFragment newInstance(int profileIndex, String controllerActionId, List<String> sbrickAddresses) {

        EditControllerActionFragment fragment = new EditControllerActionFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_CONTROLLER_PROFILE_INDEX, profileIndex);
        args.putString(ARG_CONTROLLER_ACTION_ID, controllerActionId);
        args.putStringArrayList(ARG_SBRICK_ADDRESS_LIST, new ArrayList<String>(sbrickAddresses));
        fragment.setArguments(args);

        return fragment;
    }

    //
    // Fragment overrides
    //

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate...");
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            int profileIndex = getArguments().getInt(ARG_CONTROLLER_PROFILE_INDEX);
            profile = SBrickControllerProfileManagerHolder.getManager().getProfileAt(profileIndex);
            controllerActionId = getArguments().getString(ARG_CONTROLLER_ACTION_ID);
            sbrickAddresses = getArguments().getStringArrayList(ARG_SBRICK_ADDRESS_LIST);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView...");

        View view = inflater.inflate(R.layout.fragment_edit_controller_action, container, false);

        TextView twControllerActionName = (TextView)view.findViewById(R.id.textview_controller_action_name);
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

        Spinner spSelectSBrick = (Spinner)view.findViewById(R.id.spinner_select_sbrick);
        spSelectSBrick.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, sbrickAddresses));
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

        Spinner spSelectChannel = (Spinner)view.findViewById(R.id.spinnel_select_channel);
        spSelectChannel.setAdapter(new ArrayAdapter<Integer>(getActivity(), android.R.layout.simple_list_item_1, new Integer[] { 1, 2, 3, 4 }));
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

        Switch swInvertChannel = (Switch)view.findViewById(R.id.switch_invert_channel);
        swInvertChannel.setChecked(selectedInvert);
        swInvertChannel.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i(TAG, "onCheckedChanged...");
                selectedInvert = isChecked;
            }
        });

        Button btnOk = (Button)view.findViewById(R.id.button_ok);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "btnOk.onClick...");

                SBrickControllerProfile.ControllerAction newControllerAction = new SBrickControllerProfile.ControllerAction(selectedSBrickAddress, selectedChannel, selectedInvert);
                profile.setControllerAction(controllerActionId, newControllerAction);

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
