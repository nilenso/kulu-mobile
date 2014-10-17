package nilenso.com.kulu_mobile;

import org.apache.commons.io.FilenameUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;

public class FileUtils {
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

    public static String getLastPartOfFile(String file) {
        return FilenameUtils.getBaseName(file) + "." + FilenameUtils.getExtension(file);
    }
}
