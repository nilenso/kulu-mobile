package nilenso.com.kulu_mobile;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import io.realm.RealmResults;

public class InvoiceListAdapter extends ArrayAdapter<ExpenseEntry> {
    public InvoiceListAdapter(Context ctx, int viewResourceId, RealmResults<ExpenseEntry> fs) {
        super(ctx, viewResourceId, fs);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = getConvertView(convertView, parent);
        ExpenseEntry currentItem = getItem(position);

        setRemarks(convertView, currentItem);
        setCreatedAtTime(convertView, currentItem);

        if (invoiceExists(currentItem))
            setUploadListener(convertView, currentItem);

        return convertView;
    }

    private View getConvertView(View convertView, ViewGroup parent) {
        if (!convertViewExists(convertView))
            convertView = inflateConvertView(parent);
        return convertView;
    }

    private View inflateConvertView(ViewGroup parent) {
        return LayoutInflater.from(getContext())
                .inflate(R.layout.invoices_list_item, parent, false);
    }

    private boolean convertViewExists(View convertView) {
        return convertView != null;
    }

    private void setUploadListener(View convertView, ExpenseEntry currentItem) {
        ImageButton b = (ImageButton) convertView.findViewById(R.id.upload_button);
        // so that we can access the item inside the handler
        b.setTag(currentItem.getInvoicePath());
        b.setOnClickListener(uploadButtonHandler);
    }

    private boolean invoiceExists(ExpenseEntry currentItem) {
        return currentItem.getInvoice() != null;
    }

    private void setCreatedAtTime(View convertView, ExpenseEntry currentItem) {
        TextView tv1 = (TextView) convertView.findViewById(R.id.invoice_file_timestamp);
        tv1.setText(currentItem.getCreatedAt().toString());
    }

    private void setRemarks(View convertView, ExpenseEntry currentItem) {
        TextView tv = (TextView) convertView.findViewById(R.id.invoice_file_name);
        tv.setText(currentItem.getComments());
    }

    View.OnClickListener uploadButtonHandler = new View.OnClickListener() {
        public void onClick(View v) {
            String filePath = (String) v.getTag();
            Intent intent = new Intent(getContext(), InvoiceUploadService.class);
            intent.putExtra(InvoiceUploadService.ARG_FILE_PATH, filePath);
            getContext().startService(intent);
        }
    };
}