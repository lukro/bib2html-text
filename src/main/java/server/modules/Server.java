package server.modules;

import com.rabbitmq.client.*;
import com.rabbitmq.client.AMQP.BasicProperties;
import global.controller.IConnectionPoint;
import global.identifiers.QueueNames;
import global.logging.Log;
import global.logging.LogLevel;
import global.model.*;
import global.util.ConnectionUtils;
import global.util.FileUtils;
import org.apache.commons.lang3.SerializationUtils;
import server.events.*;

import java.io.File;
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
    private final static String REGISTRATION_QUEUE_NAME = QueueNames.MICROSERVICE_REGISTRATION_QUEUE_NAME.toString();
    private final static String STOP_EXCHANGE_NAME = QueueNames.STOP_EXCHANGE_NAME.toString();
    private final static String CLIENT_CALLBACK_EXCHANGE_NAME = QueueNames.CLIENT_CALLBACK_EXCHANGE_NAME.toString();
    private final static int PER_CONSUMER_LIMIT = MicroServiceManager.MAXIMUM_UTILIZATION;
    private final static String DEFAULT_BLACKLIST_FILE_NAME = "blacklist.txt";
    private final static String VALID_CLIENT_SECRET_KEYS_FILE_NAME = "secretkeys.txt";

    private final String serverID, hostIP, callbackQueueName;
    private final Connection connection;
    private final Channel channel;
    private final BasicProperties replyProps;
    private HashMap<String, CallbackInformation> clientIDtoCallbackInformation = new HashMap<>();
    private Collection<String> blacklistedClients = new ArrayList<>();
    private Collection<String> validSecretKeys = new ArrayList<>();


    public Server() throws IOException, TimeoutException {
        this("localhost");
    }

    public Server(String hostIP) throws IOException, TimeoutException {
        this(hostIP, UUID.randomUUID().toString());
    }

    public Server(String hostIP, String serverID) throws IOException, TimeoutException {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    shutdownServer();
                } catch (IOException | TimeoutException e) {
                    Log.log("Failed to shutdown Server. ", e);
                }
            }
        });
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
        //TODO remove queued entries, tell PRC to disregard all associated orders
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
        Log.log("ClientID from Result: " + clientID, LogLevel.LOW);
        CallbackInformation clientCBI = clientIDtoCallbackInformation.get(clientID);
        Log.log("ClientID from CBI: " + clientCBI.basicProperties.getCorrelationId(), LogLevel.LOW);
        try {
            channel.basicPublish("", clientCBI.basicProperties.getReplyTo(), clientCBI.replyProperties, SerializationUtils.serialize(toNotify.getResult()));
//            channel.basicPublish(CLIENT_CALLBACK_EXCHANGE_NAME, clientID, null, SerializationUtils.serialize(toNotify.getResult()));
            Log.log("Finished result. Published to :" + clientID);
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
            Log.log("server received registration request from: " + ((IRegistrationRequest) deliveredObject).getID());
            ReceivedRegistrationRequestEvent event = new ReceivedRegistrationRequestEvent((IRegistrationRequest) deliveredObject);
//            EventManager.BasicgetInstance().publishEvent(event);
            Log.log("Received Registration Request!");
            handleReceivedRegistrationRequest((IRegistrationRequest) deliveredObject, basicProperties);
        } else if (deliveredObject instanceof IStopOrderAck) {
            String idToRemove = ((IStopOrderAck) deliveredObject).getStoppedMicroServiceID();
            EventManager.getInstance().publishEvent(new MicroServiceDisconnectedEvent(idToRemove));
        }
    }

    private void handleReceivedRegistrationRequest(IRegistrationRequest deliveredObject, BasicProperties basicProperties) {
        final BasicProperties replyProps = ConnectionUtils.getReplyProps(basicProperties);
        final IRegistrationAck ack = new DefaultRegistrationAck(TASK_QUEUE_NAME);
        try {
            Log.log("Sending acknowledge connection request to microService: " + basicProperties.getCorrelationId(), LogLevel.LOW);
            channel.basicPublish("", basicProperties.getReplyTo(), replyProps, SerializationUtils.serialize(ack));
            EventManager.getInstance().publishEvent(new MicroServiceConnectedEvent(deliveredObject.getID(), deliveredObject.getIP()));
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
        final String requestID = deliveredClientRequest.getClientID();
        final BasicProperties replyProps = ConnectionUtils.getReplyProps(basicProperties);
        final CallbackInformation callbackInformation = new CallbackInformation(basicProperties, replyProps);
        clientIDtoCallbackInformation.put(requestID, callbackInformation);
        //check received secretKey
        if (!isValidSecretKey(deliveredClientRequest.getSecretKey())) {
            Log.log("Invalid secret key from request with ID '" + requestID + "'");
            channel.basicPublish("", basicProperties.getReplyTo(), clientIDtoCallbackInformation.get(requestID).replyProperties, SerializationUtils.serialize("Invalid secret key."));
            clientIDtoCallbackInformation.remove(requestID);
        } else {
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
    }

    /**
     * Class for clean storing of a tuple of BasicProperties.
     */
    private class CallbackInformation {
        private final BasicProperties basicProperties;
        private final BasicProperties replyProperties;

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
        final int countOfEntries = deliveredClientRequest.getEntries().size();
        final int countOfPartialPerEntry = firstEntry.getAmountOfExpectedPartials();
        final int requestSize = countOfEntries * countOfPartialPerEntry;

        RequestAcceptedEvent requestAcceptedEvent = new RequestAcceptedEvent(deliveredClientRequest.getClientID(), requestSize);
        EventManager.getInstance().publishEvent(requestAcceptedEvent);

        Log.log("Server successfully received a ClientRequest.");

        for (IEntry currentEntry : deliveredClientRequest.getEntries()) {
            channel.basicPublish("", TASK_QUEUE_NAME, replyProps, SerializationUtils.serialize(currentEntry));
        }
    }

    private void initBlacklist() {
        if (!Files.exists(Paths.get(DEFAULT_BLACKLIST_FILE_NAME)))
            createBlacklistFile();
        List<String> allLines;
        try {
            allLines = Files.readAllLines(Paths.get(DEFAULT_BLACKLIST_FILE_NAME));
        } catch (IOException e) {
            Log.log("Couldn't read blacklist file.", e);
            createBlacklistFile();
            allLines = new ArrayList<>();
        }

        for (String line : allLines)
            if (!isValidUUID(line))
                allLines.remove(line);
        blacklistedClients = allLines;
    }

    private void initSecretKeyFile() {
        if (!Files.exists(Paths.get(VALID_CLIENT_SECRET_KEYS_FILE_NAME)))
            createSecretKeyFile();
        List<String> allLines;
        try {
            allLines = Files.readAllLines(Paths.get(VALID_CLIENT_SECRET_KEYS_FILE_NAME));
        } catch (IOException e) {
            Log.log("Couldn't read secretKey file.", e);
            createSecretKeyFile();
            allLines = new ArrayList<>();
        }
        validSecretKeys = allLines;
    }

    private void createSecretKeyFile() {
        try {
            Files.createFile(Paths.get(VALID_CLIENT_SECRET_KEYS_FILE_NAME));
        } catch (IOException e) {
            Log.log("Couldn't create blacklist file.", e);
        }
    }

    private void createBlacklistFile() {
        try {
            Files.createFile(Paths.get(DEFAULT_BLACKLIST_FILE_NAME));
        } catch (IOException e) {
            Log.log("Couldn't create blacklist file.", e);
        }
    }

    private boolean isValidUUID(String clientID) {
        final String validChars = "[a-fA-F0-9]";
        final String[] groupLengths = {8 + "", 4 + "", 4 + "", 4 + "", 12 + ""};
        String regEx2 = validChars + "{%1$s}-"
                + validChars + "{%2$s}-"
                + validChars + "{%3$s}-"
                + validChars + "{%4$s}-"
                + validChars + "{%5$s}";
        String regEx = validChars + "{8}"
                + validChars + "{4}"
                + validChars + "{4}"
                + validChars + "{4}"
                + validChars + "{12}";
        regEx2 = String.format(regEx2, groupLengths);
        if (!clientID.matches(regEx))
            return false;
        return true;
    }

    /**
     * Tells us whether a certain client id was blacklisted.
     * Uses a persistent blacklist file.
     *
     * @param clientID The id for which we want to know the blacklisting state
     * @return A boolean.
     */
    private boolean isBlacklisted(String clientID) {
        return blacklistedClients.contains(clientID);
    }

    private boolean isValidSecretKey(String secretKey) {
        return validSecretKeys.contains(secretKey);
    }

    /**
     * Blacklists the client id.
     * Uses a persistent blacklist file.
     *
     * @param clientIDToBlock The id to block.
     */
    private void blacklistClient(String clientIDToBlock) {
        blacklistedClients.add(clientIDToBlock);
        String currentBlacklistContent = "";
        try {
            currentBlacklistContent = FileUtils.readStringFromFile(new File(DEFAULT_BLACKLIST_FILE_NAME));
        } catch (IOException e) {
            Log.log("Couldn't read blacklist-file.", e);
        }
        final String newBlacklistContent = currentBlacklistContent + System.lineSeparator();

        try {
            Files.write(Paths.get(DEFAULT_BLACKLIST_FILE_NAME), newBlacklistContent.getBytes());
        } catch (IOException e) {
            Log.log("Couldn't write blacklist-file.", e);
        }
    }

    @Override
    public void initConnectionPoint() throws IOException {
        initSecretKeyFile();
        initBlacklist();
        declareQueues();
        run();
    }

    @Override
    public void closeConnection() throws IOException, TimeoutException {
        connection.close();
    }

    @Override
    public void declareQueues() throws IOException {
        //outgoing queues
        channel.exchangeDeclare(STOP_EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
        channel.exchangeDeclare(CLIENT_CALLBACK_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
        channel.queueDeclare(TASK_QUEUE_NAME, false, false, false, null);
        //incoming queues
        channel.queueDeclare(CLIENT_REQUEST_QUEUE_NAME, false, false, false, null);
        channel.queueDeclare(callbackQueueName, false, false, false, null);
        channel.queueDeclare(REGISTRATION_QUEUE_NAME, false, false, false, null);
        clearQueues();
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
            channel.basicPublish(STOP_EXCHANGE_NAME, "", replyProps, SerializationUtils.serialize(stopMe));
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
        } else if (toNotify instanceof MicroServiceDisconnectionRequestEvent) {
            String idToRemove = ((MicroServiceDisconnectionRequestEvent) toNotify).getToDisconnectID();
            sendStopOrderToMicroService(idToRemove);
        } else if (toNotify instanceof RefreshSecretKeysEvent) {
            Log.log("Refreshing the secret keys from the secretkeys.txt in the Server directory...");
            initSecretKeyFile();
        }
    }

    @Override
    public Set<Class<? extends IEvent>> getEvents() {
        Set<Class<? extends IEvent>> events = new HashSet<>();
        events.addAll(Arrays.asList(RefreshSecretKeysEvent.class, FinishedCollectingResultEvent.class, RequestStoppedEvent.class, ClientBlockRequestEvent.class, MicroServiceConnectedEvent.class, MicroServiceDisconnectionRequestEvent.class));
        return events;
    }

    private void shutdownServer() throws IOException, TimeoutException {
        for (String msID : MicroServiceManager.getInstance().getMicroServices()) {
            sendStopOrderToMicroService(msID);
        }
        closeConnection();
    }
}
