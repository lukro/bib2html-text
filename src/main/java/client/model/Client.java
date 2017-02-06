package client.model;

import client.controller.BibTeXFileSplitter;
import com.rabbitmq.client.*;
import com.rabbitmq.client.AMQP.BasicProperties;
import global.controller.IConnectionPoint;
import global.identifiers.QueueNames;
import global.logging.Log;
import global.logging.LogLevel;
import global.model.DefaultClientRequest;
import global.model.IClientRequest;
import global.model.IResult;
import org.apache.commons.lang3.SerializationUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

/**
 * @author daan
 *         created on 11/30/16.
 *
 *  Client's main class. Used by the various controller implementations and tests.
 */
public class Client implements IConnectionPoint, Runnable, Consumer {

    private final String clientID, callbackQueueName;
    private final String CLIENT_REQUEST_QUEUE_NAME = QueueNames.CLIENT_REQUEST_QUEUE_NAME.toString();
    private final String CLIENT_CALLBACK_EXCHANGE_NAME = QueueNames.CLIENT_CALLBACK_EXCHANGE_NAME.toString();
    private String hostIP;
    private String secretKey = "";

    private Connection connection;
    private Channel channel;
    private final BasicProperties replyProps;

    private ClientFileModel clientFileModel;
    private String outputDirectory;
    private final String DEFAULT_RESULT_PREFIX = "result";
    private final SimpleDateFormat DEFAULT_TIMESTAMP_FORMAT = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss");
    private final String DEFAULT_RESULT_FILE_EXTENSION = ResultFileExtension.HTML.toString();
    private String resultFileExtension = DEFAULT_RESULT_FILE_EXTENSION;


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
        this.replyProps = new BasicProperties
                .Builder()
                .correlationId(clientID)
                .replyTo(callbackQueueName)
                .build();
        this.outputDirectory = "";
    }

    @Override
    public void run() {
        try {
            initConnectionPoint();
        } catch (IOException e) {
            Log.log("failed to consume incoming queues in client.run()", e);
        }
    }

    private long timeStart = 0;

    private long clientRequestSize = 0;
    private final double AMOUNT_OF_SECS = 59;
    public void sendClientRequest() throws IOException {
        //time measuring starts before request creation
        //timeStart = System.currentTimeMillis();
        IClientRequest clientRequestToSend = this.createClientRequest();
        channel.basicPublish("", CLIENT_REQUEST_QUEUE_NAME, replyProps, SerializationUtils.serialize(clientRequestToSend));
        //time measuring starts after request creation
        timeStart = System.currentTimeMillis();
        clientRequestSize = clientRequestToSend.getEntries().size();
        Log.log("Client with ID: " + this.clientID + " sent a ClientRequest.", LogLevel.INFO);
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
        logTimeAndWorkingLoadLimit();
        final Object deliveredObject = SerializationUtils.deserialize(bytes);
        if (deliveredObject instanceof IResult) {
            Log.log("Message is instance of IResult.", LogLevel.INFO);
            handleResult((IResult) deliveredObject);
        } else {
            //TODO: log message debuggen (start zeichen)
            Log.log("SERVER: " + new String(bytes), LogLevel.SEVERE);
        }
    }

    private void logTimeAndWorkingLoadLimit() {
        long timeEnd = System.currentTimeMillis();
        long timeTakenMillis = (timeEnd - timeStart);
//        Log.log("timeTakenInMillis: " + timeTakenMillis, LogLevel.INFO);
        double timeTakenSecs = timeTakenMillis / 1000.0;
//        Log.log("timeTakenInSecs: " + timeTakenSecs, LogLevel.INFO);
        int timeTakenFullMins = ((Double) (timeTakenSecs / 60.0)).intValue();
//        Log.log("timeTakenInFullMins: " + timeTakenFullMins, LogLevel.INFO);
        int timeTakenFullMinsInSecs = timeTakenFullMins * 60;
//        Log.log("timeTakenFullMinsInSec: " + timeTakenFullMinsInSecs);
        int timeTakenSecDifference = ((Double) (timeTakenSecs - timeTakenFullMinsInSecs)).intValue();
        Log.log("TIME TAKEN: " + timeTakenFullMins + " min " + timeTakenSecDifference + " sec");
        int workingLoadLimitXSecs = ((Double) ((double) clientRequestSize / timeTakenSecs * AMOUNT_OF_SECS)).intValue();
        Log.log("WORKING LOAD LIMIT: " + workingLoadLimitXSecs + " entries in " + AMOUNT_OF_SECS + " secs. ");
    }

    private void handleResult(IResult result) {
        final File outDir = new File(outputDirectory);
        int additionalIndex = 0;
        for (String resultContent : result.getFileContents()) {
            final String timeStamp = DEFAULT_TIMESTAMP_FORMAT.format(new Date());
            final String filename = DEFAULT_RESULT_PREFIX + "_" + timeStamp + additionalIndex + resultFileExtension;

            if (!outDir.exists())
                Log.log("Output-directory doesn't exist!", LogLevel.SEVERE);
            else {
                try {
                    Files.write(new File(outDir, filename).toPath(), resultContent.getBytes());
                } catch (IOException e) {
                    Log.log("Failed to write output file", e);
                }
            }
            additionalIndex++;
        }
    }

    @Override
    public void closeConnection() throws IOException, TimeoutException {
        channel.close();
        connection.close();
    }

    @Override
    public void initConnectionPoint() throws IOException {
        declareQueues();
        consumeIncomingQueues();
        Log.log("initialised the connection point");
    }

    @Override
    public void declareQueues() throws IOException {
        //outgoing queues
        channel.queueDeclare(CLIENT_REQUEST_QUEUE_NAME, false, false, false, null);
        //incoming queues
        channel.queueDeclare(callbackQueueName, false, false, false, null);

//        channel.exchangeDeclare(CLIENT_CALLBACK_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
//        channel.queueBind(callbackQueueName, CLIENT_CALLBACK_EXCHANGE_NAME, clientID);
    }

    @Override
    public void consumeIncomingQueues() throws IOException {
        channel.basicConsume(callbackQueueName, true, this);
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

    public ClientFileModel getClientFileModel() {
        return clientFileModel;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public void setResultFileExtension(ResultFileExtension resultFileExtension) {
        this.resultFileExtension = resultFileExtension.toString();
    }

    private IClientRequest createClientRequest() throws IOException {
        return new DefaultClientRequest(secretKey, clientID, BibTeXFileSplitter.INSTANCE.createIEntryListFromClientFileModel(clientFileModel));
    }

    public void setKeyToUse(String secretKey) {
        this.secretKey = secretKey;
        Log.log("Set secretKey to: " + secretKey);
    }
}
