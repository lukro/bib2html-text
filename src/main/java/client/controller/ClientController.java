package client.controller;

import client.model.Client;
import client.model.ResultFileExtension;
import global.controller.Console;
import global.logging.Log;
import global.logging.LogLevel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Popup;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author Maximilian Schirm, daan
 *         created on 09.12.2016
 */
public class ClientController {

    private Client client;
    private Console consoleStream;
    private final static String DEFAULT_OUT_DIR = System.getProperty("user.home");

    public ClientController() {
        try {
            client = new Client();
        } catch (IOException e) {
            Log.log("Failed to initialize Client instance", e);
            System.exit(1);
        }
    }

    @FXML
    private TextArea clientConsoleTextArea;

    @FXML
    private ListView<File> bibFilesListView;

    @FXML
    private ListView<File> cslFilesListView;

    @FXML
    private ListView<File> templateListView;

    @FXML
    private TextField secretKeyTextField;

    @FXML
    private ComboBox<ResultFileExtension> outputFileTypeComboBox;

    @FXML
    private TextField outputDirectoryTextField;

    @FXML
    private TextField serverAdressTextField;

    @FXML
    public void initialize() {
        consoleStream = new Console(clientConsoleTextArea);
        Log.alterOutputStream(consoleStream);

        Log.log("Initializing Client..");

        //Init here.
        if (client == null)
            Log.log("Client was not properly initialized! Instance broken!", LogLevel.ERROR);

        //Set default output dir
        outputDirectoryTextField.setText(DEFAULT_OUT_DIR);
        client.setOutputDirectory(DEFAULT_OUT_DIR);


        outputFileTypeComboBox.getItems().addAll(ResultFileExtension.values());
        outputFileTypeComboBox.getSelectionModel().select(0);
        outputFileTypeComboBox.setOnAction((event) -> client.setResultFileExtension(outputFileTypeComboBox.getValue()));

        Log.log("Client Initialized.");
    }

    //GENERAL BUTTONS

    @FXML
    public void startConversionButtonPressed() {
        try {
            client.sendClientRequest();
        } catch (Exception e) {
            Log.log("Failed to send Client Request", e);
        }
    }

    @FXML
    public void connectToServerButtonPressed() {
        String serverAdress = serverAdressTextField.getText();
        Log.log("Connecting to server @" + serverAdress);
        if (client.connectToHost(serverAdress))
            Log.log("Successfully connected to Host!", LogLevel.INFO);
        else
            Log.log("Failed to connect to that Host!", LogLevel.WARNING);
    }

    @FXML
    public void chooseOutputDirectoryButtonPressed() {
        DirectoryChooser outputDirectoryChooser = new DirectoryChooser();
        outputDirectoryChooser.setTitle("Select an output directory...");
        File outputDirNew = outputDirectoryChooser.showDialog(new Popup());
        if (outputDirNew == null) {
            Log.log("User aborted directory selection", LogLevel.INFO);
        } else {
            client.setOutputDirectory(outputDirNew.getAbsolutePath());
            outputDirectoryTextField.setText(outputDirNew.getAbsolutePath());
            Log.log("Selected new Output Directory " + outputDirNew.getAbsolutePath());
        }
    }

    @FXML
    public void addBibButtonPressed() {
        FileChooser bibChooser = new FileChooser();
        bibChooser.setTitle("Choose bib File(s)...");
        bibChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("BIB Files (*.bib)", "*.bib"));
        List<File> chosenBib = bibChooser.showOpenMultipleDialog(new Popup());
        if (chosenBib == null) {
            Log.log("User aborted bib adding", LogLevel.INFO);
        } else {
            int successfullyAddedBibFilesCounter = 0;
            for (File currentBibFile : chosenBib) {
                if (client.getClientFileModel().addBibFile(currentBibFile)) {
                    bibFilesListView.getItems().add(currentBibFile);
                    successfullyAddedBibFilesCounter++;
                }
            }
            Log.log("Added " + successfullyAddedBibFilesCounter + " .bib-file(s)", LogLevel.INFO);
        }
    }

    @FXML
    public void removeBibButtonPressed() {
        File toRemove = bibFilesListView.getSelectionModel().getSelectedItem();
        if (client.getClientFileModel().removeBibFile(toRemove)) {
            bibFilesListView.getItems().remove(toRemove);
            Log.log("Removed .bib-file '" + toRemove + "' from the selection.", LogLevel.INFO);
        }
    }

    @FXML
    public void clearBibButtonPressed() {
        client.getClientFileModel().clearBibFiles();
        bibFilesListView.getItems().clear();
        Log.log("Removed all .bib-files from the selection.", LogLevel.INFO);
    }

    //CSL LIST BUTTONS


    @FXML
    public void addCslButtonPressed() {
        FileChooser cslChooser = new FileChooser();
        cslChooser.setTitle("Choose csl File(s)...");
        cslChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSL Files (*.csl)", "*.csl"));
        List<File> chosenBib = cslChooser.showOpenMultipleDialog(new Popup());
        if (chosenBib == null) {
            Log.log("User aborted csl adding", LogLevel.INFO);
        } else {
            int successfullyAddedCslFilesCounter = 0;
            for (File currentCslFile : chosenBib) {
                if (client.getClientFileModel().addCslFile(currentCslFile)) {
                    cslFilesListView.getItems().add(currentCslFile);
                    successfullyAddedCslFilesCounter++;
                }
            }
            Log.log("Added " + successfullyAddedCslFilesCounter + " .csl-file(s)", LogLevel.INFO);
        }
    }

    @FXML
    public void removeCslButtonPressed() {
        File toRemove = cslFilesListView.getSelectionModel().getSelectedItem();
        if (client.getClientFileModel().removeCslFile(toRemove)) {
            cslFilesListView.getItems().remove(toRemove);
            Log.log("Removed .csl-file '" + toRemove + "' from the selection.", LogLevel.INFO);
        }

    }

    @FXML
    public void clearCslButtonPressed() {
        client.getClientFileModel().clearCslFiles();
        cslFilesListView.getItems().clear();
        Log.log("Removed all .csl-files from the selection.", LogLevel.INFO);
    }

    @FXML
    public void clearLogButtonPressed() {
        consoleStream.clearTextArea();
    }

    @FXML
    public void addTmplButtonPressed(ActionEvent actionEvent) {
        FileChooser templateChooser = new FileChooser();
        templateChooser.setTitle("Select a Template file...");
        templateChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("TEMPLATE File (*.html)", "*.html"));
        File newTemplate = templateChooser.showOpenDialog(new Popup());
        if (newTemplate == null) {
            Log.log("User aborted template selection", LogLevel.INFO);
        } else if (client.getClientFileModel().addTemplate(newTemplate)) {
            templateListView.getItems().add(newTemplate);
            Log.log("User added a new template " + newTemplate.getAbsolutePath(), LogLevel.INFO);
        } else {
            Log.log("Could not add new template", LogLevel.WARNING);
        }
    }

    @FXML
    public void removeTmplButtonPressed(ActionEvent actionEvent) {
        File toRemove = templateListView.getSelectionModel().getSelectedItem();
        if (client.getClientFileModel().removeTemplate(toRemove)) {
            templateListView.getItems().remove(toRemove);
            Log.log("Removed template file '" + toRemove + "' from the selection.", LogLevel.INFO);
        }
    }

    @FXML
    public void clearTmplButtonPressed(ActionEvent actionEvent) {
        client.getClientFileModel().clearTemplates();
        templateListView.getItems().clear();
        Log.log("Removed all template files from the selection.", LogLevel.INFO);
    }

    public void setSecretKeyButtonPressed(ActionEvent actionEvent) {
        client.setKeyToUse(secretKeyTextField.getText());
    }
}