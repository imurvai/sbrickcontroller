package com.scn.sbrickcontroller;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
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

import com.scn.sbrickmanager.SBrick;
import com.scn.sbrickmanager.SBrickManager;
import com.scn.sbrickmanager.SBrickManagerHolder;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SBrickListActivity extends BaseActivity {

    //
    // Private members
    //

    private static final String TAG = SBrickListActivity.class.getSimpleName();

    private SBrickListAdapter sbrickListAdapter;
    private AsyncTask<Void, Integer, Void> scanAsyncTask;

    private ListView listViewSBricks;

    //
    // Activity overrides
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sbrick_list);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Log.i(TAG, "  Setup the list view adapter...");
        listViewSBricks = (ListView)findViewById(R.id.listview_sbricks);
        listViewSBricks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "onItemClick...");

                // Open the SBrick details fragment
                List<SBrick> sbrickList = SBrickManagerHolder.getManager().getSBricks();
                SBrick sbrick = sbrickList.get(position);
                String selectedSBrickAddress = sbrick.getAddress();

                Intent intent = new Intent(SBrickListActivity.this, SBrickDetailsActivity.class);
                intent.putExtra(Constants.EXTRA_SBRICK_ADDRESS, selectedSBrickAddress);
                startActivity(intent);
            }
        });
        sbrickListAdapter = new SBrickListAdapter(this);
        listViewSBricks.setAdapter(sbrickListAdapter);
        listViewSBricks.setEmptyView(findViewById(R.id.textview_empty_sbricks));
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume...");
        super.onResume();

        Log.i(TAG, "  Register the SBrick local broadcast reveiver...");
        IntentFilter filter = new IntentFilter();
        filter.addAction(SBrickManager.ACTION_FOUND_AN_SBRICK);
        LocalBroadcastManager.getInstance(this).registerReceiver(sbrickBroadcastReceiver, filter);

        sbrickListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause...");
        super.onPause();

        Log.i(TAG, "  Unregister the SBrick local broadcast receiver...");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(sbrickBroadcastReceiver);

        SBrickManagerHolder.getManager().stopSBrickScan();
        SBrickManagerHolder.getManager().saveSBricks();

        if (scanAsyncTask != null) {
            Log.i(TAG, "  Cancel the scanning...");
            scanAsyncTask.cancel(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "onCreateOptionsMenu...");

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_sbrick_list, menu);

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

            case R.id.menu_item_start_scan:
                Log.i(TAG, "  menu_item_start_scan");

                if (SBrickManagerHolder.getManager().startSBrickScan()) {

                    scanAsyncTask = new AsyncTask<Void, Integer, Void>() {

                        private static final int MaxProgress = 20;
                        private ProgressDialog progressDialog;

                        @Override
                        protected void onPreExecute() {
                            Log.i(TAG, "  scanAsyncTask.onPreExecute...");

                            progressDialog = Helper.showProgressDialog(SBrickListActivity.this, "Scanning for SBricks...", MaxProgress, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Log.i(TAG, "progressDialog.onClick...");
                                    scanAsyncTask.cancel(true);
                                }
                            });
                        }

                        @Override
                        protected Void doInBackground(Void... params) {
                            try {
                                for (int progress = 0 ; progress <= MaxProgress; progress++) {
                                    Thread.sleep(1000);
                                    if (isCancelled())
                                        break;

                                    publishProgress(progress);
                                }
                            }
                            catch (Exception ex) {}

                            return null;
                        }

                        @Override
                        protected void onProgressUpdate(Integer... progress) {
                            Log.i(TAG, "  scanAsyncTask.onProgressUpdate - " + progress[0]);

                            if (progressDialog != null)
                                progressDialog.setProgress(progress[0]);
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            Log.i(TAG, "  scanAsyncTask.onPostExecute - ");
                            cleanup();
                        }

                        @Override
                        protected void onCancelled() {
                            Log.i(TAG, "  scanAsyncTask.onCancelled - ");
                            cleanup();
                        }

                        private void cleanup() {
                            if (progressDialog != null) {
                                progressDialog.dismiss();
                                progressDialog = null;
                            }
                            SBrickManagerHolder.getManager().stopSBrickScan();
                            scanAsyncTask = null;
                        }
                    }.execute();
                }
                else {
                    Helper.showMessageBox(SBrickListActivity.this, "Could not start scanning for SBricks.", null);
                }

                return true;
        }

        return super.onOptionsItemSelected(item);
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
            return SBrickManagerHolder.getManager().getSBricks().size();
        }

        @Override
        public Object getItem(int position) {
            return SBrickManagerHolder.getManager().getSBricks().get(position);
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
                rowView = inflater.inflate(R.layout.listview_item_sbrick_list, parent, false);
            }

            final SBrick sbrick = (SBrick)getItem(position);

            TextView twSBrickName = (TextView)rowView.findViewById(R.id.textview_sbrick_name);
            TextView twSBrickAddress = (TextView)rowView.findViewById(R.id.textview_sbrick_address);
            twSBrickName.setText(sbrick.getName());
            twSBrickAddress.setText(sbrick.getAddress());

            ImageButton btnRenameSBrick = (ImageButton)rowView.findViewById(R.id.button_rename_sbrick);
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
                            if (newName.length() > 0) {
                                sbrick.setName(newName);
                                SBrickListAdapter.this.notifyDataSetChanged();
                            }
                            else {
                                Helper.showMessageBox(context, "The name can't be empty.", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                });
                            }
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

            ImageButton btnForgetSBrick = (ImageButton)rowView.findViewById(R.id.button_forget_sbrick);
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
                                    SBrickManagerHolder.getManager().forgetSBrick(sbrick.getAddress());
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
