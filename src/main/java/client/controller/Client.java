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
public class Client implements Runnable, Consumer, IConnectionPoint {

    private final String clientID, hostIP, callbackQueueName;
    private final Connection connection;
    private final Channel channel;
    private final String CLIENT_REQUEST_QUEUE_NAME = "clientRequestQueue";
    private String outputDirectory;

    private final BibTeXEntryFormatter bibTeXEntryFormatter = BibTeXEntryFormatter.getINSTANCE();
    private ClientFileModel clientFileModel;


    public Client() throws IOException, TimeoutException {
        this("localhost");
    }

    public Client(String hostIP) throws IOException, TimeoutException {
        this(hostIP, UUID.randomUUID().toString());
    }

    public Client(String hostIP, String clientID) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(hostIP);
        this.hostIP = hostIP;
        this.clientID = clientID;
        this.callbackQueueName = clientID;
        this.connection = factory.newConnection();
        this.channel = connection.createChannel();
        this.clientFileModel = new ClientFileModel(this.clientID);
        initConnectionPoint();
    }

    @Override
    public void run() {
        try {
            channel.basicConsume(callbackQueueName, false, this);
        } catch (IOException e) {
            Log.log("Failure to run channel.basicConsume() in Client.run",e);
        }
    }

    public void sendClientRequest() throws IOException {
        channel.basicPublish("", CLIENT_REQUEST_QUEUE_NAME, null, SerializationUtils.serialize(this.createClientRequest()));
        System.out.println("Client with ID: " + this.clientID + " sent a ClientRequest.");
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
        System.out.println("Client with ID: " + this.clientID + " received a message on queue: " + this.callbackQueueName);
    }

    @Override
    public void closeConnection() throws IOException, TimeoutException {
        channel.close();
        connection.close();
    }

    @Override
    public void initConnectionPoint() throws IOException {
        channel.queueDeclare(CLIENT_REQUEST_QUEUE_NAME, false, false, false, null);
        this.run();
    }

    @Override
    public String getHostIP() {
        return hostIP;
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
