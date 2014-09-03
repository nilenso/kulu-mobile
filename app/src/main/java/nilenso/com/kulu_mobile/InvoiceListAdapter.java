package nilenso.com.kulu_mobile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class InvoiceListAdapter extends ArrayAdapter<File> {
    public InvoiceListAdapter(Context ctx, int viewResourceId, ArrayList<File> files) {
        super(ctx, viewResourceId, files);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.invoices_list_item, parent, false);
        }

        File currentItem = getItem(position);

        TextView tv = (TextView) convertView.findViewById(R.id.invoice_file_name);
        tv.setText(FileUtils.getBaseNameForFile(currentItem));

        TextView tv1 = (TextView) convertView.findViewById(R.id.invoice_file_timestamp);
        tv1.setText(FileUtils.getPrettyPrintedDate(currentItem));

        if (currentItem.exists()) {
            Button b = (Button) convertView.findViewById(R.id.upload_button);
            b.setTag(getItem(position).getPath());
            b.setOnClickListener(uploadButtonHandler);

            ImageView iv = (ImageView) convertView.findViewById(R.id.invoice_file_thumb);
            iv.setImageBitmap(FileUtils.getThumbnailForImage(currentItem));

        } else {
            remove(currentItem);
            notifyDataSetChanged();
        }

        return convertView;
    }

    View.OnClickListener uploadButtonHandler = new View.OnClickListener() {
        public void onClick(View v) {
            String filePath = (String) v.getTag();
            Intent intent = new Intent(getContext(), InvoiceUploadService.class);
            intent.putExtra(InvoiceUploadService.ARG_FILE_PATH, filePath);
            getContext().startService(intent);

            IntentFilter f = new IntentFilter(InvoiceUploadService.UPLOAD_FINISHED_ACTION);
            v.getContext().registerReceiver(uploadFinishedReceiver, f);
        }
    };

    public BroadcastReceiver uploadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            notifyDataSetChanged();
        }
    };
}