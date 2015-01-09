package nilenso.com.kulu_mobile.validations;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;

import java.text.ParseException;
import java.util.List;

import nilenso.com.kulu_mobile.expenses.RecordNoProofExpense;

public class AddExpenseActivity implements Validator.ValidationListener {
    private Context mContext;

    public AddExpenseActivity(Context context) {
        mContext = context;
    }

    @Override

    public void onValidationSucceeded() {
        try {
            ((RecordNoProofExpense) mContext).createExpenseEntry();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        ((Activity) mContext).finish();
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            View failedView = error.getView();
            String errorMessage = error.getFailedRules().get(0).getMessage(mContext);

            if (failedView instanceof EditText) {
                ((EditText) failedView).setError(errorMessage);
            }
        }
    }
}
