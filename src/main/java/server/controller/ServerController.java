package server.controller;

import client.controller.Client;
import global.logging.Log;
import global.logging.LogLevel;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import microservice.MicroService;
import server.events.*;
import server.modules.Server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeoutException;

/**
 * @author Maximilian Schirm
 *         created on 08.12.2016
 */

public class ServerController implements EventListener {

    private class Console extends OutputStream {

        TextArea textArea;

        private Console(TextArea textArea) {
            this.textArea = textArea;
        }

        public void clearTextArea() {
            textArea.clear();
        }

        @Override
        public void write(int b) throws IOException {
            textArea.appendText(String.valueOf((char) b));
        }
    }

    private Server server;
    private Console consoleStream;

    @FXML
    ChoiceBox<LogLevel> logLevelChoiceBox;

    @FXML
    Label serverAdressLabel;

    @FXML
    TextArea serverConsoleTextArea;

    @FXML
    ListView<Client> clientListView;

    @FXML
    ListView<MicroService> microServiceListView;

    @FXML
    ListView<String> clientRequestListView;

    public ServerController() {
        try {
            server = new Server();
        } catch (IOException | TimeoutException e) {
            Log.log("Failed to initialize Server", e);
        }
    }

    @FXML
    public void initialize() {
        //Initialize the Console and the Log
        consoleStream = new Console(serverConsoleTextArea);
        Log.alterOutputStream(consoleStream);

        //Fill UI elements
        logLevelChoiceBox.getItems().addAll(LogLevel.values());
        //TODO : Uncomment. Only Commented for incompatibility with local VM
        logLevelChoiceBox.onActionProperty().set(eventhandler -> Log.alterMinimumRequiredLevel(logLevelChoiceBox.getValue()));

        try {
            serverAdressLabel.setText(Inet4Address.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            Log.log("Failed to display Host IP", e);
        }

        Log.log("Initialized the Server.", LogLevel.INFO);
    }

    @Override
    public void notify(Event toNotify) {
        if (toNotify instanceof ClientRegisteredEvent) {
            Client toAdd = ((ClientRegisteredEvent) toNotify).getRegisteredClient();
            Log.log("Registered Client with ID " + toAdd.getID(), LogLevel.INFO);
            clientListView.getItems().add(toAdd);
        } else if (toNotify instanceof ClientDisconnectedEvent) {
            Client toRemove = ((ClientDisconnectedEvent) toNotify).getDisconnectedClient();
            Log.log("Disconnected Client with ID " + toRemove.getID(), LogLevel.WARNING);
            clientListView.getItems().remove(toRemove);
        } else if (toNotify instanceof MicroServiceConnectedEvent) {
            MicroService toAdd = ((MicroServiceConnectedEvent) toNotify).getConnectedSvc();
            Log.log("Registered Microservice with ID " + toAdd.getID(), LogLevel.WARNING);
            microServiceListView.getItems().add(toAdd);
        } else if (toNotify instanceof MicroServiceDisconnectedEvent) {
            MicroService toRemove = ((MicroServiceDisconnectedEvent) toNotify).getDisconnectedSvc();
            Log.log("Unregistered Microservice with ID " + toRemove.getID(), LogLevel.WARNING);
            microServiceListView.getItems().remove(toRemove);
        } else if (toNotify instanceof RequestStoppedEvent) {
            String toRemoveClientID = ((RequestStoppedEvent) toNotify).getStoppedRequestClientID();
            Log.log("Removed Request with ID " + toRemoveClientID, LogLevel.WARNING);
            clientRequestListView.getItems().remove(toRemoveClientID);
        }
    }

    @Override
    public Set<Class<? extends Event>> getEvents() {
        return new HashSet(Arrays.asList(ClientRegisteredEvent.class, ClientDisconnectedEvent.class, MicroServiceConnectedEvent.class, MicroServiceDisconnectedEvent.class, RequestStoppedEvent.class));
    }

    public void removeMicroserviceButtonPressed() {
        if (microServiceListView.getSelectionModel().getSelectedItem() != null) {
            MicroService serviceToRemove = microServiceListView.getSelectionModel().getSelectedItem();
            Log.log("Disconnecting MicroSerivce " + serviceToRemove.getID());
            EventManager.getInstance().publishEvent(new MicroserviceDisconnectionRequestEvent(serviceToRemove.getID()));
        }
    }

    public void removeClientButtonPressed() {
        if (clientListView.getSelectionModel().getSelectedItem() != null) {
            String clientToBlock = clientListView.getSelectionModel().getSelectedItem().getID();
            Log.log("Blocking Client " + clientToBlock);
            EventManager.getInstance().publishEvent(new ClientBlockRequestEvent(clientToBlock));
        }
    }

    public void cancelRequestButtonPressed() {
        if (clientRequestListView.getSelectionModel().getSelectedItem() != null) {
            String toStopClientID = clientRequestListView.getSelectionModel().getSelectedItem();
            Log.log("Cancelling Request " + toStopClientID);
            EventManager.getInstance().publishEvent(new RequestStoppedEvent(toStopClientID));
        }
    }

    public void clearLogButtonPressed() {
        clearConsole();
    }

    public void clearConsole() {
        consoleStream.clearTextArea();
    }
}
