package com.scn.sbrickcontroller;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.scn.sbrickcontrollerprofilemanager.SBrickControllerProfile;
import com.scn.sbrickcontrollerprofilemanager.SBrickControllerProfileManagerHolder;
import com.scn.sbrickmanager.SBrick;
import com.scn.sbrickmanager.SBrickManagerHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller profile list fragment.
 */
public class ControllerProfileListFragment extends Fragment {

    //
    // Private members
    //

    private static final String TAG = ControllerProfileListFragment.class.getSimpleName();

    private static final int MENU_ITEM_EDIT = 0;
    private static final int MENU_ITEM_REMOVE = 1;

    private ControllerProfileListAdapter controllerProfileListAdapter;

    private ListView listViewControllerProfiles;
    private Button buttonAddControllerProfile;

    //
    // Constructors
    //

    public ControllerProfileListFragment() {
        // Required empty public constructor
    }

    public static ControllerProfileListFragment newInstance() {
        ControllerProfileListFragment fragment = new ControllerProfileListFragment();

        Bundle args = new Bundle();
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
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_controller_profile_list, container, false);

        listViewControllerProfiles = (ListView)view.findViewById(R.id.listViewControllerProfiles);
        listViewControllerProfiles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "onItemClick...");

                // TODO: open the controller fragment
            }
        });
        listViewControllerProfiles.setAdapter(new ControllerProfileListAdapter(getActivity()));
        registerForContextMenu(listViewControllerProfiles);

        buttonAddControllerProfile = (Button)view.findViewById(R.id.buttonAddControllerProfile);
        buttonAddControllerProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick...");

                // TODO: open the controller profile editor with a blank profile
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume...");
        super.onResume();

        try {
            SBrickControllerProfileManagerHolder.getManager().loadProfiles();
        }
        catch (Exception ex) {
            Helper.showMessageBox(
                    getActivity(),
                    "Could not load controller profiles.",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.i(TAG, "onClick...");
                        }
            });
        }
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause...");
        super.onPause();

        try {
            SBrickControllerProfileManagerHolder.getManager().saveProfiles();
        }
        catch (Exception ex) {
            Helper.showMessageBox(
                    getActivity(),
                    "Could not save controller profiles.",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.i(TAG, "onClick...");
                        }
                    });
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        if (v == listViewControllerProfiles) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
            SBrickControllerProfile profile = SBrickControllerProfileManagerHolder.getManager().getProfileAt(info.position);
            menu.setHeaderTitle(profile.getName());
            menu.add(Menu.NONE, 0, MENU_ITEM_EDIT, "Edig profile");
            menu.add(Menu.NONE, 0, MENU_ITEM_REMOVE, "Remove profile");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Log.i(TAG, "onContextItemSelected...");

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        final SBrickControllerProfile profile = SBrickControllerProfileManagerHolder.getManager().getProfileAt(info.position);

        if (item.getItemId() == MENU_ITEM_EDIT) {
            Log.i(TAG, "  MENU_ITEM_EDIT");

            // TODO: open the profile editor with the selected profile
        }
        else if (item.getItemId() == MENU_ITEM_REMOVE) {
            Log.i(TAG, "  MENU_ITEM_EDIT");

            Helper.showQuestionDialog(
                    getActivity(),
                    "Do you really want to remove this profile?",
                    "Yes",
                    "No",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.i(TAG, "onClick...");
                            SBrickControllerProfileManagerHolder.getManager().removeProfile(profile);
                            controllerProfileListAdapter.notifyDataSetChanged();
                        }
                    },
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Do nothing here
                        }
                    }
            );
        }

        return true;
    }

    //
    // Private methods and classes
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
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView = convertView;

            if (rowView == null) {
                LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowView = inflater.inflate(R.layout.controller_profile_item, parent, false);
            }

            SBrickControllerProfile profile = (SBrickControllerProfile) getItem(position);

            TextView twControllerProfileName = (TextView)rowView.findViewById(R.id.textviewControllerProfileName);
            twControllerProfileName.setText(profile.getName());

            return rowView;
        }
    }
}
