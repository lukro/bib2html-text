package client.controller;

import com.rabbitmq.client.*;
import client.model.ClientFileModel;
import global.controller.IConnectionPoint;
import global.model.DefaultClientRequest;
import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.sql.Time;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

/**
 * Created by daan on 11/30/16.
 */
public class Client implements Runnable, Consumer, IConnectionPoint {

    private final String clientID;
    private final BibTeXEntryFormatter bibTeXEntryFormatter = BibTeXEntryFormatter.getINSTANCE();
    private ClientFileModel clientFileModel;
    private final Connection connection;
    private final Channel channel;
    private final String hostIP;
    private final String callbackQueueName;
    private String outputDirectory;
    private final String CLIENT_REQUEST_NAME = "clientRequestQueue";

    public Client() throws IOException, TimeoutException {
        this("localhost");
    }

    public Client(String hostIP) throws IOException, TimeoutException {
        this(hostIP, UUID.randomUUID().toString());
    }

    public Client(String hostIP, String callbackQueueName) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(hostIP);
        this.hostIP = hostIP;
        this.clientID = callbackQueueName;
        this.callbackQueueName = callbackQueueName;
        this.connection = factory.newConnection();
        this.channel = connection.createChannel();
        this.clientFileModel = new ClientFileModel(clientID);
    }

    @Override
    public void run() {
        try {
            channel.basicConsume(callbackQueueName, false, this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendClientRequest() throws IOException {
        System.out.println("request sent");
        channel.basicPublish("", CLIENT_REQUEST_NAME, null, SerializationUtils.serialize(this.createClientRequest()));
    }

    public void sendMessage(String message, String routingKey) throws IOException {
        channel.basicPublish("", routingKey, null, message.getBytes());
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
        System.out.println("Client: message received");
    }

    public String getClientID() {
        return clientID;
    }

    public ClientFileModel getClientFileModel() {
        return clientFileModel;
    }

    public BibTeXEntryFormatter getBibTeXEntryFormatter() {
        return bibTeXEntryFormatter;
    }

    public DefaultClientRequest createClientRequest() throws IOException {
        return new DefaultClientRequest(this.clientID, bibTeXEntryFormatter.createBibTeXEntryObjectListFromClientFileModel(this.clientFileModel));
    }

    @Override
    public void closeConnection() {

    }

    @Override
    public void initConnectionPoint() {

    }

    @Override
    public String getHostIP() {
        return null;
    }

    @Override
    public String getID() {
        return null;
    }
}
