package nilenso.com.kulu_mobile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmResults;


public class MainActivity extends ActionBarActivity {
    private InvoiceListAdapter invoiceListAdapter;
    private String LOG_TAG = "MainActivity";
    public static final String INVOICE_LOCATION = "invoiceLocationFromCamera";

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
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(uploadFinishedReceiver);
        super.onDestroy();
    }

    private void addExpense(Uri invoiceName) {
        Intent recordExpense = new Intent(this, RecordExpense.class);
        recordExpense.putExtra(INVOICE_LOCATION, invoiceName.getPath());

        startActivity(recordExpense);
    }

    public BroadcastReceiver uploadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extra = intent.getExtras();
            File fileToRemove = new File(extra.getString(InvoiceUploadService.FILEUPLOADED_EXTRA));

            if (!fileToRemove.delete()) {
                Log.e(LOG_TAG, "Couldn't remove the file " + fileToRemove.toString());
            }

            invoiceListAdapter.notifyDataSetChanged();
        }
    };

    private Uri mCurrentPhotoPath = null;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                addExpense(mCurrentPhotoPath);

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

    public void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;

            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e(LOG_TAG, "Problem in saving the file" + ex.getMessage());
            }

            // continue only if the File was successfully created
            if (photoFile != null) {
                mCurrentPhotoPath = Uri.fromFile(photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCurrentPhotoPath);
                startActivityForResult(takePictureIntent, 1);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;

        Log.i(LOG_TAG, "Creating image file...");
        mediaFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES) +
                File.separator +
                "IMG_" + timeStamp + ".jpg");

        return mediaFile;
    }

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
                dispatchTakePictureIntent();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}