package client.controller;

import global.logging.Log;
import global.logging.LogLevel;
import org.jbibtex.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Objects;

/**
 * @author daan
 *         created on 111/30/16.
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
            sb.append(System.lineSeparator());
            sb.append(br.readLine());
        }
        fis.close();
        isr.close();
        br.close();
        return sb.toString();
    }

    static BibTeXDatabase getBibTeXDatabaseObjectFromFile(File bibFile) throws FileNotFoundException {
        FileReader fileReader = new FileReader(bibFile);
        BibTeXDatabase result = null;
        try {
            BibTeXParser parser = new BibTeXParser();
            result = parser.parse(fileReader);
            fileReader.close();
        } catch (Exception e) {
//            Log.log("invalid .bib-file.", LogLevel.WARNING);
        }
        return result;
    }

    static ArrayList<String> createStringListFromFileList(ArrayList<File> fileList) throws IOException {
        ArrayList<String> stringList = new ArrayList<>();
        for (File currentFile : fileList)
            stringList.add(ClientFileHandler.readStringFromFile(currentFile));
        return stringList;
    }

    public static boolean isValidBibFile(File bibFile) throws FileNotFoundException {
        return (getBibTeXDatabaseObjectFromFile(bibFile) != null);

    }

    public static boolean isValidCslFile(File cslFile) {
        //TODO: implement algorithm to validate cslFile/xmlFile
        if (true && cslFile.equals(cslFile))
            return true;
        return false;
    }

}