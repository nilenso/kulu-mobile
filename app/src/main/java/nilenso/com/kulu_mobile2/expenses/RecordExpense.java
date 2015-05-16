package nilenso.com.kulu_mobile2.expenses;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;

import java.io.File;
import java.text.ParseException;
import java.util.Date;
import java.util.UUID;

import io.realm.Realm;
import nilenso.com.kulu_mobile2.ExpenseEntry;
import nilenso.com.kulu_mobile2.MainActivity;
import nilenso.com.kulu_mobile2.R;
import nilenso.com.kulu_mobile2.SplashScreen;
import nilenso.com.kulu_mobile2.User;


public class RecordExpense extends FragmentActivity {
    protected String LOG_TAG = "RecordExpenseActivity";
    protected String invoiceLocation = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_expense);
        invoiceLocation = getIntent().getStringExtra(MainActivity.INVOICE_LOCATION);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void saveExpense(View view) throws ParseException {
        createExpenseEntry();
        finish();
    }

    protected String getExpenseType() {
        return getCheckedRadioButtonValue();
    }

    protected String getCheckedRadioButtonValue() {
        RadioGroup expenseType = (RadioGroup) findViewById(R.id.expense_type);
        switch (expenseType.getCheckedRadioButtonId()) {
            case R.id.company:
                return "Company";
            case R.id.reimbursement:
                return "Reimbursement";
            default:
                return "";
        }
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
        createUserIfMissing(realm);
        expenseEntry.setEmail(currentUserEmail());
        realm.commitTransaction();
    }

    protected void createUserIfMissing(Realm realm) {
        User user = realm.where(User.class).equalTo("email", currentUserEmail()).findFirst();
        if (isNull(user)) {
            user = realm.createObject(User.class);
            user.setCurrentUserInfo(currentUserEmail());
        }
    }

    private boolean isNull(User user) {
        return user == null;
    }

    protected String getFileNameFromUri(String invoiceLocation) {
        return new File(invoiceLocation).getName();
    }

    protected String getRemarks() {
        return ((EditText) findViewById(R.id.editComments)).getText().toString();
    }

    protected String currentUserEmail() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPref.getString(SplashScreen.ACCOUNT_NAME, "");
    }
}
