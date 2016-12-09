package server.modules;

import com.rabbitmq.client.*;
import global.controller.IConnectionPoint;
import global.model.DefaultClientRequest;
import global.model.DefaultEntry;
import global.model.DefaultResult;
import org.apache.commons.lang3.SerializationUtils;
import server.events.Event;
import server.events.EventListener;
import server.events.EventManager;
import server.events.FinishedCollectingResultEvent;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

/**
 * @author Maximilian Schirm, daan
 *         created 05.12.2016
 */

public class Server implements IConnectionPoint, Runnable, Consumer, EventListener {

    private final String serverID, hostIP, callbackQueueName;
    private final String CLIENT_REQUEST_QUEUE_NAME = "clientRequestQueue";
    private final String TASK_QUEUE_NAME = "taskQueue";
//    private final URI address;

    private final Connection connection;
    private final Channel channel;

    private ArrayList<String> invalidClientIDs = new ArrayList<>();

    private final MicroServiceManager microServiceManager;
    private final PartialResultCollector partialResultCollector;

    public Server() throws IOException, TimeoutException {
        this("localhost");
    }

    public Server(String hostIP) throws IOException, TimeoutException {
        this(hostIP, UUID.randomUUID().toString());
    }

    public Server(String hostIP, String serverID) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(hostIP);
        this.hostIP = hostIP;
//        this.address = URI.create(getHostIP());
        this.serverID = serverID;
        this.callbackQueueName = serverID;
        this.connection = factory.newConnection();
        this.channel = connection.createChannel();
        this.microServiceManager = MicroServiceManager.getInstance();
        this.partialResultCollector = PartialResultCollector.getInstance();
        EventManager.getInstance().registerListener(this);
        initConnectionPoint();
    }

    public boolean sendEntryToMicroServices(DefaultEntry entry) {
        try {
            //TODO : Implement properly after checking out how the rabbitmq publish works
            String microServiceKey = MicroServiceManager.getInstance().getFreeMicroServiceKey();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void run() {
        try {
            declareAndConsumeIncomingQueues();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void notify(Event toNotify) {
        if (toNotify instanceof FinishedCollectingResultEvent) {
            DefaultResult eventResult = ((FinishedCollectingResultEvent) toNotify).getResult();
            String clientID = eventResult.getClientID();
            //TODO : Publish to client
        }
    }

    @Override
    public Set<Class<? extends Event>> getEvents() {
        Set<Class<? extends Event>> evts = new HashSet<>();
        evts.add(FinishedCollectingResultEvent.class);
        return evts;
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
        DefaultClientRequest deliveredClientRequest = (DefaultClientRequest) SerializationUtils.deserialize(bytes);
        for (DefaultEntry currentEntry : deliveredClientRequest.getEntries()) {
            //TODO: publish entries so microservices
        }
        System.out.println("Server received message");
        channel.basicPublish("", TASK_QUEUE_NAME, null, bytes);
    }

    @Override
    public void closeConnection() throws IOException, TimeoutException {
        channel.close();
        connection.close();
    }

    @Override
    public void initConnectionPoint() throws IOException {
        declareOutgoingQueue();
        run();
    }

    @Override
    public void declareAndConsumeIncomingQueues() throws IOException {
        //declare & consume queue from clients to server
        channel.queueDeclare(CLIENT_REQUEST_QUEUE_NAME, false, false, false, null);
        channel.basicConsume(CLIENT_REQUEST_QUEUE_NAME, true, this);
        //declare & consume queue from microservices to server
        channel.queueDeclare(callbackQueueName, false, false, false, null);
        channel.basicConsume(callbackQueueName, true, this);
    }

    @Override
    public String getHostIP() {
        return hostIP;
    }

    @Override
    public String getID() {
        return serverID;
    }

    private void declareOutgoingQueue() throws IOException {
        channel.queueDeclare(TASK_QUEUE_NAME, false, false, false, null);
    }

    public void banClientID(String clientID) {
        if (!invalidClientIDs.contains(clientID))
            invalidClientIDs.add(clientID);
    }

    public void unbanClientID(String clientID) {
        if (invalidClientIDs.contains(clientID))
            invalidClientIDs.remove(clientID);
    }


}
