package nilenso.com.kulu_mobile;

import org.apache.commons.io.FilenameUtils;

import java.io.File;

public class FileUtils {
    public static String getLastPartOfFile(String file) {
        return FilenameUtils.getBaseName(file) + "." + FilenameUtils.getExtension(file);
    }
}
