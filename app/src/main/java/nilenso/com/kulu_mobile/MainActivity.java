package nilenso.com.kulu_mobile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmResults;


public class MainActivity extends ActionBarActivity {
    private ArrayList<File> filesList;
    private InvoiceListAdapter invoiceListAdapter;
    private String LOG_TAG = "MainActivity";


    private void updateView() {
        setContentView(R.layout.activity_main);
        ListView invoiceList = (ListView) findViewById(R.id.listView);
        Realm realm = Realm.getInstance(this);

        RealmResults<ExpenseEntry> expenses = realm.where(ExpenseEntry.class).findAll().sort("createdAt", RealmResults.SORT_ORDER_DECENDING);
        invoiceListAdapter = new InvoiceListAdapter(this, R.layout.invoices_list_item, expenses);
        invoiceList.setAdapter(invoiceListAdapter);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateView();
        IntentFilter f = new IntentFilter(InvoiceUploadService.UPLOAD_FINISHED_ACTION);
        registerReceiver(uploadFinishedReceiver, f);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateView();
       // invoiceListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(uploadFinishedReceiver);
        super.onDestroy();
    }

    private void addExpense() {
        Intent recordExpense = new Intent(this, RecordExpense.class);
        startActivity(recordExpense);
    }

    public BroadcastReceiver uploadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extra = intent.getExtras();
            File fileToRemove = new File(extra.getString(InvoiceUploadService.FILEUPLOADED_EXTRA));

            if (!fileToRemove.delete()) {
                Log.e(LOG_TAG, "Couldn't remove the file" + fileToRemove.toString());
            }

            File itemToRemove = null;
            for (File item : filesList) {
                if (item.toString().equals(fileToRemove.toString())) {
                    itemToRemove = item;
                }
            }
            if(itemToRemove != null) {
                filesList.remove(itemToRemove);
            }

            invoiceListAdapter.notifyDataSetChanged();
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(getApplicationContext(),
                        "New image added.", Toast.LENGTH_SHORT).show();

            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(),
                        "No new image was added.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "Failed to capture image.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    AbsListView.MultiChoiceModeListener multiChoiceModeListener = new AbsListView.MultiChoiceModeListener() {
        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            // Here you can do something when items are selected/de-selected,
            // such as update the title in the CAB
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            // Respond to clicks on the actions in the CAB
            switch (item.getItemId()) {
                case R.id.action_upload:
                    addExpense();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.main_activity_selection_actions, menu);

            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) { }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_scan:
                addExpense();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}