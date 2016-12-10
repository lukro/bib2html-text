package client.model;

import client.controller.ClientFileHandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * @author daan
 *         created on 11/30/16.
 */
public class ClientFileModel {

    private final String clientID;
    private ArrayList<File> bibFiles = new ArrayList<>();
    private ArrayList<String> cslFilesAsStrings = new ArrayList<>();
    private ArrayList<String> templatesAsStrings = new ArrayList<>();

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

    public ArrayList<String> getTemplatesAsStrings() {
        return templatesAsStrings;
    }

    /**
     * adds 1 .bib-file to clientFileModel
     *
     * @param bibFileToAdd .bib-file you want to add
     * @throws IOException if .bib-file isn't valid
     */
    public void addBibFile(File bibFileToAdd) throws IOException {
        Objects.requireNonNull(bibFileToAdd, "(bibFileToAdd == null) in clientFileModel.addBibFile()");
        if (bibFiles.contains(bibFileToAdd))
            throw new IllegalArgumentException("bibFileList already contains chosen .bib-file.");
        else if (ClientFileHandler.isValidBibFile(bibFileToAdd))
            bibFiles.add(bibFileToAdd);
    }

    /**
     * adds 1 .csl-file to clientFileModel
     *
     * @param cslFileToAdd .csl-file you want to add
     * @throws IOException if file-problems appear
     */
    public void addCslFileAsString(File cslFileToAdd) throws IOException {
        Objects.requireNonNull(cslFileToAdd, "(cslFileToAdd == null) in clientFileModel.addCslFileAsString()");
        if (ClientFileHandler.isValidCslFile(cslFileToAdd)) {
            String cslFileAsString = ClientFileHandler.readStringFromFile(cslFileToAdd);
            if (cslFilesAsStrings.contains(cslFileAsString))
                throw new IllegalArgumentException("cslFileList already contains chosen .csl-file.");
            else
                cslFilesAsStrings.add(cslFileAsString);
        }
    }

    /**
     * adds 1 template to clientFileModel
     *
     * @param templateToAdd template you want to add
     * @throws IOException if file-problems appear
     */
    public void addTemplateAsString(File templateToAdd) throws IOException {
        Objects.requireNonNull(templateToAdd, "(templateToAdd == null) in clientFileModel.addTemplateAsString()");
        String templateFileAsString = ClientFileHandler.readStringFromFile(templateToAdd);
        if (templatesAsStrings.contains(templateFileAsString))
            throw new IllegalArgumentException("templateList already contains chosen tempalte.");
        else
            templatesAsStrings.add(templateFileAsString);
    }

    /**
     * adds n .bib-files to clientFileModel
     *
     * @param bibFiles .bib-files you want to add
     * @throws IOException if at least one .bib-file is invalid
     */
    public void addBibFiles(Collection<File> bibFiles) throws IOException {
        Objects.requireNonNull(bibFiles, "(bibFiles == null) in clientFileModel.addBibFiles()");
        for (File currentBibFile : bibFiles)
            addBibFile(currentBibFile);
    }

    /**
     * adds n .csl-files to clientFileModel
     *
     * @param cslFiles .csl-files you want to add
     * @throws IOException if at least one .csl-files causes problems
     */
    public void addCslFilesAsStrings(Collection<File> cslFiles) throws IOException {
        Objects.requireNonNull(cslFiles, "(cslFiles == null) in clientFileModel.addCslFilesAsString()");
        for (File currentCslFile : cslFiles)
            addCslFileAsString(currentCslFile);
    }

    /**
     * adds n templates to clientFileModel
     *
     * @param templates tempaltes you want to add
     * @throws IOException if at least one template causes problems
     */
    public void addTemplatesAsStrings(Collection<File> templates) throws IOException {
        Objects.requireNonNull(templates, "(templates == null) in clientFileModel.addTemplatesAsStrings()");
        for (File currentTemplate : templates)
            addTemplateAsString(currentTemplate);
    }

    /**
     * removes 1 .bib-File from clientFileModel
     *
     * @param bibFileToRemove .bib-file you want to remove
     */
    public void removeBibFile(File bibFileToRemove) {
        Objects.requireNonNull(bibFileToRemove, "(bibFileToRemove == null) in clientFileModel.removeBibFile()");
        if (!bibFiles.contains(bibFileToRemove))
            throw new IllegalArgumentException("bibFileList doesn't contain chosen .bib-file to remove.");
        bibFiles.remove(bibFileToRemove);
    }

    /**
     * removes n .bib-files from clientFileModel
     *
     * @param bibFilesToRemove .bib-files you want to remove
     */
    public void removeBibFiles(Collection<File> bibFilesToRemove) {
        for (File currentBibFile : bibFilesToRemove) {
            removeBibFile(currentBibFile);
        }
    }

    /**
     * removes ALL .bib-files from clientFileModel
     */
    public void clearBibFiles() {
        bibFiles.clear();
    }

    /**
     * removes 1 .csl-file from clientFileModel
     *
     * @param cslFileToRemove .csl-file you want to remove
     * @throws IOException if file-problems appear
     */
    public void removeCslFile(File cslFileToRemove) throws IOException {
        Objects.requireNonNull(cslFileToRemove, "(cslFileToRemove == null) in clientFileModel.removeCslFile()");
        String cslFileToRemoveAsString = ClientFileHandler.readStringFromFile(cslFileToRemove);
        if (!cslFilesAsStrings.contains(cslFileToRemoveAsString))
            throw new IllegalArgumentException("cslFileList doesn't contain chosen .csl-file to remove.");
        cslFilesAsStrings.remove(cslFileToRemoveAsString);
    }

    /**
     * removes n .csl-files from clientFileModel
     */
    public void removeCslFiles(Collection<File> cslFilesToRemove) throws IOException {
        for (File currentCslFile : cslFilesToRemove) {
            removeCslFile(currentCslFile);
        }
    }

    /**
     * removes ALL .csl-files from clientFileModel
     */
    public void clearCslFiles() {
        cslFilesAsStrings.clear();
    }

    /**
     * removes 1 template from clientFileModel
     *
     * @param templateToRemove template you want to remove
     * @throws IOException if file-problems appear
     */
    public void removeTemplate(File templateToRemove) throws IOException {
        Objects.requireNonNull(templateToRemove, "(templateToRemove == null) in clientFileModel.removeTemplate()");
        String templateFileToRemoveAsString = ClientFileHandler.readStringFromFile(templateToRemove);
        if (!templatesAsStrings.contains(templateFileToRemoveAsString))
            throw new IllegalArgumentException("templateFileList doesn't contain chosen template to remove.");
        templatesAsStrings.remove(templateFileToRemoveAsString);
    }

    /**
     * removes n templates from clientFileModel
     *
     * @param templatesToRemove templates you want to remove
     */
    public void removeTemplates(Collection<File> templatesToRemove) throws IOException {
        for (File currentTemplate : templatesToRemove) {
            removeTemplate(currentTemplate);
        }
    }

    /**
     * removes ALL templates from clientFileModel
     */
    public void clearTemplates() {
        templatesAsStrings.clear();
    }

    @Override
    public String toString() {
        return (System.lineSeparator() + "ClientFileModel '" + clientID + "' has " + bibFiles.size() +
                " bibFiles, " + cslFilesAsStrings.size() + " cslFiles and "
                + templatesAsStrings.size() + " templates." + System.lineSeparator());
    }
}
