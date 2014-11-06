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
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.invoices_list_item, parent, false);
        }

        ExpenseEntry currentItem = getItem(position);

        TextView tv = (TextView) convertView.findViewById(R.id.invoice_file_name);
        tv.setText(currentItem.getComments());

        TextView tv1 = (TextView) convertView.findViewById(R.id.invoice_file_timestamp);
        tv1.setText(currentItem.getCreatedAt().toString());

        if (currentItem.getInvoice() != null ) {
            ImageButton b = (ImageButton) convertView.findViewById(R.id.upload_button);
            // so that we can access the item inside the handler
            b.setTag(currentItem.getInvoice());
            b.setOnClickListener(uploadButtonHandler);
        }

        return convertView;
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