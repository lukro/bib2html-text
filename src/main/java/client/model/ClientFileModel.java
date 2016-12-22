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
        try {
            Objects.requireNonNull(bibFileToAdd);
        } catch (NullPointerException e) {
            Log.log("(bibFileToAdd == null) in clientFileModel.addBibFile()", LogLevel.WARNING);
            return false;
        }
        if (bibFiles.contains(bibFileToAdd))
            return false;
        try {
            if (ClientFileHandler.isValidBibFile(bibFileToAdd)) {
                try {
                    bibFiles.add(bibFileToAdd);
                    return true;
                } catch (Exception e) {
                    //expandability & maintenance
                    Log.log("couldn't add .bib-file '" + bibFileToAdd.getAbsolutePath() + "' to file-model.", LogLevel.WARNING);
                    return false;
                }
            }
        } catch (FileNotFoundException e) {
            Log.log(".bib-file '" + bibFileToAdd.getAbsolutePath() + "' not found.", LogLevel.WARNING);
            return false;
        }
        Log.log("file '" + bibFileToAdd.getAbsolutePath() + "' isn't a valid .bib-file.", LogLevel.WARNING);
        return false;
    }

    /**
     * adds 1 .csl-file to model
     *
     * @param cslFileToAdd .csl-file you want to add
     * @return true if .csl-file added successfully or model already contains it, else: false
     */
    public boolean addCslFile(File cslFileToAdd) {
        try {
            Objects.requireNonNull(cslFileToAdd);
        } catch (NullPointerException e) {
            Log.log("(cslFileToAdd == null) in clientFileModel.addCslFile()", LogLevel.WARNING);
            return false;
        }
        if (ClientFileHandler.isValidCslFile(cslFileToAdd)) {
            if (cslFiles.contains(cslFileToAdd))
                return false;
            else {
                try {
                    cslFiles.add(cslFileToAdd);
                    return true;
                } catch (Exception e) {
                    //expandability & maintenance
                    Log.log("couldn't add .csl-file '" + cslFileToAdd.getAbsolutePath() + "' to file-model.", LogLevel.WARNING);
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
        try {
            Objects.requireNonNull(templateToAdd);
        } catch (NullPointerException e) {
            Log.log("(templateToAdd == null) in clientFileModel.addTemplate()", LogLevel.WARNING);
            return false;
        }
        if (templates.contains(templateToAdd))
            return false;
        else
            try {
                templates.add(templateToAdd);
                return true;
            } catch (Exception e) {
                //expandability & maintenance
                Log.log("couldn't add template '" + templateToAdd.getAbsolutePath() + "' to file-model.", LogLevel.WARNING);
                return false;
            }
    }


//    /**
//     * adds n .bib-files to model
//     *
//     * @param bibFiles .bib-files you want to add
//     * @return true if n .bib-files added successfully, false if at least one file wasn't added successfully
//     */
//    public boolean addBibFiles(Collection<File> bibFiles) {
//        try {
//            Objects.requireNonNull(bibFiles);
//        } catch (NullPointerException e) {
//            Log.log("(bibFiles == null) in clientFileModel.addBibFiles()", LogLevel.WARNING);
//            return false;
//        }
//        boolean result = true;
//        for (File currentBibFile : bibFiles)
//            if (!addBibFile(currentBibFile))
//                result = false;
//        return result;
//    }


//    /**
//     * adds n .csl-files to model
//     *
//     * @param cslFiles .csl-files you want to add
//     * @return true if n .csl-files added successfully, false if at least one file wasn't added successfully
//     */
//    public boolean addCslFiles(Collection<File> cslFiles) {
//        try {
//            Objects.requireNonNull(cslFiles);
//        } catch (NullPointerException e) {
//            Log.log("(cslFiles == null) in clientFileModel.addCslFilesAsString()", LogLevel.WARNING);
//            return false;
//        }
//        boolean result = true;
//        for (File currentCslFile : cslFiles)
//            if (!addCslFile(currentCslFile))
//                result = false;
//        return result;
//    }

//    /**
//     * adds n templates to model
//     *
//     * @param templates templates you want to add
//     * @return true if n templates added successfully, false if at least one template wasn't added successfully
//     */
//    public boolean addTemplates(Collection<File> templates) {
//        try {
//            Objects.requireNonNull(templates);
//        } catch (NullPointerException e) {
//            Log.log("(templates == null) in clientFileModel.addTemplates()", LogLevel.WARNING);
//            return false;
//        }
//        boolean result = true;
//        for (File currentTemplate : templates)
//            if (!addTemplate(currentTemplate))
//                result = false;
//        return result;
//    }

    //'REMOVE'-methods:

    /**
     * removes 1 .bib-file from model
     *
     * @param bibFileToRemove .bib-file you want to remove
     * @return true if .bib-file removed successfully or model doesn't contain it, else: false
     */
    public boolean removeBibFile(File bibFileToRemove) {
        try {
            Objects.requireNonNull(bibFileToRemove);
        } catch (NullPointerException e) {
            Log.log("(bibFileToRemove == null) in clientFileModel.removeBibFile()", LogLevel.WARNING);
            return false;
        }
        if (!bibFiles.contains(bibFileToRemove))
            return false;
        try {
            bibFiles.remove(bibFileToRemove);
            return true;
        } catch (Exception e) {
            //expandability & maintenance
            Log.log("couldn't remove .bib-file from file-model.", LogLevel.WARNING);
            return false;
        }
    }

//    /**
//     * removes n .bib-files from model
//     *
//     * @param bibFiles .bib-files you want to remove
//     * @return true if n .bib-files removed successfully, false if at least one .bib-file wasn't removed successfully
//     */
//    public boolean removeBibFiles(Collection<File> bibFiles) {
//        try {
//            Objects.requireNonNull(bibFiles);
//        } catch (NullPointerException e) {
//            Log.log("(bibFiles== null) in clientFileModel.removeBibFiles()", LogLevel.WARNING);
//            return false;
//        }
//        boolean result = true;
//        for (File currentBibFile : bibFiles)
//            if (!removeBibFile(currentBibFile))
//                result = false;
//        return result;
//    }

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
            Log.log("couldn't clear .bib-files in file-model.", LogLevel.WARNING);
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
        try {
            Objects.requireNonNull(cslFileToRemove);
        } catch (NullPointerException e) {
            Log.log("(cslFileToRemove == null) in clientFileModel.removeCslFile()", LogLevel.WARNING);
            return false;
        }
        if (!cslFiles.contains(cslFileToRemove))
            return false;
        try {
            cslFiles.remove(cslFileToRemove);
            return true;
        } catch (Exception e) {
            //expandability & maintenance
            Log.log("couldn't remove .csl-file from file-model.", LogLevel.WARNING);
            return false;
        }

    }

//    /**
//     * removes n .csl-files from model
//     *
//     * @param cslFiles .csl-files you want to remove
//     * @return true if n .csl-files removed successfully, false if at least one .csl-file wasn't removed successfully
//     */
//    public boolean removeCslFiles(Collection<File> cslFiles) {
//        try {
//            Objects.requireNonNull(cslFiles);
//        } catch (NullPointerException e) {
//            Log.log("(cslFileToRemove == null) in clientFileModel.removeCslFile()", LogLevel.WARNING);
//            return false;
//        }
//        boolean result = true;
//        for (File currentCslFile : cslFiles)
//            if (!removeCslFile(currentCslFile))
//                result = false;
//        return result;
//    }

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
            Log.log("couldn't clear .csl-files in file-model.", LogLevel.WARNING);
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
        try {
            Objects.requireNonNull(templateToRemove);
        } catch (NullPointerException e) {
            Log.log("(templateToRemove == null) in clientFileModel.removeTemplate()", LogLevel.WARNING);
            return false;
        }
        if (!templates.contains(templateToRemove))
            return false;
        try {
            templates.remove(templateToRemove);
            return true;
        } catch (Exception e) {
            //expandability & maintenance
            Log.log("couldn't remove tempalte from file-model.", LogLevel.WARNING);
            return false;
        }
    }

//    /**
//     * removes n templates from model
//     *
//     * @param templates templates you want to remove
//     * @return true if n templates removed successfully, false if at least one template wasn't removed successfully
//     */
//    public boolean removeTemplates(Collection<File> templates) {
//        try {
//            Objects.requireNonNull(templates);
//        } catch (NullPointerException e) {
//            Log.log("(templates == null) in clientFileModel.removeTemplates()", LogLevel.WARNING);
//            return false;
//        }
//        boolean result = true;
//        for (File currentTemplate : templates)
//            if (!removeTemplate(currentTemplate))
//                result = false;
//        return result;
//    }

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
            Log.log("couldn't clear templates in file-model.", LogLevel.WARNING);
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
