package client.controller;

import client.model.ClientFileModel;
import global.logging.Log;
import global.logging.LogLevel;
import global.model.DefaultEntry;
import global.model.IEntry;
import org.jbibtex.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

/**
 * @author daan
 *         created on 12/21/16.
 */
public enum BibTeXFileSplitter {
    INSTANCE;

    private final BibTeXEntryFormatter bibTeXEntryFormatter = new BibTeXEntryFormatter();

    private final class BibTeXEntryFormatter extends BibTeXFormatter {

        private BibTeXEntryFormatter() {
        }

        private String formatBibTeXEntryAsString(BibTeXEntry bibTeXEntry, StringWriter stringWriter) {
            try {
                format(bibTeXEntry, stringWriter);
            } catch (IOException e) {
                Log.log("couldn't format bibTeXEntry as String in BibTeXFileSplitter.", LogLevel.ERROR);
                return null;
            }
            return stringWriter.toString();
        }

    }

    ArrayList<IEntry> createIEntryListFromClientFileModel(ClientFileModel clientFileModel) {
        Objects.requireNonNull(clientFileModel, "(clientFileModel == null) in BibTexEntryFormatter.createIEntryListFromClientFileModel()");
        ArrayList<IEntry> entryObjectList = new ArrayList<>();
        ArrayList<String> cslFilesAsStrings, templatesAsStrings;
        try {
            cslFilesAsStrings = ClientFileHandler.createStringListFromFileList(clientFileModel.getCslFiles());
            templatesAsStrings = ClientFileHandler.createStringListFromFileList(clientFileModel.getTemplates());
        } catch (IOException e) {
            Log.log("couldn't create fileLists as Strings from clientFileModel.", LogLevel.ERROR);
            return null;
        }
        //create DefaultEntry-Objects
        for (int bibFileIndex = 0; bibFileIndex < clientFileModel.getBibFiles().size(); bibFileIndex++) {
            ArrayList<String> entryContentList = this.createBibTeXEntryContentList(clientFileModel.getBibFiles().get(bibFileIndex));
            for (int positionInBibFile = 0; positionInBibFile < entryContentList.size(); positionInBibFile++) {
                DefaultEntry currentEntryObject = new DefaultEntry(clientFileModel.getClientID(),
                        entryContentList.get(positionInBibFile), bibFileIndex, positionInBibFile, cslFilesAsStrings,
                        templatesAsStrings);
                entryObjectList.add(currentEntryObject);
            }
        }
        return entryObjectList;
    }

    ArrayList<String> createBibTeXEntryContentList(File bibFile) {
        Objects.requireNonNull(bibFile, "(bibFile == null) in BibTexEntryFormatter.createBibTeXEntryContentList()");
        BibTeXDatabase bibTeXDatabase = BibTeXFileSplitter.getBibTeXDatabaseObjectFromFile(bibFile);
        if (bibTeXDatabase != null) {
            ArrayList<String> entryContentList = new ArrayList<>();
            StringWriter stringWriter;
            for (Map.Entry currentBibTeXEntry : bibTeXDatabase.getEntries().entrySet()) {
                stringWriter = new StringWriter();
                entryContentList.add(bibTeXEntryFormatter.formatBibTeXEntryAsString((BibTeXEntry) currentBibTeXEntry.getValue(), stringWriter));
                try {
                    stringWriter.close();
                } catch (IOException e) {
                    Log.log("couldn't close StringWritrer in BibTeXFileSpliter.", LogLevel.ERROR);
                }

            }
            return entryContentList;
        }
        return null;
    }

    static BibTeXDatabase getBibTeXDatabaseObjectFromFile(File bibFile) {
        FileReader fileReader;
        try {
            fileReader = new FileReader(bibFile);
        } catch (FileNotFoundException e) {
            Log.log("couldn't find .bib-file in BibTeXFileSplitter.", LogLevel.ERROR);
            return null;
        }
        BibTeXDatabase result = null;
        try {
            BibTeXParser parser = new BibTeXParser();
            result = parser.parse(fileReader);
            try {
                fileReader.close();
            } catch (IOException e) {
                Log.log("couldn't close FileReader in BibTeXFileSplitter.", LogLevel.ERROR);
            }
        } catch (ParseException e) {
            Log.log("invalid .bib-file.", LogLevel.WARNING);
        }
        return result;
    }

}
