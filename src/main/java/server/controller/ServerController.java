package server.controller;

import client.controller.Client;
import global.logging.Log;
import global.logging.LogLevel;
import global.model.IClientRequest;
import javafx.event.ActionEvent;
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
    ListView<IClientRequest> clientRequestListView;

    public ServerController() {
        try {
            server = new Server();
        } catch (IOException | TimeoutException e) {
            Log.log("Failed to initialize Server", e);
        }
    }

    @FXML
    public void initialize() {
        consoleStream = new Console(serverConsoleTextArea);
        Log.alterOutputStream(consoleStream);

        logLevelChoiceBox.getItems().addAll(LogLevel.values());
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
            Log.log("Disconnected Client with ID " + toRemove.getID(), LogLevel.INFO);
            clientListView.getItems().remove(toRemove);
        } else if (toNotify instanceof MicroServiceConnectedEvent) {
            MicroService toAdd = ((MicroServiceConnectedEvent) toNotify).getConnectedSvc();
            Log.log("Registered Microservice with ID " + toAdd.getID(), LogLevel.INFO);
            microServiceListView.getItems().add(toAdd);
        } else if (toNotify instanceof MicroServiceDisconnectedEvent) {
            MicroService toRemove = ((MicroServiceDisconnectedEvent) toNotify).getDisconnectedSvc();
            Log.log("Unregistered Microservice with ID " + toRemove.getID(), LogLevel.INFO);
            microServiceListView.getItems().remove(toRemove);
        } else if (toNotify instanceof RequestStoppedEvent){
            IClientRequest toRemove = ((StopRequestEvent) toNotify).getRequest();
            Log.log("Removed Request with ID " + toRemove.getClientID());
            clientRequestListView.getItems().remove(toRemove);
        }
    }

    @Override
    public Set<Class<? extends Event>> getEvents() {
        return new HashSet(Arrays.asList(ClientRegisteredEvent.class, ClientDisconnectedEvent.class, MicroServiceConnectedEvent.class, MicroServiceDisconnectedEvent.class));
    }

    public void removeMicroserviceButtonPressed(ActionEvent actionEvent) {
        if(microServiceListView.getSelectionModel().getSelectedItem() != null){
            MicroService serviceToRemove = microServiceListView.getSelectionModel().getSelectedItem();
            Log.log("Disconnecting MicroSerivce " + serviceToRemove.getID());
            EventManager.getInstance().publishEvent(new MicroserviceDisconnectionRequestEvent(serviceToRemove));
        }
    }

    public void removeClientButtonPressed(ActionEvent actionEvent) {
        if(clientListView.getSelectionModel().getSelectedItem() != null) {
            String clientToBlock = clientListView.getSelectionModel().getSelectedItem().getID();
            Log.log("Blocking Client " + clientToBlock);
            EventManager.getInstance().publishEvent(new ClientBlockRequestEvent(clientToBlock));
        }
    }

    public void cancelRequestButtonPressed(ActionEvent actionEvent) {
        if(clientRequestListView.getSelectionModel().getSelectedItem() != null){
            IClientRequest toStop = clientRequestListView.getSelectionModel().getSelectedItem();
            Log.log("Cancelling Request " + toStop.getClientID());
            EventManager.getInstance().publishEvent(new StopRequestEvent(toStop));
        }
    }

    public OutputStream getConsolePrintStream() {
        return consoleStream;
    }
}
