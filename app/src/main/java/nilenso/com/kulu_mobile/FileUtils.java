package nilenso.com.kulu_mobile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;

public class FileUtils {
    public static Bitmap getThumbnailForImage(File file) {
        return ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(file.toString()), 90, 90);
    }

    public static String getPrettyPrintedDate(File file) {
        DateTime dt = new DateTime(file.lastModified());
        DateTimeFormatter fmt = DateTimeFormat.forPattern("MMMM dd, yyyy, hh:mm a");
        return fmt.print(dt);
    }

    public static String getBaseNameForFile(File file) {
        String name = file.getName();
        int pos = name.lastIndexOf(".");
        if (pos > 0) {
            name = name.substring(0, pos);
        }
        return name;
    }
}
