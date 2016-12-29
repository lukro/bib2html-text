package client.controller;

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

    private static String readStringFromFile(File file) throws IOException {
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

    static ArrayList<String> createStringListFromFileList(ArrayList<File> fileList) throws IOException {
        ArrayList<String> stringList = new ArrayList<>();
        for (File currentFile : fileList)
            stringList.add(ClientFileHandler.readStringFromFile(currentFile));
        return stringList;
    }

    public static boolean isValidBibFile(File bibFile) throws FileNotFoundException {
        return (BibTeXFileSplitter.getBibTeXDatabaseFromFile(bibFile) != null);

    }

}