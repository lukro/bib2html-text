package server.controller;

import client.model.Client;
import global.controller.Console;
import global.logging.Log;
import global.logging.LogLevel;
import global.model.IResult;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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

    private Console consoleStream;

    @FXML
    ChoiceBox<LogLevel> logLevelChoiceBox;

    @FXML
    Label serverAdressLabel;

    @FXML
    TextArea serverConsoleTextArea;

    @FXML
    CheckBox useUtilisationCheckingBox;

    @FXML
    ListView<String> microServiceListView;

    @FXML
    ListView<ClientRequestDisplayItem> clientRequestListView;


    private class ClientRequestDisplayItem {

        private final String clientID;
        private final int expectedSize;
        private double completion;
        private ClientRequestDisplayItem(String clientID, int expectedSize) {
            this.clientID = clientID;
            this.expectedSize = expectedSize;
            completion = 0.0;
        }

        public void setCompletion(double completion) {
            this.completion = completion;
        }

        @Override
        public String toString() {
            return clientID + ", Expected Size : " + expectedSize + " (" + completion + ")";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ClientRequestDisplayItem that = (ClientRequestDisplayItem) o;
            return clientID.equals(that.clientID);
        }

        public String getClientID() {
            return clientID;
        }

    }
    public ServerController() {
        Thread serverStartThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final String originalThreadName = Thread.currentThread().getName();
                    Thread.currentThread().setName(originalThreadName + " - ServerStartThread");
                    new Server();
                } catch (IOException | TimeoutException e) {
                    Log.log("Failed to initialize Server", e);
                    System.exit(-1);
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
            serverAdressLabel.setText("ERROR");
        }

        EventManager.getInstance().registerListener(this);
        Log.log("Initialized the Server.", LogLevel.INFO);
    }

    @Override
    public void notify(IEvent toNotify) {
        if (toNotify instanceof MicroServiceConnectedEvent) {
            String toAddID = ((MicroServiceConnectedEvent) toNotify).getConnectedSvcID();
            Log.log("Registered Microservice with ID " + toAddID, LogLevel.WARNING);
            Platform.runLater(() -> microServiceListView.getItems().add(toAddID));
        } else if (toNotify instanceof MicroServiceDisconnectedEvent) {
            String disconnectedServiceID = ((MicroServiceDisconnectedEvent) toNotify).getDisconnectedSvcID();
            Log.log("Unregistered Microservice with ID " + disconnectedServiceID, LogLevel.WARNING);
            Platform.runLater(() -> microServiceListView.getItems().remove(disconnectedServiceID));
        } else if (toNotify instanceof RequestStoppedEvent) {
            String toRemoveClientID = ((RequestStoppedEvent) toNotify).getStoppedRequestClientID();
            Log.log("Removed Request with ID " + toRemoveClientID, LogLevel.WARNING);
            Platform.runLater(() -> clientRequestListView.getItems().remove(toRemoveClientID));
        } else if (toNotify instanceof RequestAcceptedEvent) {
            int newRequestSize = ((RequestAcceptedEvent) toNotify).getReqSize();
            String newClientID = ((RequestAcceptedEvent) toNotify).getRequestID();
            ClientRequestDisplayItem newDisplayItem = new ClientRequestDisplayItem(newClientID, newRequestSize);
            Platform.runLater(() -> clientRequestListView.getItems().add(newDisplayItem));
        } else if (toNotify instanceof FinishedCollectingResultEvent) {
            String removeClientID = ((FinishedCollectingResultEvent) toNotify).getResult().getClientID();
            ClientRequestDisplayItem toRemoveItem = new ClientRequestDisplayItem(removeClientID, 0);
            Platform.runLater(() -> clientRequestListView.getItems().remove(toRemoveItem));
        } else if (toNotify instanceof ProgressUpdateEvent) {
            String toUpdateClientID = ((ProgressUpdateEvent) toNotify).getClientID();
            double newCompletion = ((ProgressUpdateEvent) toNotify).getProgress();
            for (ClientRequestDisplayItem item : clientRequestListView.getItems()) {
                if (item.getClientID().equals(toUpdateClientID)) {
                    item.setCompletion(newCompletion);
                    break;
                }
            }
        }
    }

    @Override
    public Set<Class<? extends IEvent>> getEvents() {
        return new HashSet(Arrays.asList(ClientRegisteredEvent.class, ClientDisconnectedEvent.class,
                MicroServiceConnectedEvent.class, MicroServiceDisconnectedEvent.class,
                RequestStoppedEvent.class, RequestAcceptedEvent.class,
                FinishedCollectingResultEvent.class, ProgressUpdateEvent.class));
    }

    public void removeMicroServiceButtonPressed() {
        if (microServiceListView.getSelectionModel().getSelectedItem() != null) {
            String serviceIDToRemove = microServiceListView.getSelectionModel().getSelectedItem();
            EventManager.getInstance().publishEvent(new MicroServiceDisconnectionRequestEvent(serviceIDToRemove));
        }
    }

    public void cancelRequestButtonPressed() {
        if (clientRequestListView.getSelectionModel().getSelectedItem() != null) {
            String toStopClientID = clientRequestListView.getSelectionModel().getSelectedItem().getClientID();
            EventManager.getInstance().publishEvent(new RequestStoppedEvent(toStopClientID));
        }
    }

    public void clearLogButtonPressed() {
        consoleStream.clearTextArea();
    }

    public void addMicroServiceButtonPressed(ActionEvent event) {
        EventManager.getInstance().publishEvent(new StartMicroServiceEvent());
    }

    public void switchUseUtilisationCheckingBoxClicked(ActionEvent actionEvent) {
        EventManager.getInstance().publishEvent(new SwitchUtilisationCheckingEvent(useUtilisationCheckingBox.isSelected()));
    }
}
