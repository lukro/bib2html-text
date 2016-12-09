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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeoutException;

/**
 * @author Maximilian Schirm
 *         created on 08.12.2016
 */

public class ServerController implements EventListener {

    @Override
    public void notify(Event toNotify) {
        if(toNotify instanceof ClientRegisteredEvent){
            Client toAdd = ((ClientRegisteredEvent) toNotify).getRegisteredClient();
            Log.log("Registered Client with ID " + toAdd.getID(), LogLevel.INFO);
            clientListView.getItems().add(toAdd);
        }
        else if(toNotify instanceof ClientDisconnectedEvent){
            Client toRemove = ((ClientDisconnectedEvent) toNotify).getDisconnectedClient();
            Log.log("Disconnected Client with ID " + toRemove.getID(), LogLevel.INFO);
            clientListView.getItems().remove(toRemove);
        }
        else if(toNotify instanceof MicroServiceConnectedEvent){
            MicroService toAdd = ((MicroServiceConnectedEvent) toNotify).getConnectedSvc();
            Log.log("Registered Microservice with ID " + toAdd.getRoutingKey(), LogLevel.INFO);
            microServiceListView.getItems().add(toAdd);
        }
        else if(toNotify instanceof MicroServiceDisconnectedEvent){
            MicroService toRemove = ((MicroServiceDisconnectedEvent) toNotify).getDisconnectedSvc();
            Log.log("Unregistered Microservice with ID " + toRemove.getRoutingKey(), LogLevel.INFO);
            microServiceListView.getItems().remove(toRemove);
        }
    }

    @Override
    public Set<Class<? extends Event>> getEvents() {
        return new HashSet(Arrays.asList(ClientRegisteredEvent.class, ClientDisconnectedEvent.class, MicroServiceConnectedEvent.class, MicroServiceDisconnectedEvent.class));
    }

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
            Log.log("Failed to display Host IP",e);
        }
        ;

        Log.log("Initialized the Server.", LogLevel.INFO);
    }

    public void removeMicroserviceButtonPressed(ActionEvent actionEvent) {

    }

    public void removeClientButtonPressed(ActionEvent actionEvent) {

    }

    public void cancelJobButtonPressed(ActionEvent actionEvent) {

    }

    public OutputStream getConsolePrintStream() {
        return consoleStream;
    }

    public void addClient(Client client){

    }
}
