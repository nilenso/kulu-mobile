package nilenso.com.kulu_mobile;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.realm.Realm;


public class RecordExpense extends FragmentActivity {
    private String LOG_TAG = "RecordExpenseActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_expense);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_record_expense, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void saveExpense(View view) {
        EditText comments = (EditText) findViewById(R.id.editComments);

        RadioGroup expenseType = (RadioGroup) findViewById(R.id.expense_type);
        String checkedExpenseType = null;

        switch(expenseType.getCheckedRadioButtonId()) {
            case R.id.company:
                checkedExpenseType = "Company";
                break;
            case R.id.personal:
                checkedExpenseType = "Personal";
                break;
            case R.id.reimbursement:
                checkedExpenseType = "Reimbursement";
                break;
        }

        Realm realm = Realm.getInstance(this);
        realm.beginTransaction();
        ExpenseEntry expenseEntry = realm.createObject(ExpenseEntry.class);
        expenseEntry.setComments(comments.getText().toString());
        expenseEntry.setExpenseType(checkedExpenseType);
        expenseEntry.setInvoice(MediaStore.EXTRA_OUTPUT);
        expenseEntry.setCreatedAt(new Date());
        realm.commitTransaction();
        finish();
    }

    public void dispatchTakePictureIntent(View view) {
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

                // add the new file to the top of the main list of files
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


}
