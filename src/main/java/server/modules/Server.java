package server.modules;

import com.rabbitmq.client.*;
import com.rabbitmq.client.AMQP.BasicProperties;
import global.controller.IConnectionPoint;
import global.identifiers.QueueNames;
import global.logging.Log;
import global.logging.LogLevel;
import global.model.*;
import org.apache.commons.lang3.SerializationUtils;
import server.events.*;
import server.events.IEventListener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeoutException;

/**
 * @author Maximilian Schirm, daan
 *         created 05.12.2016
 */

public class Server implements IConnectionPoint, Runnable, Consumer, IEventListener {

    private final static String CLIENT_REQUEST_QUEUE_NAME = QueueNames.CLIENT_REQUEST_QUEUE_NAME.toString();
    private final static String TASK_QUEUE_NAME = QueueNames.TASK_QUEUE_NAME.toString();
    private final String REGISTRATION_QUEUE_NAME = QueueNames.MICROSERVICE_REGISTRATION_QUEUE_NAME.toString();
    private final String STOP_QUEUE_NAME = QueueNames.MICROSERVICE_STOP_QUEUE_NAME.toString();
    private static final int PER_CONSUMER_LIMIT = MicroServiceManager.MAXIMUM_UTILIZATION;

    private final String serverID, hostIP, callbackQueueName;
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
        //Create connection and channel with new ConnectionFactory
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(hostIP);
        this.connection = factory.newConnection();
        this.channel = connection.createChannel();
        channel.basicQos(PER_CONSUMER_LIMIT);

        //Assign final fields
        this.hostIP = hostIP;
        this.serverID = serverID;
        this.callbackQueueName = serverID;
        this.replyProps = new BasicProperties
                .Builder()
                .correlationId(serverID)
                .replyTo(callbackQueueName)
                .build();

        //Initialize modules
        MicroServiceManager.initialize(channel, TASK_QUEUE_NAME);
        PartialResultCollector.getInstance();
        EventManager.getInstance().registerListener(this);
        initConnectionPoint();

        //create blacklistfile, if it does not exist
        if (!Files.exists(Paths.get("blacklist.txt"))) {
            Files.createFile(Paths.get("blacklist.txt"));
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
        channel.basicConsume(REGISTRATION_QUEUE_NAME, true, this);
    }

    /**
     * The action to take when a RequestStoppedEvent is registered.
     * <p>
     * (Extracted as it's own method for readability improvement)
     *
     * @param toNotify The event that was registered.
     */
    private void handleRequestStoppedEvent(RequestStoppedEvent toNotify) {
        String toStopClientID = toNotify.getStoppedRequestClientID();
        CallbackInformation clientCBI = clientIDtoCallbackInformation.get(toStopClientID);
        try {
            channel.basicPublish("", clientCBI.basicProperties.getReplyTo(), clientCBI.replyProperties, SerializationUtils.serialize("Server Admin forcefully stopped your Request."));
        } catch (IOException e) {
            Log.log("COULD NOT RETURN RESULT TO CLIENT", LogLevel.SEVERE);
            Log.log("", e);
        }
    }

    /**
     * The action to take when a FinishedCollectingResultEvent is registered.
     * <p>
     * (Extracted as it's own method for readability improvement)
     *
     * @param toNotify The event that was registered.
     */
    private void handleFinishedCollectingResultEvent(FinishedCollectingResultEvent toNotify) {
        String clientID = toNotify.getResult().getClientID();
        CallbackInformation clientCBI = clientIDtoCallbackInformation.get(clientID);
        try {
            channel.basicPublish("", clientCBI.basicProperties.getReplyTo(), clientCBI.replyProperties, SerializationUtils.serialize(toNotify.getResult()));
        } catch (IOException e) {
            Log.log("COULD NOT RETURN RESULT TO CLIENT", LogLevel.SEVERE);
            Log.log("", e);
        }
    }


    @Override
    public void handleDelivery(String s, Envelope envelope, AMQP.BasicProperties basicProperties, byte[] bytes) throws IOException {
        Object deliveredObject = SerializationUtils.deserialize(bytes);
        if (deliveredObject instanceof IClientRequest) {
            handleDeliveredClientRequest((IClientRequest) deliveredObject, basicProperties);
        } else if (deliveredObject instanceof IPartialResult) {
            ReceivedPartialResultEvent event = new ReceivedPartialResultEvent((IPartialResult) deliveredObject);
            EventManager.getInstance().publishEvent(event);
        } else if (deliveredObject instanceof IRegistrationRequest) {
            ReceivedRegistrationRequestEvent event = new ReceivedRegistrationRequestEvent((IRegistrationRequest) deliveredObject);
//            EventManager.getInstance().publishEvent(event);
            Log.log("Received Registration Request!", LogLevel.SEVERE);
            handleReceivedRegistrationRequest((IRegistrationRequest)deliveredObject, basicProperties);
        } else if (deliveredObject instanceof IStopOrderAck){
            String idToRemove = deliveredObject.getStopMicroServiceID();
            EventManager.getInstance().publishEvent(new MicroServiceDisconnectedEvent(idToRemove));
        }
    }

    private void handleReceivedRegistrationRequest(IRegistrationRequest deliveredObject, BasicProperties basicProperties) {
        BasicProperties replyProps = new BasicProperties
                .Builder()
                .correlationId(basicProperties.getCorrelationId())
                .build();
        CallbackInformation callbackInformation = new CallbackInformation(basicProperties, replyProps);
        IRegistrationAck ack = new DefaultRegistrationAck(TASK_QUEUE_NAME);
        try {
            Log.log("Sending acknowledge connection request to a MicroService", LogLevel.LOW);
            channel.basicPublish("", basicProperties.getReplyTo(), replyProps, SerializationUtils.serialize(ack));
            EventManager.getInstance().publishEvent(new MicroServiceConnectedEvent(deliveredObject.getID(),deliveredObject.getIP()));
        } catch (IOException e) {
            Log.log("Failed to send acknowledgement to microservice", e);
        }

    }

    /**
     * Conducts all necessary operations for handling a received ClientRequest.
     * (Checks for blacklisting of Client and does other preprocessing checks, then sends the IClientRequest to processing).
     *
     * @param deliveredClientRequest The received request.
     * @param basicProperties        The basic properties of the received package. Used for replying to bad requests.
     * @throws IOException Thrown in case an issue with the callback queue occurs.
     */
    private void handleDeliveredClientRequest(IClientRequest deliveredClientRequest, BasicProperties basicProperties) throws IOException {
        //Generate Callback info
        String requestID = deliveredClientRequest.getClientID();
        BasicProperties replyProps = new BasicProperties
                .Builder()
                .correlationId(basicProperties.getCorrelationId())
                .build();
        CallbackInformation callbackInformation = new CallbackInformation(basicProperties, replyProps);
        clientIDtoCallbackInformation.put(requestID, callbackInformation);

        //Check for blacklisting and handle accordingly
        if (isBlacklisted(requestID)) {
            Log.log("Illegal ClientRequest with ID '" + requestID + "' refused.");
            channel.basicPublish("", basicProperties.getReplyTo(), clientIDtoCallbackInformation.get(requestID).replyProperties, SerializationUtils.serialize("Unfortunately you have been banned."));
            clientIDtoCallbackInformation.remove(requestID);
        } else {
            if (deliveredClientRequest.getEntries().isEmpty()) {
                Log.log("received request with 0 entries.", LogLevel.INFO);
                channel.basicPublish("", basicProperties.getReplyTo(), clientIDtoCallbackInformation.get(requestID).replyProperties, SerializationUtils.serialize("Server received empty request. Conversion aborted."));
                clientIDtoCallbackInformation.remove(requestID);
            } else
                processDeliveredClientRequest(deliveredClientRequest);
        }
    }

    /**
     * Class for clean storing of a tuple of BasicProperties.
     */
    private class CallbackInformation {
        private BasicProperties basicProperties;
        private BasicProperties replyProperties;
        private CallbackInformation(BasicProperties basicProperties, BasicProperties replyProperties) {
            this.basicProperties = basicProperties;
            this.replyProperties = replyProperties;
        }

    }
    /**
     * Processes the received Request.
     *
     * @param deliveredClientRequest A IClientRequest with at least one Entry
     * @throws IOException Thrown in case the publishing to the channel fails.
     */
    private void processDeliveredClientRequest(IClientRequest deliveredClientRequest) throws IOException {
        IEntry firstEntry = deliveredClientRequest.getEntries().get(0);
        int countOfEntries = deliveredClientRequest.getEntries().size();

//        int countOfCSL = firstEntry.getCslFiles().size();
//        int countOfTempl = firstEntry.getTemplates().size();
//
//        if (countOfCSL == 0)
//            countOfCSL = 1;
//        if (countOfTempl == 0)
//            countOfTempl = 1;

//        int requestSize = countOfEntries * countOfCSL * countOfTempl;

        int countOfPartialPerEntry = firstEntry.getAmountOfExpectedPartials();

        int requestSize = countOfEntries * countOfPartialPerEntry;

        RequestAcceptedEvent requestAcceptedEvent = new RequestAcceptedEvent(deliveredClientRequest.getClientID(), requestSize);
        EventManager.getInstance().publishEvent(requestAcceptedEvent);

        Log.log("Server successfully received a ClientRequest.");

        for (IEntry currentEntry : deliveredClientRequest.getEntries()) {
            channel.basicPublish("", TASK_QUEUE_NAME, replyProps, SerializationUtils.serialize(currentEntry));
        }
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
//        List<String> lines = null;
//        try {
//            lines = Files.readAllLines(Paths.get("blacklist.txt"));
//            for(String line: lines) {
//                if(clientID.equals(line))
//                    return true;
//            }
//        } catch (IOException e) {
//            Log.log("Could not read blacklist file", e);
//        }
//        return false;
    }

    /**
     * Blacklists the client id.
     * Can be expanded by persistent blacklist file.
     * TODO Make persistent
     *
     * @param clientIDToBlock The id to block.
     */
    private void blacklistClient(String clientIDToBlock) {

        blacklistedClients.add(clientIDToBlock);
        Log.log("Blacklisted Client " + clientIDToBlock, LogLevel.WARNING);

//        try {
//            Files.write(Paths.get("blacklist.txt"), (clientIDToBlock + "\n").getBytes());
//        } catch (IOException e) {
//            Log.log("Failed to write to blacklist file", e);
//        }
//        blacklistedClients.add(clientIDToBlock);
//        Log.log("Blacklisted Client " + clientIDToBlock, LogLevel.WARNING);
    }

    @Override
    public void initConnectionPoint() throws IOException {
        declareQueues();
        run();
    }

    @Override
    public void closeConnection() throws IOException, TimeoutException {
        channel.close();
        connection.close();
    }

    @Override
    public void declareQueues() throws IOException {
        //outgoing queues
        channel.queueDeclare(TASK_QUEUE_NAME, false, false, false, null);
        channel.exchangeDeclare(STOP_QUEUE_NAME, "fanout");
        //incoming queues
        channel.queueDeclare(CLIENT_REQUEST_QUEUE_NAME, false, false, false, null);
        channel.queueDeclare(callbackQueueName, false, false, false, null);
        channel.queueDeclare(REGISTRATION_QUEUE_NAME, false, false, false, null);
    }

    //Empty (i.e. not yet used) methods from interface.


    @Override
    public String getHostIP() {
        return hostIP;
    }

    @Override
    public String getID() {
        return serverID;
    }

    /**
     * clears client request queue and task queue
     */
    private void clearQueues() {
        try {
            channel.queuePurge(TASK_QUEUE_NAME);
            channel.queuePurge(CLIENT_REQUEST_QUEUE_NAME);
            Log.log("cleared client_request_queue and task_queue");
        } catch (IOException e) {
            Log.log("failed to clear queues", LogLevel.ERROR);
        }
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

    //TODO : Replace with safer approach?
    public PartialResultCollector getPartialResultCollector() {
        return PartialResultCollector.getInstance();
    }

    //TODO : Replace with safer approach?
    public MicroServiceManager getMicroServiceManager() {
        return MicroServiceManager.getInstance();
    }

    private void sendStopOrderToMicroService(String idToRemove) {
        Log.log("Disconnecting MicroService " + idToRemove + "...");

        try {
            IStopOrder stopMe = new DefaultStopOrder(idToRemove);
            channel.basicPublish(STOP_QUEUE_NAME, "", null, SerializationUtils.serialize(stopMe));
            Log.log("Successfully sent stop order to service " + idToRemove, LogLevel.LOW);
        } catch (IOException e) {
            Log.log("Failed to send cancel request to service " + idToRemove, e);
        }
    }

    @Override
    public void notify(IEvent toNotify) {
        if (toNotify instanceof FinishedCollectingResultEvent) {
            handleFinishedCollectingResultEvent((FinishedCollectingResultEvent) toNotify);
        } else if (toNotify instanceof RequestStoppedEvent) {
            handleRequestStoppedEvent((RequestStoppedEvent) toNotify);
        } else if (toNotify instanceof ClientBlockRequestEvent) {
            String toBlock = ((ClientBlockRequestEvent) toNotify).getClientID();
            blacklistClient(toBlock);
        } else if (toNotify instanceof  MicroserviceDisconnectionRequestEvent){
            String idToRemove = ((MicroserviceDisconnectionRequestEvent) toNotify).getToDisconnectID();
            sendStopOrderToMicroService(idToRemove);
        }
    }

    @Override
    public Set<Class<? extends IEvent>> getEvents() {
        Set<Class<? extends IEvent>> evts = new HashSet<>();
        evts.addAll(Arrays.asList(FinishedCollectingResultEvent.class, RequestStoppedEvent.class, ClientBlockRequestEvent.class, MicroServiceConnectedEvent.class, MicroserviceDisconnectionRequestEvent.class));
        return evts;
    }
}
