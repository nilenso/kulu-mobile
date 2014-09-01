package nilenso.com.kulu_mobile;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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

        ImageView iv = (ImageView) convertView.findViewById(R.id.upload_button);
        iv.setImageBitmap(getImageThumbnail(getItem(position)));

        return convertView;
    }

    private Bitmap getImageThumbnail(File path) {
        return ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(path.toString()), 90, 90);
    }
}