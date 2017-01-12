package server.controller;

import client.controller.Client;
import global.controller.Console;
import global.logging.Log;
import global.logging.LogLevel;
import global.model.IResult;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import microservice.MicroService;
import server.events.*;
import server.modules.Server;

import java.io.IOException;
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

public class ServerController implements IEventListener {

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
    ListView<String> microServiceListView;

    @FXML
    ListView<String> clientRequestListView;

    public ServerController() {
        Thread serverStartThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final String originalThreadName = Thread.currentThread().getName();
                    Thread.currentThread().setName(originalThreadName + " - ServerStartThread");
                    server = new Server();
                } catch (IOException | TimeoutException e) {
                    Log.log("Failed to initialize Server", e);
                }

            }
        });
        serverStartThread.start();
    }

    @FXML
    public void initialize() {
        //Initialize the Console and the Log
        consoleStream = new Console(serverConsoleTextArea);
        Log.alterOutputStream(consoleStream);
        Log.alterMinimumRequiredLevel(LogLevel.INFO);

        //Fill UI elements
        logLevelChoiceBox.getItems().addAll(LogLevel.values());
        logLevelChoiceBox.getSelectionModel().select(LogLevel.INFO);
        logLevelChoiceBox.onActionProperty().set(eventhandler -> Log.alterMinimumRequiredLevel(logLevelChoiceBox.getValue()));
        try {
            serverAdressLabel.setText(Inet4Address.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            Log.log("Failed to display Host IP", e);
        }

        EventManager.getInstance().registerListener(this);
        Log.log("Initialized the Server.", LogLevel.INFO);
    }

    @Override
    public void notify(IEvent toNotify) {
        if (toNotify instanceof ClientRegisteredEvent) {
            Client toAdd = ((ClientRegisteredEvent) toNotify).getRegisteredClient();
            Log.log("Registered Client with ID " + toAdd.getID(), LogLevel.INFO);
            Platform.runLater(() -> clientListView.getItems().add(toAdd));
        } else if (toNotify instanceof ClientDisconnectedEvent) {
            Client toRemove = ((ClientDisconnectedEvent) toNotify).getDisconnectedClient();
            Log.log("Disconnected Client with ID " + toRemove.getID(), LogLevel.WARNING);
            Platform.runLater(() -> clientListView.getItems().remove(toRemove));
        } else if (toNotify instanceof MicroServiceConnectedEvent) {
            String toAddID = ((MicroServiceConnectedEvent) toNotify).getConnectedSvcID();
            Log.log("Registered Microservice with ID " + toAddID, LogLevel.WARNING);
            Platform.runLater(() -> microServiceListView.getItems().add(toAddID));
        } else if (toNotify instanceof MicroServiceDisconnectedEvent) {
            MicroService toRemove = ((MicroServiceDisconnectedEvent) toNotify).getDisconnectedSvc();
            Log.log("Unregistered Microservice with ID " + toRemove.getID(), LogLevel.WARNING);
            Platform.runLater(() -> microServiceListView.getItems().remove(toRemove));
        } else if (toNotify instanceof RequestStoppedEvent) {
            String toRemoveClientID = ((RequestStoppedEvent) toNotify).getStoppedRequestClientID();
            Log.log("Removed Request with ID " + toRemoveClientID, LogLevel.WARNING);
            Platform.runLater(() -> clientRequestListView.getItems().remove(toRemoveClientID));
        } else if (toNotify instanceof RequestAcceptedEvent) {
            String toAddRequestString = ((RequestAcceptedEvent) toNotify).getRequestID() + " - Size : " + ((RequestAcceptedEvent) toNotify).getReqSize();
            Platform.runLater(() -> clientRequestListView.getItems().add(toAddRequestString));
        } else if (toNotify instanceof FinishedCollectingResultEvent) {
            IResult result = ((FinishedCollectingResultEvent) toNotify).getResult();
            String toRemoveString = result.getClientID() + " - Size : " + result.getFileContents().size();
            Platform.runLater(() -> clientRequestListView.getItems().remove(toRemoveString));
        }
    }

    @Override
    public Set<Class<? extends IEvent>> getEvents() {
        return new HashSet(Arrays.asList(ClientRegisteredEvent.class, ClientDisconnectedEvent.class,
                MicroServiceConnectedEvent.class, MicroServiceDisconnectedEvent.class,
                RequestStoppedEvent.class, RequestAcceptedEvent.class,
                FinishedCollectingResultEvent.class));
    }

    public void removeMicroserviceButtonPressed() {
        if (microServiceListView.getSelectionModel().getSelectedItem() != null) {
            String serviceIDToRemove = microServiceListView.getSelectionModel().getSelectedItem();
            Log.log("Disconnecting MicroSerivce " + serviceIDToRemove);
            EventManager.getInstance().publishEvent(new MicroserviceDisconnectionRequestEvent(serviceIDToRemove));
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
        consoleStream.clearTextArea();
    }
}
