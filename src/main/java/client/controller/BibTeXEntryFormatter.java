package client.controller;

import client.model.ClientFileModel;
import global.model.DefaultEntry;
import org.jbibtex.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

/**
 * @author daan
 *         created on 12/4/16.
 */
class BibTeXEntryFormatter extends BibTeXFormatter {

    private static BibTeXEntryFormatter INSTANCE = new BibTeXEntryFormatter();

    private BibTeXEntryFormatter() {
        //singleton
    }

    static BibTeXEntryFormatter getINSTANCE() {
        return INSTANCE;
    }

    private ArrayList<String> createBibTeXEntryContentList(File bibFile) throws IOException {
        Objects.requireNonNull(bibFile, "(bibFile == null) in BibTexEntryFormatter.createBibTeXEntryContentList()");
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

    ArrayList<DefaultEntry> createBibTeXEntryObjectListFromClientFileModel(ClientFileModel clientFileModel) throws IOException {
        Objects.requireNonNull(clientFileModel, "(clientFileModel == null) in BibTexEntryFormatter.createBibTeXEntryObjectListFromClientFileModel()");
        ArrayList<DefaultEntry> entryObjectList = new ArrayList<>();
//        for (File currentFile : clientFileModel.getBibFiles()) {
//            ArrayList<String> entryContentList = this.createBibTeXEntryContentList(currentFile);
//            for (String currentEntryContent : entryContentList) {
//                DefaultEntry currentEntryObject = new DefaultEntry(clientFileModel.getClientID(),
//                        currentEntryContent, clientFileModel.getCslFiles(),
//                        clientFileModel.getTemplates());
//                entryObjectList.add(currentEntryObject);
//            }
//        }

        //create csl-/template-String-Lists
        ArrayList<String> cslFilesAsStrings = ClientFileHandler.createStringListFromFileList(clientFileModel.getCslFiles());
        ArrayList<String> templatesAsStrings = ClientFileHandler.createStringListFromFileList(clientFileModel.getTemplates());


//        for (File currentCslFile : clientFileModel.getCslFiles())
//            cslFilesAsStrings.add(ClientFileHandler.readStringFromFile(currentCslFile));
//        for (File currentTemplate : clientFileModel.getTemplates())
//            templatesAsStrings.add(ClientFileHandler.readStringFromFile(currentTemplate));

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

//        for (int bibFileIndex = 0; bibFileIndex < clientFileModel.getBibFiles().size(); bibFileIndex++) {
//            ArrayList<String> entryContentList = this.createBibTeXEntryContentList(clientFileModel.getBibFiles().get(bibFileIndex));
//            for (int positionInBibFile = 0; positionInBibFile < entryContentList.size(); positionInBibFile++) {
//                DefaultEntry currentEntryObject = new DefaultEntry(clientFileModel.getClientID(),
//                        entryContentList.get(positionInBibFile), bibFileIndex, positionInBibFile, clientFileModel.getCslFiles(),
//                        clientFileModel.getTemplates());
//                entryObjectList.add(currentEntryObject);
//            }
//        }
        return entryObjectList;
    }

    private String formatBibTeXEntryAsString(BibTeXEntry bibTeXEntry, StringWriter stringWriter) throws IOException {
        format(bibTeXEntry, stringWriter);
        return stringWriter.toString();
    }


}
