package server.modules;

import com.rabbitmq.client.*;
import global.controller.IConnectionPoint;
import global.logging.Log;
import global.logging.LogLevel;
import global.model.*;
import org.apache.commons.lang3.SerializationUtils;
import com.rabbitmq.client.AMQP.BasicProperties;
import server.events.*;
import server.events.EventListener;
import sun.security.x509.IPAddressName;

import java.io.IOException;
import java.util.*;
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
    private final BasicProperties replyProps;

    private HashMap<String, CallbackInformation> clientIDtoCallbackInformation = new HashMap<>();

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
        EventManager.getInstance().registerListener(this);
        this.replyProps = new BasicProperties
                .Builder()
                .correlationId(serverID)
                .replyTo(callbackQueueName)
                .build();
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
            Log.log("failed to consume incoming queues in server.run()", e);
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
        } else if (toNotify instanceof StopRequestEvent) {
            IClientRequest toStop = ((StopRequestEvent) toNotify).getRequest();
            stopRequest(toStop);
        } else if (toNotify instanceof ClientBlockRequestEvent) {
            String toBlock = ((ClientBlockRequestEvent) toNotify).getClientID();
            blacklistClient(toBlock);
        } else if (toNotify instanceof MicroserviceDisconnectionRequestEvent) {
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
        Object deliveredObject = SerializationUtils.deserialize(bytes);
        if (deliveredObject instanceof IClientRequest) {
            handleDeliveredClientRequest((IClientRequest) deliveredObject, basicProperties);
        } else if (deliveredObject instanceof IPartialResult) {
            ReceivedPartialResultEvent event = new ReceivedPartialResultEvent((IPartialResult) deliveredObject);
            EventManager.getInstance().publishEvent(event);
        }
    }

    private void handleDeliveredClientRequest(IClientRequest deliveredClientRequest, BasicProperties basicProperties) throws IOException {
        String requestID = deliveredClientRequest.getClientID();
        clientIDtoCallbackInformation.put(requestID,
                new CallbackInformation(basicProperties, new BasicProperties
                        .Builder()
                        .correlationId(basicProperties.getCorrelationId())
                        .build()));
        if (isBlacklisted(requestID)) {
            Log.log("Illegal ClientRequest with ID '" + requestID + "' refused.");
            channel.basicPublish("", basicProperties.getReplyTo(), clientIDtoCallbackInformation.get(requestID).replyProperties, SerializationUtils.serialize("Unfortunately you have been banned."));
            clientIDtoCallbackInformation.remove(requestID);
        } else {
            IEntry firstEntry = deliveredClientRequest.getEntries().stream().findFirst().get();
            if (firstEntry != null) {
                processDeliveredClientRequest(deliveredClientRequest, firstEntry);
            } else {
                //TODO Handle 0 Entries request
            }
        }
    }

    public class CallbackInformation {

        BasicProperties basicProperties;
        BasicProperties replyProperties;

        public CallbackInformation(BasicProperties basicProperties, BasicProperties replyProperties) {
            this.basicProperties = basicProperties;
            this.replyProperties = replyProperties;
        }
    }

    private void processDeliveredClientRequest(IClientRequest deliveredClientRequest, IEntry firstEntry) throws IOException {
        int countOfEntries = deliveredClientRequest.getEntries().size();
        int countOfCSL = firstEntry.getCslFiles().size();
        int countOfTempl = firstEntry.getTemplates().size();

        if (countOfCSL == 0)
            countOfCSL = 1;
        else if (countOfTempl == 0)
            countOfTempl = 1;

        int requestSize = countOfEntries * countOfCSL * countOfTempl;

        RequestAcceptedEvent requestAcceptedEvent = new RequestAcceptedEvent(deliveredClientRequest.getClientID(), requestSize);
        EventManager.getInstance().publishEvent(requestAcceptedEvent);

        Log.log("Server successfully received a ClientRequest.");

        for (IEntry currentEntry : deliveredClientRequest.getEntries())
            channel.basicPublish("", TASK_QUEUE_NAME, replyProps, SerializationUtils.serialize(currentEntry));
    }

    private Collection<String> blacklistedClients = new HashSet<>();

    /**
     * Tells us whether a certain client id was blacklisted.
     * Can later be expanded by persistent blacklist file.
     * TODO Make persistent
     *
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
     *
     * @param clientIDToBlock The id to block.
     */
    public void blacklistClient(String clientIDToBlock) {
        blacklistedClients.add(clientIDToBlock);
        Log.log("Blacklisted Client " + clientIDToBlock, LogLevel.WARNING);
    }

    /**
     * Stops a running Request.
     *
     * @param request
     */
    private void stopRequest(IClientRequest request) {
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
