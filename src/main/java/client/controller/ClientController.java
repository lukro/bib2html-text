package client.controller;

import global.logging.Log;
import global.logging.LogLevel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Popup;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * @author Maximilian Schirm, daan
 *         created on 09.12.2016
 */
public class ClientController {

    private Client client;
    private Console consoleStream;

    public class Console extends OutputStream {

        TextArea textArea;

        public Console(TextArea textArea) {
            this.textArea = textArea;
        }

        @Override
        public void write(int b) throws IOException {
            textArea.appendText(String.valueOf((char) b));
        }

        public void clearTextArea() {
            textArea.clear();
        }

    }
    public ClientController() {
        try {
            client = new Client();
        } catch (IOException e) {
            Log.log("Failed to initialize Client instance", e);
            System.exit(1);
        }
    }

    @FXML
    TextArea clientConsoleTextArea;

    @FXML
    ListView<File> bibFilesListView;

    @FXML
    ListView<File> cslFilesListView;

    @FXML
    TextField templateDirectoryTextField;

    @FXML
    TextField outputDirectoryTextField;

    @FXML
    TextField serverAdressTextField;

    @FXML
    public void initialize() {
        consoleStream = new Console(clientConsoleTextArea);
        Log.alterOutputStream(consoleStream);

        Log.log("Initializing Client..");

        //Init here.
        if (client == null)
            Log.log("Client was not properly initialized! Instance broken!", LogLevel.ERROR);

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
    public void chooseTemplateButtonPressed() {
        FileChooser templateChooser = new FileChooser();
        templateChooser.setTitle("Select a Template file...");
        templateChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("TEMPLATE File (*.latex)","*.latex"));
        File newTemplate = templateChooser.showOpenDialog(new Popup());
        if (client.getClientFileModel().addTemplate(newTemplate)) {
            templateDirectoryTextField.setText(newTemplate.getAbsolutePath());
            Log.log("User selected new template " + newTemplate.getAbsolutePath(), LogLevel.INFO);
        } else {
            Log.log("Could not set new template", LogLevel.WARNING);
        }
    }

    //BIB LIST BUTTONS


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

    public void clearLogButtonPressed() {
        clearConsole();
    }

    public void clearConsole() {
        consoleStream.clearTextArea();
    }
}