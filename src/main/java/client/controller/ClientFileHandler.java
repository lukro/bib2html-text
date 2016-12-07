package client.controller;

import org.jbibtex.*;

import java.io.*;
import java.util.Objects;

/**
 * Created by daan on 11/30/16.
 */
public final class ClientFileHandler {

    private ClientFileHandler() {
        throw new AssertionError("ClientFileHandler is a static class.");
    }

    public static String readStringFromFile(File file) throws IOException {
        Objects.requireNonNull(file, "(file == null) in ClientFileHandler.readStringFromFile()");
        FileInputStream fis = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader br = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        sb.append(br.readLine());
        while (br.ready()) {
//            sb.append("\r\n");
            sb.append(System.lineSeparator());
            sb.append(br.readLine());
        }
        fis.close();
        isr.close();
        br.close();
        return sb.toString();
    }

    static BibTeXDatabase getBibTeXDatabaseObjectFromFile(File bibFile) throws FileNotFoundException {
        Objects.requireNonNull(bibFile, "(bibFile == null) in ClientFileHandler.getBibTeXDatabaseObjectFromFile()");
        FileReader fileReader = new FileReader(bibFile);
        BibTeXDatabase result = null;
        try {
            BibTeXParser parser = new BibTeXParser();
            result = parser.parse(fileReader);
            fileReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static boolean isValidBibFile(File bibFile) throws FileNotFoundException {
        Objects.requireNonNull(bibFile, "(bibFile == null) in ClientFileHandler.isValidBibFile()");
        return (getBibTeXDatabaseObjectFromFile(bibFile) != null);

    }

    public static boolean isValidCslFile(File cslFile) {
        Objects.requireNonNull(cslFile, "(cslFile == null) in ClientFileHandler.isValidCslFile()");
        //TODO: implement algorithm to validate cslFile/xmlFile
        if (true) {
            return true;
        }
        return false;
    }

}
