package com.scn.sbrickcontroller;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.scn.sbrickcontrollerprofilemanager.SBrickControllerProfileManagerHolder;
import com.scn.sbrickmanager.SBrick;
import com.scn.sbrickmanager.SBrickManager;
import com.scn.sbrickmanager.SBrickManagerHolder;

import java.util.ArrayList;
import java.util.List;


public class SBrickListFragment extends Fragment {

    //
    // Private members
    //

    private static final String TAG = SBrickListFragment.class.getSimpleName();

    private SBrickListAdapter sbrickListAdapter;
    private ProgressDialog progressDialog;

    private ListView listViewSBricks;
    private Button buttonScanSBricks;

    //
    // Constructors
    //

    public SBrickListFragment() {

    }

    public static SBrickListFragment newInstance() {
        SBrickListFragment fragment = new SBrickListFragment();

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
        View view =  inflater.inflate(R.layout.fragment_sbrick_list, container, false);

        Log.i(TAG, "  Setup the list view adapter...");
        listViewSBricks = (ListView)view.findViewById(R.id.listViewSBricks);
        listViewSBricks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "onItemClick...");

                // Open the SBrick details fragment
                List<SBrick> sbrickList = new ArrayList<>(SBrickManagerHolder.getSBrickManager().getSBricks());
                SBrick sbrick = sbrickList.get(position);
                String selectedSBrickAddress = sbrick.getAddress();
                MainActivity activity = (MainActivity) getActivity();
                activity.startSBrickDetailsFragment(selectedSBrickAddress);
            }
        });
        sbrickListAdapter = new SBrickListAdapter(getActivity());
        listViewSBricks.setAdapter(sbrickListAdapter);

        buttonScanSBricks = (Button)view.findViewById(R.id.buttonScanSBricks);
        buttonScanSBricks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick...");
                if (SBrickManagerHolder.getSBrickManager().startSBrickScan()) {
                    progressDialog = Helper.showProgressDialog(SBrickListFragment.this.getActivity(), "Scanning for SBricks...", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.i(TAG, "onClick...");
                            SBrickManagerHolder.getSBrickManager().stopSBrickScan();
                            progressDialog.dismiss();
                            progressDialog = null;
                        }
                    });
                }
                else {
                    Helper.showMessageBox(SBrickListFragment.this.getActivity(), "Could not start scanning for SBricks.", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                }
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume...");
        super.onResume();

        Log.i(TAG, "  Register the SBrick local broadcast reveiver...");
        IntentFilter filter = new IntentFilter();
        filter.addAction(SBrickManager.ACTION_FOUND_AN_SBRICK);
        LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(sbrickBroadcastReceiver, filter);
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause...");
        super.onPause();

        Log.i(TAG, "  Unregister the SBrick local broadcast receiver...");
        LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(sbrickBroadcastReceiver);

        SBrickManagerHolder.getSBrickManager().stopSBrickScan();
        SBrickManagerHolder.getSBrickManager().saveSBricks();

        Log.i(TAG, "  Dismiss the progress dialog if open...");
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    //
    // Private methods and classes
    //

    private final BroadcastReceiver sbrickBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "sbrickBroadcastReceiver.onReceive...");

            switch (intent.getAction()) {

                case SBrickManager.ACTION_FOUND_AN_SBRICK:
                    Log.i(TAG, "  ACTION_FOUND_AN_SBRICK");

                    String sbrickName = intent.getStringExtra(SBrickManager.EXTRA_SBRICK_NAME);
                    String sbrickAddress = intent.getStringExtra(SBrickManager.EXTRA_SBRICK_ADDRESS);
                    Log.i(TAG, "  SBrick name   : " + sbrickName);
                    Log.i(TAG, "  Sbrick address: " + sbrickAddress);
                    sbrickListAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    private static class SBrickListAdapter extends BaseAdapter {

        private Context context;

        SBrickListAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return SBrickManagerHolder.getSBrickManager().getSBricks().size();
        }

        @Override
        public Object getItem(int position) {
            List<SBrick> sbrickList = new ArrayList<>(SBrickManagerHolder.getSBrickManager().getSBricks());
            return sbrickList.get(position);
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
                rowView = inflater.inflate(R.layout.sbrick_list_item, parent, false);
            }

            final SBrick sbrick = (SBrick)getItem(position);

            TextView twSBrickName = (TextView)rowView.findViewById(R.id.textview_sbrick_name);
            TextView twSBrickAddress = (TextView)rowView.findViewById(R.id.textview_sbrick_address);
            twSBrickName.setText(sbrick.getName());
            twSBrickAddress.setText(sbrick.getAddress());

            Button btnRenameSBrick = (Button)rowView.findViewById(R.id.rename_sbrick_button);
            btnRenameSBrick.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final EditText et = new EditText(context);
                    et.setText(sbrick.getName());

                    AlertDialog.Builder ab = new AlertDialog.Builder(context);
                    ab.setTitle("Rename the SBRick");
                    ab.setView(et);
                    ab.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String newName = et.getText().toString();
                            sbrick.setName(newName);
                            SBrickListAdapter.this.notifyDataSetChanged();
                        }
                    });
                    ab.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    ab.show();
                }
            });

            Button btnForgetSBrick = (Button)rowView.findViewById(R.id.forget_sbrick_button);
            btnForgetSBrick.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Helper.showQuestionDialog(
                            context,
                            "Do you really want to forget this SBrick?",
                            "Yes",
                            "No",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Log.i(TAG, "onClick...");
                                    SBrickManagerHolder.getSBrickManager().forgetSBrick(sbrick.getAddress());
                                    SBrickListAdapter.this.notifyDataSetChanged();
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
