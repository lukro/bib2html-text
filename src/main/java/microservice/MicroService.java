package microservice;

import com.rabbitmq.client.*;
import com.rabbitmq.client.AMQP.BasicProperties;
import global.controller.IConnectionPoint;
import global.identifiers.QueueNames;
import global.logging.Log;
import global.logging.LogLevel;
import global.model.*;
import global.util.ConnectionUtils;
import microservice.model.processor.DefaultEntryProcessor;
import microservice.model.processor.IEntryProcessor;
import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

/**
 * @author Maximilian Schirm, Karsten Schaefers, daan
 *         created 05.12.2016
 *         <p>
 */
public class MicroService implements IConnectionPoint, Runnable, Consumer {

    private final String hostIP;
    private final String microServiceID;
    private final String REGISTRATION_QUEUE_NAME = QueueNames.MICROSERVICE_REGISTRATION_QUEUE_NAME.toString();
    private final String STOP_EXCHANGE_NAME = QueueNames.STOP_EXCHANGE_NAME.toString();
    private final String registrationCallbackQueueName;
    private final String stopQueueName;
    private String taskQueueName = "";

    private volatile boolean isRunning = false;

    private final IEntryProcessor DEFAULT_PROCESSOR = new DefaultEntryProcessor();
    private final Connection connection;
    private final Channel channel;

    private final BasicProperties registrationReplyProps;

    public static void main(String... args) {
        final MicroService createdService;
        try {
            createdService = (args.length == 0) ? new MicroService() : new MicroService(args[0]);
            createdService.start();
        } catch (IOException | TimeoutException e) {
            Log.log("failed to start microService.", e);
        }
    }

    /**
     * use only for LOCAL services, isRunning on the same device as the server
     *
     * @throws IOException      if rabbitmq couldn't create channel or connection
     * @throws TimeoutException if rabbitmq timeouts
     */
    public MicroService() throws IOException, TimeoutException {
        this("localhost");
    }

    /**
     * use only for REMOTE services
     *
     * @param hostIP: ipv4 adress of the device, the server is isRunning on
     * @throws IOException      if rabbitmq couldn't create channel or connection
     * @throws TimeoutException if rabbitmq timeouts
     */
    public MicroService(String hostIP) throws IOException, TimeoutException {
        this.hostIP = hostIP;
        this.microServiceID = UUID.randomUUID().toString();
        this.registrationCallbackQueueName = microServiceID;
        this.stopQueueName = QueueNames.MICROSERVICE_STOP_QUEUE_NAME.toString() + microServiceID;
        final ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(hostIP);
        this.connection = factory.newConnection();
        this.channel = connection.createChannel();
        this.registrationReplyProps = new BasicProperties
                .Builder()
                .correlationId(microServiceID)
                .replyTo(registrationCallbackQueueName)
                .build();
    }

    @Override
    public void handleConsumeOk(String s) {

    }

    @Override
    public void handleCancelOk(String s) {
        Log.log("MicroService cancelled: " + microServiceID);
    }

    @Override
    public void handleCancel(String s) throws IOException {
        Log.log("MicroService cancelled: " + microServiceID);
    }

    @Override
    public void handleShutdownSignal(String s, ShutdownSignalException e) {

    }

    @Override
    public void handleRecoverOk(String s) {

    }

    @Override
    public void handleDelivery(String s, Envelope envelope, AMQP.BasicProperties basicProperties, byte[] bytes) throws IOException {
        Log.log("MicroService (ID: " + microServiceID + " received a message", LogLevel.LOW);
        final Object receivedObject = SerializationUtils.deserialize(bytes);
        final AMQP.BasicProperties replyProps = ConnectionUtils.getReplyProps(basicProperties);
        if (receivedObject instanceof IStopOrder) {
            if (((IStopOrder) receivedObject).getMicroServiceID().equals(microServiceID)) {
                Log.log("Stopping MicroService", LogLevel.INFO);
                sendStopOrderAck(basicProperties, replyProps);
                terminate();
                Log.log("Disconnected MicroService", LogLevel.INFO);
            }
        } else if (receivedObject instanceof IRegistrationAck) {
            consumeReceivedTaskQueue(((IRegistrationAck) receivedObject));
        } else if (receivedObject instanceof IEntry) {
            IEntry received = SerializationUtils.deserialize(bytes);
            DEFAULT_PROCESSOR.processEntry(received).forEach(partialResult -> {
                try {
                    channel.basicPublish("", basicProperties.getReplyTo(), replyProps, SerializationUtils.serialize(partialResult));
                } catch (IOException e) {
                    Log.log("Failed to send a PartialResult to server", e);
                }
            });
            //TODO: IN THE END!!: uncomment & change boolean in consumeIncomingQueues()
//        channel.basicAck(envelope.getDeliveryTag(), false);
        }
    }

    @Override
    public void run() {
        try {
            final String originalThreadName = Thread.currentThread().getName();
            Thread.currentThread().setName(originalThreadName + " - A MicroService Thread");
            initConnectionPoint();
        } catch (IOException e) {
            Log.log("Failed to init connection point in a MicroService", e);
        }
        while (isRunning) {
            //microService isRunning
        }
        this.closeConnection();
    }

    @Override
    public void closeConnection() {
        try {
            channel.close();
            connection.close();
            Log.log("successfully disconnected microservice", LogLevel.INFO);
        } catch (IOException | TimeoutException e) {
            Log.log("failed to close channel/connection from MicroService: " + microServiceID, e);
        }
    }

    @Override
    public void initConnectionPoint() throws IOException {
        declareQueues();
        consumeIncomingQueues();
        initRegistrationProcess();
        Log.log("new microServiceID: " + microServiceID, LogLevel.INFO);
    }

    @Override
    public void declareQueues() throws IOException {
        //incoming queues
        channel.queueDeclare(REGISTRATION_QUEUE_NAME, false, false, false, null);
        channel.queueDeclare(registrationCallbackQueueName, false, false, false, null);
        channel.queueDeclare(stopQueueName, false, false, false, null);
        //declare exchange
        channel.exchangeDeclare(STOP_EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
        channel.queueBind(stopQueueName, STOP_EXCHANGE_NAME, "");
    }

    @Override
    public void consumeIncomingQueues() throws IOException {
        channel.basicConsume(registrationCallbackQueueName, true, this);
        channel.basicConsume(stopQueueName, true, this);
    }

    private void consumeReceivedTaskQueue(IRegistrationAck registrationAck) {
        final String receivedTaskQueueName = registrationAck.getTaskQueueName();
        this.taskQueueName = receivedTaskQueueName;
        Log.log("successfully received acknowledgement: " + taskQueueName, LogLevel.INFO);
        try {
            channel.queueDeclare(taskQueueName, false, false, false, null);
            channel.basicConsume(taskQueueName, true, this);
        } catch (IOException e) {
            Log.log("couldn't declare/consume taskQueue received from server.", e);
        }
    }

    private void initRegistrationProcess() throws IOException {
        IRegistrationRequest registrationRequest = new DefaultRegistrationRequest(microServiceID, hostIP);
        channel.basicPublish("", REGISTRATION_QUEUE_NAME, registrationReplyProps, SerializationUtils.serialize(registrationRequest));
    }

    @Override
    public String getHostIP() {
        return hostIP;
    }

    @Override
    public String getID() {
        return microServiceID;
    }

    private void sendStopOrderAck(AMQP.BasicProperties basicProperties, AMQP.BasicProperties replyProps) throws IOException {
        IStopOrderAck stopOrderAck = new DefaultStopOrderAck(this.getID(), this.getHostIP());
        channel.basicPublish("", basicProperties.getReplyTo(), replyProps, SerializationUtils.serialize(stopOrderAck));
    }

    private void start() {
        Thread serviceThread = new Thread(this);
        serviceThread.start();
        isRunning = true;
    }

    private void terminate() {
        isRunning = false;
    }

}
