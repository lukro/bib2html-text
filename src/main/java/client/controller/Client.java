package client.controller;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import client.model.ClientFileModel;
import global.model.DefaultClientRequest;
import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

/**
 * Created by daan on 11/30/16.
 */
public class Client extends ConnectionPoint implements Runnable, Consumer {

    private final String clientID = UUID.randomUUID().toString();
    private final BibTeXEntryFormatter bibTeXEntryFormatter = BibTeXEntryFormatter.getInstance();
    private ClientFileModel clientFileModel = new ClientFileModel(clientID);

    public Client() throws IOException, TimeoutException {
        super();
    }

    public Client(String hostIP) throws IOException, TimeoutException {
        super(hostIP);
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
        channel.basicPublish("", QUEUE_TO_SERVER_NAME, null, SerializationUtils.serialize(this.createClientRequest()));
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
        System.out.println(this.routingKey + ": message received");
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
}
