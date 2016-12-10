package client.controller;

import global.logging.Log;
import global.logging.LogLevel;
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
import java.util.concurrent.TimeoutException;

/**
 * @author Maximilian Schirm
 * @created 09.12.2016
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
    }

    public ClientController() {
        try {
            client = new Client();
        } catch (IOException | TimeoutException e) {
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
        //TODO : Client.connectToServer...
    }

    @FXML
    public void chooseOutputDirectoryButtonPressed() {
        DirectoryChooser outputDirectoryChooser = new DirectoryChooser();
        outputDirectoryChooser.setTitle("Select an output directory...");
        File outputDirNew = outputDirectoryChooser.showDialog(new Popup());
        if (outputDirNew == null) {
            Log.log("User aborted directory selection");
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
        File newTemplate = templateChooser.showOpenDialog(new Popup());
        try {
            client.getClientFileModel().addTemplate(newTemplate);
            templateDirectoryTextField.setText(newTemplate.getAbsolutePath());
            Log.log("User selected new template " + newTemplate.getAbsolutePath());
        } catch (IOException e) {
            Log.log("Could not set new template", e);
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
            Log.log("User aborted bib adding");
        } else {
            chosenBib.forEach(file -> {
                try {
                    client.getClientFileModel().addBibFile(file);
                } catch (IOException e) {
                    Log.log("Could not add bibfile " + file.getAbsolutePath(), e);
                }
            });

            Log.log("Added " + chosenBib.size() + " bib File(s)", LogLevel.INFO);
        }
    }

    @FXML
    public void removeBibButtonPressed() {
        File toRemove = bibFilesListView.getSelectionModel().getSelectedItem();
        client.getClientFileModel().removeBibFile(toRemove);
        Log.log("Removed bib " + toRemove + " from the selection.");
    }

    @FXML
    public void clearBibButtonPressed() {
        client.getClientFileModel().removeBibFiles(client.getClientFileModel().getBibFiles());
        Log.log("Removed all bib files from the selection.");
    }


    //CSL LIST BUTTONS

    @FXML
    public void addCslButtonPressed() {
        FileChooser cslChooser = new FileChooser();
        cslChooser.setTitle("Choose csl File(s)...");
        cslChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSL Files (*.csl)", "*.csl"));
        List<File> chosenBib = cslChooser.showOpenMultipleDialog(new Popup());
        if (chosenBib == null) {
            Log.log("User aborted csl adding");
        } else {
            chosenBib.forEach(file -> {
                try {
                    client.getClientFileModel().addCslFileAsString(file);
                } catch (IOException e) {
                    Log.log("Could not add cslfile " + file.getAbsolutePath(), e);
                }
            });

            Log.log("Added " + chosenBib.size() + " csl File(s)");
        }
    }

    @FXML
    public void removeCslButtonPressed() {
        File toRemove = cslFilesListView.getSelectionModel().getSelectedItem();
        //TODO : wait for daan to update
        //client.getClientFileModel().removeCslFile(toRemove);
        Log.log("Removed csl " + toRemove + " from the selection.");
    }

    @FXML
    public void clearCslButtonPressed() {
        //client.getClientFileModel().removeCslFiles(client.getClientFileModel().g);
        Log.log("Removed all csl files from the selection.");
    }
}