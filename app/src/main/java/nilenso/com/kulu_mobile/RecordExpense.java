package nilenso.com.kulu_mobile;

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
import java.util.Date;

import io.realm.Realm;


public class RecordExpense extends FragmentActivity {
    private String LOG_TAG = "RecordExpenseActivity";
    private String invoiceLocation = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_expense);
        setupSpinner();
        invoiceLocation = getIntent().getStringExtra(MainActivity.INVOICE_LOCATION);
    }

    private void setupSpinner() {
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.planets_array, android.R.layout.simple_spinner_item);
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
        String checkedExpenseType = getCheckedRadioButtonValue();
        String paidBy = getPaidByValue();
        createExpenseEntry(comments, getExpenseType(checkedExpenseType, paidBy));
        finish();
    }

    private String getExpenseType(String checkedExpenseType, String paidBy) {
        if (paidBy.equals("Self") && checkedExpenseType.equals("Company"))
            return "Reimbursement";
        return checkedExpenseType;
    }


    private String getPaidByValue() {
        Spinner mySpinner=(Spinner) findViewById(R.id.spinner);
        return mySpinner.getSelectedItem().toString();
    }

    private void createExpenseEntry(EditText comments, String checkedExpenseType) {
        Realm realm = Realm.getInstance(this);
        realm.beginTransaction();
        ExpenseEntry expenseEntry = realm.createObject(ExpenseEntry.class);
        expenseEntry.setComments(comments.getText().toString());
        expenseEntry.setExpenseType(checkedExpenseType);
        expenseEntry.setInvoice(getFileNameFromUri(invoiceLocation));
        expenseEntry.setInvoicePath(invoiceLocation);
        expenseEntry.setCreatedAt(new Date());
        realm.commitTransaction();
    }

    private String getFileNameFromUri(String invoiceLocation) {
        return new File(invoiceLocation).getName();
    }

    private String getCheckedRadioButtonValue() {
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
