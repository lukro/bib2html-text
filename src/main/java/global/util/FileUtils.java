package global.util;

import java.io.*;
import java.util.Objects;

/**
 * Created by daan on 1/17/17.
 */
public final class FileUtils {

    private FileUtils() {
        throw new AssertionError("FileUtils is a static class.");
    }

    public static String readStringFromFile(File file) throws IOException {
        Objects.requireNonNull(file, "(file == null) in ClientFileHandler.readStringFromFile()");
        FileInputStream fis = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader br = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        sb.append(br.readLine());
        while (br.ready()) {
            sb.append(System.lineSeparator());
            sb.append(br.readLine());
        }
        fis.close();
        isr.close();
        br.close();
        return sb.toString();
    }
}
