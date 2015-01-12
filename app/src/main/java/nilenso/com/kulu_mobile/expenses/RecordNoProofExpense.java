package nilenso.com.kulu_mobile.expenses;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import io.realm.Realm;

import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;

import nilenso.com.kulu_mobile.ExpenseEntry;
import nilenso.com.kulu_mobile.R;
import nilenso.com.kulu_mobile.fragments.DatePickerFragment;
import nilenso.com.kulu_mobile.validations.AddExpenseActivity;

public class RecordNoProofExpense extends RecordExpense {
    private Validator validator;

    @NotEmpty
    private EditText amount;

    @NotEmpty
    private EditText merchantName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_no_proof_expense);
        super.setupSpinner();
        setupDatePicker();
        setupCurrencyPicker();

        validator = new Validator(this);
        validator.setValidationListener(new AddExpenseActivity(this));
    }

    private void setupCurrencyPicker() {
        Spinner spinner = (Spinner) findViewById(R.id.currency);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.currency_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private  void setupDatePicker() {
        SimpleDateFormat formatter = new SimpleDateFormat("MMMM dd, yyyy");
        ((TextView) findViewById(R.id.datePicker)).setText(formatter.format(new Date()));
        ((TextView) findViewById(R.id.datePicker)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getFragmentManager(), "datePicker");
            }
        });
    }

    @Override
    public void saveExpense(View view) throws ParseException {
        merchantName = (EditText) findViewById(R.id.merchantName);
        amount = (EditText) findViewById(R.id.amount);
        validator.validate();
   }

    @Override
    public void createExpenseEntry() throws ParseException {
        Realm realm = Realm.getInstance(this);
        realm.beginTransaction();
        ExpenseEntry expenseEntry = realm.createObject(ExpenseEntry.class);
        expenseEntry.setComments(getRemarks());
        expenseEntry.setExpenseType(getExpenseType());
        expenseEntry.setCurrency(getCurrency());
        expenseEntry.setCreatedAt(new Date());
        expenseEntry.setId(UUID.randomUUID().toString());
        expenseEntry.setAmount(getAmount());
        expenseEntry.setMerchantName(getMerchantName());
        expenseEntry.setExpenseDate(getExpenseDate());
        realm.commitTransaction();
    }

    private Date getExpenseDate() throws ParseException {
        TextView datePicker = (TextView) findViewById(R.id.datePicker);
        SimpleDateFormat formatter = new SimpleDateFormat("MMMM dd, yyyy");
        return formatter.parse(datePicker.getText().toString());
    }

    private int getAmount() {
        String value = amount.getText().toString();
        return Integer.valueOf(value);
    }

    public String getMerchantName() {
        return merchantName.getText().toString();
    }

    public String getCurrency() {
        return ((Spinner) findViewById(R.id.currency)).getSelectedItem().toString();
    }
}
