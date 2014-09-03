package nilenso.com.kulu_mobile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
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

        TextView tv = (TextView) convertView.findViewById(R.id.invoice_file_name);
        tv.setText(getItem(position).toString());

        File currentItem = getItem(position);

        if (currentItem.exists()) {
            Button b = (Button) convertView.findViewById(R.id.upload_button);
            b.setTag(getItem(position).getPath());
            b.setOnClickListener(uploadButtonHandler);

            ImageView iv = (ImageView) convertView.findViewById(R.id.invoice_file_thumb);
            iv.setImageBitmap(getImageThumbnail(currentItem));

        } else {
            remove(currentItem);
            notifyDataSetChanged();
        }

        return convertView;
    }

    private Bitmap getImageThumbnail(File path) {
        return ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(path.toString()), 90, 90);
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