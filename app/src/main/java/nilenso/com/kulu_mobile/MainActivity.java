package nilenso.com.kulu_mobile;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
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
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.exceptions.RealmMigrationNeededException;
import nilenso.com.kulu_mobile.accounts.GenericAccountService;


public class MainActivity extends ActionBarActivity {
    private InvoiceListAdapter invoiceListAdapter;
    private String LOG_TAG = "MainActivity";
    public static final String INVOICE_LOCATION = "invoiceLocationFromCamera";
    public static final String CURRENT_PHOTO_PATH = "currentPhotoPath";
    public static final String DEFAULT_PHOTO_PATH = "";

    public static final String AUTHORITY = "nilenso.com.kulu_mobile.sync.basicsyncadapter";

    public static final long SECONDS_PER_MINUTE = 60L;
    public static final long SYNC_INTERVAL_IN_MINUTES = 30L;
    public static final long SYNC_INTERVAL =
            SYNC_INTERVAL_IN_MINUTES *
                    SECONDS_PER_MINUTE;

    // An account type, in the form of a domain name
    Account mAccount;
    private final RealmChangeListener syncListener = new RealmChangeListener() {
            @Override
            public void onChange() {
                ContentResolver.requestSync(
                        GenericAccountService.GetAccount(),
                        MainActivity.AUTHORITY,
                        Bundle.EMPTY);
            }
        };

    private void updateView() {
        setContentView(R.layout.activity_main);
        ListView invoiceList = (ListView) findViewById(R.id.listView);
        invoiceList.invalidate();
        RealmResults<ExpenseEntry> expenses = null;


        try {
            Realm realm = Realm.getInstance(this);
            expenses = realm.where(ExpenseEntry.class).equalTo("deleted", false).findAll("createdAt", RealmResults.SORT_ORDER_DESCENDING);
        } catch (RealmMigrationNeededException ex) {
            Realm.deleteRealmFile(this);
            Realm realm = Realm.getInstance(this);
            expenses = realm.where(ExpenseEntry.class).equalTo("deleted", false).findAll("createdAt", RealmResults.SORT_ORDER_DESCENDING);

        }
        invoiceListAdapter = new InvoiceListAdapter(this, R.layout.invoices_list_item, expenses);
        invoiceList.setAdapter(invoiceListAdapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateView();
        mAccount = CreateSyncAccount(this);
        Realm.getInstance(this).addChangeListener(syncListener);
        IntentFilter f = new IntentFilter(SyncAdapter.UPLOAD_FINISHED_ACTION);
        registerReceiver(uploadFinishedReceiver, f);
    }

    private Account CreateSyncAccount(Context context) {
        Account newAccount = GenericAccountService.GetAccount();
        AccountManager accountManager =
                (AccountManager) context.getSystemService(
                        ACCOUNT_SERVICE);

        accountManager.addAccountExplicitly(newAccount, null, null);

        ContentResolver.setSyncAutomatically(newAccount, AUTHORITY, true);
        ContentResolver.addPeriodicSync(newAccount, AUTHORITY, Bundle.EMPTY, SYNC_INTERVAL);

        return GenericAccountService.GetAccount();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateView();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(uploadFinishedReceiver);
        Realm.getInstance(this).removeAllChangeListeners();
        super.onDestroy();
    }

    private void addExpense(Uri invoiceURI) {
        Intent recordExpense = new Intent(this, RecordExpense.class);
        recordExpense.putExtra(INVOICE_LOCATION, invoiceURI.getPath());

        startActivity(recordExpense);
    }

    public BroadcastReceiver uploadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            Bundle extra = intent.getExtras();
            String fileName = extra.getString(SyncService.FILEUPLOADED_EXTRA);
            File file = new File(fileName);

            deleteUploadedExpense(file);

            Toast.makeText(context,
                    "Upload finished for " + file.getName(), Toast.LENGTH_SHORT).show();

            invoiceListAdapter.notifyDataSetChanged();
        }
    };

    private void deleteUploadedExpense(File file) {
        Realm realm = Realm.getInstance(this);
        realm.removeAllChangeListeners();
        realm.beginTransaction();
        ExpenseEntry expense = realm.where(ExpenseEntry.class)
                .equalTo("invoice", file.getName())
                .findFirst();
        if (expense != null) expense.setDeleted(true);
        realm.commitTransaction();
        realm.addChangeListener(syncListener);
        realm.close();
    }

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