package nilenso.com.kulu_mobile;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;

import java.util.Date;

import io.realm.Realm;


public class RecordExpense extends FragmentActivity {
    private String LOG_TAG = "RecordExpenseActivity";
    private String invoiceLocation = null;

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
        expenseEntry.setInvoice(invoiceLocation);
        expenseEntry.setCreatedAt(new Date());
        realm.commitTransaction();
        finish();
    }
}
