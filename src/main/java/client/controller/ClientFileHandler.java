package client.controller;

import global.util.FileUtils;

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

    static ArrayList<String> createStringListFromFileList(ArrayList<File> fileList) throws IOException {
        ArrayList<String> stringList = new ArrayList<>();
        for (File currentFile : fileList)
            stringList.add(FileUtils.readStringFromFile(currentFile));
        return stringList;
    }

    public static boolean isValidBibFile(File bibFile) throws FileNotFoundException {
        return (BibTeXFileSplitter.getBibTeXDatabaseFromFile(bibFile) != null);
    }

}