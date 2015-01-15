package nilenso.com.kulu_mobile.expenses;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import java.io.File;
import java.text.ParseException;
import java.util.Date;
import java.util.UUID;

import io.realm.Realm;
import nilenso.com.kulu_mobile.ExpenseEntry;
import nilenso.com.kulu_mobile.MainActivity;
import nilenso.com.kulu_mobile.R;


public class RecordExpense extends FragmentActivity {
    protected String LOG_TAG = "RecordExpenseActivity";
    protected String invoiceLocation = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_expense);
        setupSpinner();
        invoiceLocation = getIntent().getStringExtra(MainActivity.INVOICE_LOCATION);
    }

    protected void setupSpinner() {
        Spinner spinner = (Spinner) findViewById(R.id.paidFor);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.paid_for_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String item = (String) parent.getItemAtPosition(pos);

                if (item.equals("Self")) {
                    RadioButton button = (RadioButton) findViewById(R.id.personal);
                    button.setEnabled(false);
                    RadioGroup mOption = (RadioGroup) findViewById(R.id.expense_type);
                    mOption.check(R.id.company);
                } else {
                    RadioButton button = (RadioButton) findViewById(R.id.personal);
                    button.setEnabled(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater();
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

    public void saveExpense(View view) throws ParseException {
        EditText comments = (EditText) findViewById(R.id.editComments);
        createExpenseEntry();
        finish();
    }

    protected String getExpenseType() {
        String checkedExpenseType = getCheckedRadioButtonValue();
        String paidBy = getPaidByValue();
        if (paidBy.equals("Self") && checkedExpenseType.equals("Company"))
            return "Reimbursement";
        return checkedExpenseType;
    }

    protected String getPaidByValue() {
        Spinner mySpinner=(Spinner) findViewById(R.id.paidFor);
        return mySpinner.getSelectedItem().toString();
    }

    protected void createExpenseEntry() throws ParseException {
        Realm realm = Realm.getInstance(this);
        realm.beginTransaction();
        ExpenseEntry expenseEntry = realm.createObject(ExpenseEntry.class);
        expenseEntry.setComments(getRemarks());
        expenseEntry.setExpenseType(getExpenseType());
        expenseEntry.setInvoice(getFileNameFromUri(invoiceLocation));
        expenseEntry.setInvoicePath(invoiceLocation);
        expenseEntry.setCreatedAt(new Date());
        expenseEntry.setId(UUID.randomUUID().toString());
        realm.commitTransaction();
    }

    protected String getFileNameFromUri(String invoiceLocation) {
        return new File(invoiceLocation).getName();
    }

    protected String getRemarks() {
        return ((EditText) findViewById(R.id.editComments)).getText().toString();
    }



    protected String getCheckedRadioButtonValue() {
        RadioGroup expenseType = (RadioGroup) findViewById(R.id.expense_type);
        switch(expenseType.getCheckedRadioButtonId()) {
            case R.id.company:
                return "Company";
            case R.id.personal:
                return "Personal";
            default:
                return "";
        }
    }
}
