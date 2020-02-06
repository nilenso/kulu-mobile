package nilenso.com.kulu.expenses;

import android.os.Bundle;

import nilenso.com.kulu.R;


public class RecordExpense extends Record {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_record_expense);
        super.onCreate(savedInstanceState);
    }
}
