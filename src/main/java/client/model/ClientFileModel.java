package client.model;

import client.controller.ClientFileHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * @author daan
 *         created on 11/30/16.
 */
public class ClientFileModel {

    //TODO: change data-collections to 'File'-object and implement support for that in all dependent classes
    private final String clientID;
    private ArrayList<File> bibFiles = new ArrayList<>();
    private ArrayList<File> cslFiles = new ArrayList<>();
    private ArrayList<File> templates = new ArrayList<>();

    public ClientFileModel(String clientID) {
        this.clientID = clientID;
    }

    public String getClientID() {
        return clientID;
    }

    public ArrayList<File> getBibFiles() {
        return bibFiles;
    }

    public ArrayList<File> getCslFiles() {
        return cslFiles;
    }

    public ArrayList<File> getTemplates() {
        return templates;
    }

    /**
     * @param bibFileToAdd .bib-file you want to add
     * @return true if file added successfully, else: false
     */
    public boolean addBibFile(File bibFileToAdd) {
        Objects.requireNonNull(bibFileToAdd, "(bibFileToAdd == null) in clientFileModel.addBibFile()");
        if (bibFiles.contains(bibFileToAdd))
            return true;
        else try {
            if (ClientFileHandler.isValidBibFile(bibFileToAdd)) {
                bibFiles.add(bibFileToAdd);
                return true;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    /**
     * adds 1 .csl-file to model
     *
     * @param cslFileToAdd .csl-file you want to add
     * @return true if file added successfully, else: false
     */
    public boolean addCslFileAsString(File cslFileToAdd) {
        Objects.requireNonNull(cslFileToAdd, "(cslFileToAdd == null) in clientFileModel.addCslFileAsString()");
        if (ClientFileHandler.isValidCslFile(cslFileToAdd)) {
            if (cslFiles.contains(cslFileToAdd))
                return true;
            else {
                cslFiles.add(cslFileToAdd);
                return true;
            }
        }
        return false;
    }

    /**
     * adds 1 template to model
     *
     * @param templateToAdd template you want to add
     * @return true if file added successfully, else: false
     */
    public boolean addTemplate(File templateToAdd) {
        Objects.requireNonNull(templateToAdd, "(templateToAdd == null) in clientFileModel.addTemplate()");
        if (templates.contains(templateToAdd))
            return true;
        else
            templates.add(templateToAdd);
        return true;
    }


    /**
     * adds n .bib-files to model
     *
     * @param bibFiles .bib-fileS you want to add
     * @return true if ALL fileS added successfully, false if at least one file wasn't added successfully
     */
    public boolean addBibFiles(Collection<File> bibFiles) {
        Objects.requireNonNull(bibFiles, "(bibFiles == null) in clientFileModel.addBibFiles()");
        for (File currentBibFile : bibFiles) {

        }
        return false;
    }


    /**
     * adds n .csl-files to clientFileModel
     *
     * @param cslFiles .csl-files you want to add
     * @throws IOException if at least one .csl-files causes problems
     */
    public void addCslFilesAsStrings(Collection<File> cslFiles) {
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
    public void addTemplatesAsStrings(Collection<File> templates) {
        Objects.requireNonNull(templates, "(templates == null) in clientFileModel.addTemplatesAsStrings()");
        for (File currentTemplate : templates)
            addTemplate(currentTemplate);
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
    public void removeCslFile(File cslFileToRemove) {
        Objects.requireNonNull(cslFileToRemove, "(cslFileToRemove == null) in clientFileModel.removeCslFile()");
        String cslFileToRemoveAsString = ClientFileHandler.readStringFromFile(cslFileToRemove);
        if (!cslFiles.contains(cslFileToRemoveAsString))
            throw new IllegalArgumentException("cslFileList doesn't contain chosen .csl-file to remove.");
        cslFiles.remove(cslFileToRemoveAsString);
    }

    /**
     * removes n .csl-files from clientFileModel
     */
    public void removeCslFiles(Collection<File> cslFilesToRemove) {
        for (File currentCslFile : cslFilesToRemove) {
            removeCslFile(currentCslFile);
        }
    }

    /**
     * removes ALL .csl-files from clientFileModel
     */
    public void clearCslFiles() {
        cslFiles.clear();
    }

    /**
     * removes 1 template from clientFileModel
     *
     * @param templateToRemove template you want to remove
     * @throws IOException if file-problems appear
     */
    public void removeTemplate(File templateToRemove) {
        Objects.requireNonNull(templateToRemove, "(templateToRemove == null) in clientFileModel.removeTemplate()");
        String templateFileToRemoveAsString = ClientFileHandler.readStringFromFile(templateToRemove);
        if (!templates.contains(templateFileToRemoveAsString))
            throw new IllegalArgumentException("templateFileList doesn't contain chosen template to remove.");
        templates.remove(templateFileToRemoveAsString);
    }

    /**
     * removes n templates from clientFileModel
     *
     * @param templatesToRemove templates you want to remove
     */
    public void removeTemplates(Collection<File> templatesToRemove) {
        for (File currentTemplate : templatesToRemove) {
            removeTemplate(currentTemplate);
        }
    }

    /**
     * removes ALL templates from clientFileModel
     */
    public void clearTemplates() {
        templates.clear();
    }

    @Override
    public String toString() {
        return (System.lineSeparator() + "ClientFileModel '" + clientID + "' has " + bibFiles.size() +
                " bibFiles, " + cslFiles.size() + " cslFiles and "
                + templates.size() + " templates." + System.lineSeparator());
    }
}
