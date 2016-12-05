package client.model;

import client.controller.ClientFileHandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * Created by daan on 11/30/16.
 */
public class ClientFileModel {

    private final String clientID;
    private ArrayList<File> bibFiles = new ArrayList<>();
    private ArrayList<String> cslFilesAsStrings = new ArrayList<>();
    private ArrayList<String> templateFilesAsStrings = new ArrayList<>();

    public ClientFileModel(String clientID) {
        this.clientID = clientID;
    }

    public String getClientID() {
        return clientID;
    }

    public ArrayList<File> getBibFiles() {
        return bibFiles;
    }

    public ArrayList<String> getCslFilesAsStrings() {
        return cslFilesAsStrings;
    }

    public ArrayList<String> getTemplateFilesAsStrings() {
        return templateFilesAsStrings;
    }

    public void addBibFile(File bibFile) throws IOException {
        Objects.requireNonNull(bibFile, "(bibFile == null) in clientFileModel.addBibFile()");
        if (!bibFiles.contains(bibFile) && ClientFileHandler.isValidBibFile(bibFile))
            bibFiles.add(bibFile);
    }

    public void addCslFileAsString(File cslFile) throws IOException {
        Objects.requireNonNull(cslFile, "(cslFile == null) in clientFileModel.addCslFileAsString()");
        String cslFileAsString = ClientFileHandler.readStringFromFile(cslFile);
        if (!cslFilesAsStrings.contains(cslFileAsString))
            cslFilesAsStrings.add(cslFileAsString);
    }

    public void addTemplateFileAsString(File templateFile) throws IOException {
        Objects.requireNonNull(templateFile, "(templateFile == null) in clientFileModel.addTemplateFileAsString()");
        String templateFileAsString = ClientFileHandler.readStringFromFile(templateFile);
        if (!templateFilesAsStrings.contains(templateFileAsString))
            templateFilesAsStrings.add(templateFileAsString);
    }

    public void addBibFiles(Collection<File> bibFiles) throws IOException {
        Objects.requireNonNull(bibFiles, "(bibFiles == null) in clientFileModel.addBibFiles()");
        for (File currentBibFile : bibFiles)
            addBibFile(currentBibFile);
    }

    public void addCslFilesAsStrings(Collection<File> cslFiles) throws IOException {
        Objects.requireNonNull(cslFiles, "(cslFiles == null) in clientFileModel.addCslFilesAsString()");
        for (File currentCslFile : cslFiles)
            addCslFileAsString(currentCslFile);
    }

    public void addTemplateFilesAsStrings(Collection<File> templateFiles) throws IOException {
        Objects.requireNonNull(templateFiles, "(templateFiles == null) in clientFileModel.addTemplateFilesAsStrings()");
        for (File currentTemplateFile : templateFiles)
            addTemplateFileAsString(currentTemplateFile);
    }

    public String toString() {
        return (System.lineSeparator() + "ClientFileModel '" + clientID + "' has " + bibFiles.size() +
                " bibFiles, " + cslFilesAsStrings.size() + " cslFiles and "
                + templateFilesAsStrings.size() + " templateFiles." + System.lineSeparator());
    }
}
