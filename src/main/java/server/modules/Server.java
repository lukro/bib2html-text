package server.modules;

import com.rabbitmq.client.*;
import global.controller.IConnectionPoint;
import global.logging.Log;
import global.logging.LogLevel;
import global.model.DefaultEntry;
import global.model.DefaultResult;
import global.model.IClientRequest;
import org.apache.commons.lang3.SerializationUtils;
import server.events.*;

import java.io.IOException;
import java.util.Collection;
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
        MicroServiceManager.initialize(channel, TASK_QUEUE_NAME);
        this.partialResultCollector = PartialResultCollector.getInstance();
        EventManager.getInstance().registerListener(this);
        initConnectionPoint();
    }

    public boolean sendEntryToMicroServices(DefaultEntry entry) {
        try {
            //TODO : Implement properly after checking out how the rabbitmq publish works
            MicroServiceManager.getInstance().checkUtilization();
            channel.basicPublish("", TASK_QUEUE_NAME, null, SerializationUtils.serialize(entry));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void run() {
        try {
            consumeIncomingQueues();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void consumeIncomingQueues() throws IOException {
        channel.basicConsume(CLIENT_REQUEST_QUEUE_NAME, true, this);
        channel.basicConsume(callbackQueueName, true, this);
    }

    @Override
    public void notify(Event toNotify) {
        if (toNotify instanceof FinishedCollectingResultEvent) {
            DefaultResult eventResult = ((FinishedCollectingResultEvent) toNotify).getResult();
            String clientID = eventResult.getClientID();
            //TODO : Publish to client
        }
        else if(toNotify instanceof StopRequestEvent) {
            IClientRequest toStop = ((StopRequestEvent) toNotify).getRequest();
            stopRequest(toStop);
        }
        else if(toNotify instanceof ClientBlockRequestEvent){
            String toBlock = ((ClientBlockRequestEvent) toNotify).getClientID();
            blacklistClient(toBlock);
        }
        else if(toNotify instanceof MicroserviceDisconnectionRequestEvent){
            String toDisconnect = ((MicroserviceDisconnectionRequestEvent) toNotify).getToDisconnectID();
            MicroServiceManager.getInstance().stopMicroService(toDisconnect);
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
        IClientRequest deliveredClientRequest = SerializationUtils.deserialize(bytes);

        String clientID = deliveredClientRequest.getClientID();
        if (isBlacklisted(clientID)) {
            Log.log("Illegal Client Request Refused. ID was " + clientID);
        } else {
            int requestSize = deliveredClientRequest.getEntries().size();
            EventManager.getInstance().publishEvent(new RequestAcceptedEvent(clientID,requestSize));


            Log.log("Server received a message");

            //TODO: request verarbeiten, entries verschicken über sendEntryToMicroServices.

        }
    }

    private Collection<String> blacklistedClients = new HashSet<>();

    /**
     * Tells us whether a certain client id was blacklisted.
     * Can later be expanded by persistent blacklist file.
     * TODO Make persistent
     * @param clientID The id for which we want to know the blacklisting state
     * @return A boolean.
     */
    private boolean isBlacklisted(String clientID) {
        return blacklistedClients.contains(clientID);
    }

    /**
     * Blacklists the client id.
     * Can be expanded by persistent blacklist file.
     * TODO Make persistent
     * @param clientToBlock The id to block.
     */
    public void blacklistClient(String clientToBlock) {
        blacklistedClients.add(clientToBlock);
        //TODO : Disconnect Client
        Log.log("Blacklisted Client " +clientToBlock, LogLevel.WARNING);
    }

    /**
     * Stops a running Request.
     *
     * @param request
     */
    private void stopRequest(IClientRequest request){
        //TODO : Fill...
        EventManager.getInstance().publishEvent(new RequestStoppedEvent(request));
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
        channel.queueDeclare(TASK_QUEUE_NAME, false, false, false, null);
        //incoming queues
        channel.queueDeclare(CLIENT_REQUEST_QUEUE_NAME, false, false, false, null);
        channel.queueDeclare(callbackQueueName, false, false, false, null);
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
}
