package client.controller;

import client.model.ClientFileModel;
import client.model.Entry;
import org.jbibtex.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

/**
 * Created by daan on 12/4/16.
 */
public class BibTeXEntryFormatter extends BibTeXFormatter {

    private static BibTeXEntryFormatter instance = new BibTeXEntryFormatter();

    private BibTeXEntryFormatter() {
        //singleton
    }

    public static BibTeXEntryFormatter getInstance() {
        return instance;
    }

    private ArrayList<String> createBibTeXEntryContentList(File bibFile) throws IOException {
        Objects.requireNonNull(bibFile, "(bibFile == null) in BibTexEntryFormatter.createBibTeXEntryContentList().");
        BibTeXDatabase bibTeXDatabase = ClientFileHandler.getBibTeXDatabaseObjectFromFile(bibFile);
        if (bibTeXDatabase != null) {
            ArrayList<String> entryContentList = new ArrayList<>();
            StringWriter stringWriter;
            for (Map.Entry currentBibTeXEntry : bibTeXDatabase.getEntries().entrySet()) {
                stringWriter = new StringWriter();
                entryContentList.add(formatBibTeXEntryAsString((BibTeXEntry) currentBibTeXEntry.getValue(), stringWriter));
                stringWriter.close();
            }
            return entryContentList;
        }
        return null;
    }

    public ArrayList<Entry> createBibTeXEntryObjectListFromClientFileModel(ClientFileModel clientFileModel) throws IOException {
        Objects.requireNonNull(clientFileModel, "(clientFileModel == null) in BibTexEntryFormatter.createBibTeXEntryObjectListFromClientFileModel().");
        ArrayList<Entry> entryObjectList = new ArrayList<>();
        for (File currentFile : clientFileModel.getBibFiles()) {
            ArrayList<String> entryContentList = this.createBibTeXEntryContentList(currentFile);
            for (String currentEntryContent : entryContentList) {
                Entry currentEntryObject = new Entry(clientFileModel.getClientID(),
                        currentEntryContent, clientFileModel.getCslFilesAsStrings(),
                        clientFileModel.getTemplateFilesAsStrings());
                entryObjectList.add(currentEntryObject);
            }
        }
        return entryObjectList;
    }

    private String formatBibTeXEntryAsString(BibTeXEntry bibTeXEntry, StringWriter stringWriter) throws IOException {
        format(bibTeXEntry, stringWriter);
        return stringWriter.toString();
    }


}
