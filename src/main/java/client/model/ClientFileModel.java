package client.model;

import client.controller.ClientFileHandler;
import global.logging.Log;
import global.logging.LogLevel;

import java.io.File;
import java.io.FileNotFoundException;
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

    //'ADD'-methods:

    /**
     * adds 1 .bib-file to model
     *
     * @param bibFileToAdd .bib-file you want to add
     * @return true if .bib-file added successfully or model already contains it, else: false
     */
    public boolean addBibFile(File bibFileToAdd) {
        Objects.requireNonNull(bibFileToAdd, "(bibFileToAdd == null) in clientFileModel.addBibFile()");
        if (bibFiles.contains(bibFileToAdd))
            return true;
        try {
            if (ClientFileHandler.isValidBibFile(bibFileToAdd)) {
                try {
                    bibFiles.add(bibFileToAdd);
                    return true;
                } catch (Exception e) {
                    //expandability & maintenance
                    Log.log("couldn't add .bib-file '" + bibFileToAdd.getAbsolutePath() + "' to file-model.", e);
                    return false;
                }
            }
        } catch (FileNotFoundException e) {
            Log.log(".bib-file '" + bibFileToAdd.getAbsolutePath() + "' not found.", e);
            return false;
        }
        Log.log("file '" + bibFileToAdd.getAbsolutePath() + "' wasn't a valid .bib-file.", LogLevel.WARNING);
        return false;
    }

    /**
     * adds 1 .csl-file to model
     *
     * @param cslFileToAdd .csl-file you want to add
     * @return true if .csl-file added successfully or model already contains it, else: false
     */
    public boolean addCslFile(File cslFileToAdd) {
        Objects.requireNonNull(cslFileToAdd, "(cslFileToAdd == null) in clientFileModel.addCslFile()");
        if (ClientFileHandler.isValidCslFile(cslFileToAdd)) {
            if (cslFiles.contains(cslFileToAdd))
                return true;
            else {
                try {
                    cslFiles.add(cslFileToAdd);
                    return true;
                } catch (Exception e) {
                    //expandability & maintenance
                    Log.log("couldn't add .csl-file '" + cslFileToAdd.getAbsolutePath() + "' to file-model.", e);
                    return false;
                }

            }
        }
        return false;
    }

    /**
     * adds 1 template to model
     *
     * @param templateToAdd template you want to add
     * @return true if template added successfully or model already contains it, else: false
     */
    public boolean addTemplate(File templateToAdd) {
        Objects.requireNonNull(templateToAdd, "(templateToAdd == null) in clientFileModel.addTemplate()");
        if (templates.contains(templateToAdd))
            return true;
        else
            try {
                templates.add(templateToAdd);
                return true;
            } catch (Exception e) {
                //expandability & maintenance
                Log.log("couldn't add template '" + templateToAdd.getAbsolutePath() + "' to file-model.", e);
                return false;
            }
    }


    /**
     * adds n .bib-files to model
     *
     * @param bibFiles .bib-files you want to add
     * @return true if n .bib-files added successfully, false if at least one file wasn't added successfully
     */
    public boolean addBibFiles(Collection<File> bibFiles) {
        Objects.requireNonNull(bibFiles, "(bibFiles == null) in clientFileModel.addBibFiles()");
        boolean result = true;
        for (File currentBibFile : bibFiles)
            if (!addBibFile(currentBibFile))
                result = false;
        return result;
    }


    /**
     * adds n .csl-files to model
     *
     * @param cslFiles .csl-files you want to add
     * @return true if n .csl-files added successfully, false if at least one file wasn't added successfully
     */
    public boolean addCslFiles(Collection<File> cslFiles) {
        Objects.requireNonNull(cslFiles, "(cslFiles == null) in clientFileModel.addCslFilesAsString()");
        boolean result = true;
        for (File currentCslFile : cslFiles)
            if (!addCslFile(currentCslFile))
                result = false;
        return result;
    }

    /**
     * adds n templates to model
     *
     * @param templates templates you want to add
     * @return true if n templates added successfully, false if at least one template wasn't added successfully
     */
    public boolean addTemplates(Collection<File> templates) {
        Objects.requireNonNull(templates, "(templates == null) in clientFileModel.addTemplates()");
        boolean result = true;
        for (File currentTemplate : templates)
            if (!addTemplate(currentTemplate))
                result = false;
        return result;
    }

    //'REMOVE'-methods:

    /**
     * removes 1 .bib-file from model
     *
     * @param bibFileToRemove .bib-file you want to remove
     * @return true if .bib-file removed successfully or model doesn't contain it, else: false
     */
    public boolean removeBibFile(File bibFileToRemove) {
        Objects.requireNonNull(bibFileToRemove, "(bibFileToRemove == null) in clientFileModel.removeBibFile()");
        if (!bibFiles.contains(bibFileToRemove))
            return true;
        try {
            bibFiles.remove(bibFileToRemove);
            return true;
        } catch (Exception e) {
            //expandability & maintenance
            Log.log("couldn't remove .bib-file from file-model.", e);
            return false;
        }
    }

    /**
     * removes n .bib-files from model
     *
     * @param bibFilesToRemove .bib-files you want to remove
     * @return true if n .bib-files removed successfully, false if at least one .bib-file wasn't removed successfully
     */
    public boolean removeBibFiles(Collection<File> bibFilesToRemove) {
        boolean result = true;
        for (File currentBibFile : bibFilesToRemove)
            if (!removeBibFile(currentBibFile))
                result = false;
        return result;
    }

    /**
     * removes ALL .bib-files from model
     *
     * @return true if ALL .bib-files removed successfully, else: false
     */
    public boolean clearBibFiles() {
        try {
            bibFiles.clear();
            return true;
        } catch (Exception e) {
            //expandability & maintenance
            Log.log("couldn't clear .bib-files in file-model.", e);
            return false;
        }
    }

    /**
     * removes 1 .csl-file from model
     *
     * @param cslFileToRemove .csl-file you want to remove
     * @return true if .csl-file removed successfully or model doesn't contain it, else: false
     */
    public boolean removeCslFile(File cslFileToRemove) {
        Objects.requireNonNull(cslFileToRemove, "(cslFileToRemove == null) in clientFileModel.removeCslFile()");
        if (!cslFiles.contains(cslFileToRemove))
            return true;
        try {
            cslFiles.remove(cslFileToRemove);
            return true;
        } catch (Exception e) {
            //expandability & maintenance
            Log.log("couldn't remove .csl-file from file-model.", e);
            return false;
        }

    }

    /**
     * removes n .csl-files from model
     *
     * @param cslFilesToRemove .csl-files you want to remove
     * @return true if n .csl-files removed successfully, false if at least one .csl-file wasn't removed successfully
     */
    public boolean removeCslFiles(Collection<File> cslFilesToRemove) {
        boolean result = true;
        for (File currentCslFile : cslFilesToRemove)
            if (!removeCslFile(currentCslFile))
                result = false;
        return result;
    }

    /**
     * removes ALL .csl-files from model
     *
     * @return true if ALL .csl-files removed successfully, else: false
     */
    public boolean clearCslFiles() {
        try {
            cslFiles.clear();
            return true;
        } catch (Exception e) {
            //expandability & maintenance
            Log.log("couldn't clear .csl-files in file-model.", e);
            return false;
        }
    }

    /**
     * removes 1 template from model
     *
     * @param templateToRemove template you want to remove
     * @return true if template removed successfully or model doesn't contain it, else: false
     */
    public boolean removeTemplate(File templateToRemove) {
        Objects.requireNonNull(templateToRemove, "(templateToRemove == null) in clientFileModel.removeTemplate()");
        if (!templates.contains(templateToRemove))
            return true;
        try {
            templates.remove(templateToRemove);
            return true;
        } catch (Exception e) {
            //expandability & maintenance
            Log.log("couldn't remove tempalte from file-model.", e);
            return false;
        }
    }

    /**
     * removes n templates from model
     *
     * @param templatesToRemove templates you want to remove
     * @return true if n templates removed successfully, false if at least one template wasn't removed successfully
     */
    public boolean removeTemplates(Collection<File> templatesToRemove) {
        boolean result = true;
        for (File currentTemplate : templatesToRemove)
            if (!removeTemplate(currentTemplate))
                result = false;
        return result;
    }

    /**
     * removes ALL templates from model
     *
     * @return true if ALL templates removed successfully, else: false
     */
    public boolean clearTemplates() {
        try {
            templates.clear();
            return true;
        } catch (Exception e) {
            //expandability & maintenance
            Log.log("couldn't clear templates in file-model.", e);
            return false;
        }
    }

    @Override
    public String toString() {
        return (System.lineSeparator() + "ClientFileModel '" + clientID + "' has " + bibFiles.size() +
                " bibFiles, " + cslFiles.size() + " cslFiles and "
                + templates.size() + " templates." + System.lineSeparator());
    }
}
