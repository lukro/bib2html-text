package client.controller;

import com.rabbitmq.client.*;
import client.model.ClientFileModel;
import global.controller.IConnectionPoint;
import global.logging.Log;
import global.model.DefaultClientRequest;
import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

/**
 * Created by daan on 11/30/16.
 */
public class Client implements IConnectionPoint, Runnable, Consumer {

    private final String clientID, callbackQueueName;
    private final String CLIENT_REQUEST_QUEUE_NAME = "clientRequestQueue";
    private String outputDirectory, hostIP;

    private Connection connection;
    private Channel channel;

    private final BibTeXEntryFormatter bibTeXEntryFormatter = BibTeXEntryFormatter.getINSTANCE();
    private ClientFileModel clientFileModel;


    public Client() throws IOException {
        this("localhost");
    }

    public Client(String hostIP) throws IOException {
        this(hostIP, UUID.randomUUID().toString());
    }

    public Client(String hostIP, String clientID) throws IOException {
        connectToHost(hostIP);
        this.clientID = clientID;
        this.callbackQueueName = clientID;
        this.clientFileModel = new ClientFileModel(this.clientID);
        initConnectionPoint();
    }

    @Override
    public void run() {
        try {
            consumeIncomingQueues();
        } catch (IOException e) {
            Log.log("Failure to run channel.basicConsume() in Client.run", e);
        }
    }

    @Override
    public void consumeIncomingQueues() throws IOException {
        channel.basicConsume(callbackQueueName, true, this);
    }

    public void sendClientRequest() throws IOException {
        channel.basicPublish("", CLIENT_REQUEST_QUEUE_NAME, null, SerializationUtils.serialize(this.createClientRequest()));
        Log.log("Client with ID: " + this.clientID + " sent a ClientRequest.");
    }

    @Override
    public void handleConsumeOk(String s) {

    }

    @Override
    public void handleCancelOk(String s) {

    }

    @Override
    public void handleCancel(String s) throws IOException {

    }

    @Override
    public void handleShutdownSignal(String s, ShutdownSignalException e) {

    }

    @Override
    public void handleRecoverOk(String s) {

    }

    @Override
    public void handleDelivery(String s, Envelope envelope, AMQP.BasicProperties basicProperties, byte[] bytes) throws IOException {
        Log.log("Client with ID: " + this.clientID + " received a message on queue: " + this.callbackQueueName);
    }

    @Override
    public void closeConnection() throws IOException, TimeoutException {
        channel.close();
        connection.close();
    }

    @Override
    public void initConnectionPoint() throws IOException {
        declareQueues();
        run();
    }

    @Override
    public void declareQueues() throws IOException {
        //outgoing queues
        channel.queueDeclare(CLIENT_REQUEST_QUEUE_NAME, false, false, false, null);
        //incoming queues
        channel.queueDeclare(callbackQueueName, false, false, false, null);
    }

    @Override
    public String getHostIP() {
        return hostIP;
    }

    public boolean connectToHost(String hostIP) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(hostIP);
        try {
            this.connection = factory.newConnection();
        } catch (Exception e) {
            Log.log("Invalid Host IP " + hostIP);
            return false;
        }
        try {
            this.channel = connection.createChannel();
        } catch (IOException e) {
            Log.log("Could not create a channel for the connection to host ip " + hostIP);
            return false;
        }
        this.hostIP = hostIP;
        return true;
    }

    @Override
    public String getID() {
        return clientID;
    }

    public String getCallbackQueueName() {
        return callbackQueueName;
    }

    public String getCLIENT_REQUEST_QUEUE_NAME() {
        return CLIENT_REQUEST_QUEUE_NAME;
    }

    public BibTeXEntryFormatter getBibTeXEntryFormatter() {
        return bibTeXEntryFormatter;
    }

    public ClientFileModel getClientFileModel() {
        return clientFileModel;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    private DefaultClientRequest createClientRequest() throws IOException {
        return new DefaultClientRequest(this.clientID, bibTeXEntryFormatter.createBibTeXEntryObjectListFromClientFileModel(this.clientFileModel));
    }
}
