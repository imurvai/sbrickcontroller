package com.scn.sbrickcontroller;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

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

    private boolean scanned = false;
    private final List<SBrick> sbricks = new ArrayList<>();
    private SBrickListViewAdapter sbrickListViewAdapter;

    private ProgressDialog progressDialog;

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
        return view;
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume...");
        super.onResume();

        Log.i(TAG, "  Setup the list view adapter...");
        ListView sbricksListView = (ListView)getView().findViewById(R.id.listViewSBricks);
        sbricksListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "onItemClick...");

                // Open the SBrick details fragment
                String selectedSBrickAddress = sbricks.get(position).getAddress();
                MainActivity activity = (MainActivity)getActivity();
                activity.startSBrickDetailsFragment(selectedSBrickAddress);
            }
        });
        sbrickListViewAdapter = new SBrickListViewAdapter(getActivity(), sbricks);
        sbricksListView.setAdapter(sbrickListViewAdapter);

        Log.i(TAG, "  Register the SBrick local broadcast reveiver...");
        IntentFilter filter = new IntentFilter();
        filter.addAction(SBrickManager.ACTION_START_SBRICK_SCAN);
        filter.addAction(SBrickManager.ACTION_STOP_SBRICK_SCAN);
        filter.addAction(SBrickManager.ACTION_FOUND_AN_SBRICK);
        LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(sbrickBroadcastReceiver, filter);

        if (!scanned) {
            SBrickManagerHolder.getSBrickManager().startSBrickScan();
        }
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause...");
        super.onPause();

        Log.i(TAG, "  Unregister the SBrick local broadcast receiver...");
        LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(sbrickBroadcastReceiver);

        Log.i(TAG, "  Stop the SBrick scan. Just in case...");
        SBrickManagerHolder.getSBrickManager().stopSBrickScan();

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
                case SBrickManager.ACTION_START_SBRICK_SCAN:
                    Log.i(TAG, "  ACTION_START_SBRICK_SCAN");

                    progressDialog = Helper.showProgressDialog(SBrickListFragment.this.getActivity(), "Scanning for SBricks...", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.i(TAG, "onClick...");
                            SBrickManagerHolder.getSBrickManager().stopSBrickScan();
                        }
                    });
                    break;

                case SBrickManager.ACTION_STOP_SBRICK_SCAN:
                    Log.i(TAG, "  ACTION_STOP_SBRICK_SCAN");

                    if (progressDialog != null) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }
                    scanned = true;
                    break;

                case SBrickManager.ACTION_FOUND_AN_SBRICK:
                    Log.i(TAG, "  ACTION_FOUND_AN_SBRICK");

                    String sbrickName = intent.getStringExtra(SBrickManager.EXTRA_SBRICK_NAME);
                    String sbrickAddress = intent.getStringExtra(SBrickManager.EXTRA_SBRICK_ADDRESS);
                    Log.i(TAG, "  SBrick name   : " + sbrickName);
                    Log.i(TAG, "  Sbrick address: " + sbrickAddress);

                    SBrick sbrick = SBrickManagerHolder.getSBrickManager().getSBrick(sbrickAddress);
                    if (sbrick != null) {
                        sbricks.add(sbrick);
                        sbrickListViewAdapter.notifyDataSetChanged();
                    }
                    break;
            }
        }
    };

    private static class SBrickListViewAdapter extends ArrayAdapter<SBrick> {

        private final Context context;
        private final List<SBrick> sbricks;

        public SBrickListViewAdapter(Context context, List<SBrick> items) {
            super(context, R.layout.sbrick_list_item, items);
            this.context = context;
            this.sbricks = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView = convertView;

            if (rowView == null) {
                LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowView = inflater.inflate(R.layout.sbrick_list_item, parent, false);
            }

            SBrick sbrick = sbricks.get(position);

            TextView twSBrickName = (TextView)rowView.findViewById(R.id.textview_sbrick_name);
            TextView twSBrickAddress = (TextView)rowView.findViewById(R.id.textview_sbrick_address);
            twSBrickName.setText(sbrick.getDisplayName());
            twSBrickAddress.setText(sbrick.getAddress());

            return rowView;
        }
    }}
