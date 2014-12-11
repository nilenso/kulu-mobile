package nilenso.com.kulu_mobile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.exceptions.RealmMigrationNeededException;


public class MainActivity extends ActionBarActivity {
    private InvoiceListAdapter invoiceListAdapter;
    private String LOG_TAG = "MainActivity";
    public static final String INVOICE_LOCATION = "invoiceLocationFromCamera";
    public static final String CURRENT_PHOTO_PATH = "currentPhotoPath";
    public static final String DEFAULT_PHOTO_PATH = "";

    private void updateView() {
        setContentView(R.layout.activity_main);
        ListView invoiceList = (ListView) findViewById(R.id.listView);
        RealmResults<ExpenseEntry> expenses = null;

        try {
            Realm realm = Realm.getInstance(this);
            expenses = realm.where(ExpenseEntry.class).equalTo("deleted", false).findAll().sort("createdAt", RealmResults.SORT_ORDER_DECENDING);
        } catch (RealmMigrationNeededException ex) {
            Realm.deleteRealmFile(this);
            Realm realm = Realm.getInstance(this);
            expenses = realm.where(ExpenseEntry.class).equalTo("deleted", false).findAll().sort("createdAt", RealmResults.SORT_ORDER_DECENDING);
        }
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

    private void addExpense(Uri invoiceURI) {
        Intent recordExpense = new Intent(this, RecordExpense.class);
        recordExpense.putExtra(INVOICE_LOCATION, invoiceURI.getPath());

        startActivity(recordExpense);
    }

    public BroadcastReceiver uploadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extra = intent.getExtras();
            File file = new File(extra.getString(InvoiceUploadService.FILEUPLOADED_EXTRA));


            ImageButton uploadButton = (ImageButton) findViewById(R.id.upload_button);
            uploadButton.setEnabled(true);

            Toast.makeText(context,
                    "Upload finished for " + file.getName(), Toast.LENGTH_SHORT).show();

            invoiceListAdapter.notifyDataSetChanged();
        }
    };

    private Uri mCurrentPhotoPath = null;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            setCurrentPhotoPath();
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

    private void setCurrentPhotoPath() {
        if (currentPhotoPathExists()) return;
        mCurrentPhotoPath = getCurrentPhotoPath();
    }

    private boolean currentPhotoPathExists() {
        return mCurrentPhotoPath != null;
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
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, 1);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMG_" + timeStamp;
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);

        Log.i(LOG_TAG, "Creating image file...");
        File mediaFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        mCurrentPhotoPath = Uri.fromFile(mediaFile);
        saveCurrentPhotoPath();
        Log.e(LOG_TAG, "FILE path " + mCurrentPhotoPath);
        return mediaFile;
    }

    private void saveCurrentPhotoPath() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(CURRENT_PHOTO_PATH, mCurrentPhotoPath.getPath());
        editor.commit();
    }

    private Uri getCurrentPhotoPath() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        String currentPhotoPath = sharedPref.getString(CURRENT_PHOTO_PATH, DEFAULT_PHOTO_PATH);
        return Uri.parse(currentPhotoPath);
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