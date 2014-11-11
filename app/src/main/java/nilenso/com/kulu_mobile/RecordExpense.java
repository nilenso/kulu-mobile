package nilenso.com.kulu_mobile;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;

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

        invoiceLocation = getIntent().getStringExtra(MainActivity.INVOICE_LOCATION);
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
        createExpenseEntry(comments, checkedExpenseType);
        finish();
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
            case R.id.reimbursement:
                return "Reimbursement";
        }
        return null;
    }
}
