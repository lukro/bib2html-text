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
    private final String registrationCallbackQueueName;
    private final String STOP_QUEUE_NAME = QueueNames.MICROSERVICE_STOP_QUEUE_NAME.toString();
    private String taskQueueName = "";

    private final IEntryProcessor DEFAULT_PROCESSOR = new DefaultEntryProcessor();
    private final Connection connection;
    private final Channel channel;

    private final BasicProperties registrationReplyProps;

    public static void main(String... args) {
        try {
            final MicroService createdService = (args.length == 0) ? new MicroService() : new MicroService(args[0]);
            final Thread serviceThread = new Thread(createdService);
            serviceThread.start();
        } catch (IOException | TimeoutException e) {
            Log.log("failed to start microService.", e);
            Thread.currentThread().stop();
        }
    }

    /**
     * use only for LOCAL services, running on the same device as the server
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
     * @param hostIP: ipv4 adress of the device, the server is running on
     * @throws IOException      if rabbitmq couldn't create channel or connection
     * @throws TimeoutException if rabbitmq timeouts
     */
    public MicroService(String hostIP) throws IOException, TimeoutException {
        this.hostIP = hostIP;
        this.microServiceID = UUID.randomUUID().toString();
        this.registrationCallbackQueueName = microServiceID;
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
                Log.log("Stopping MicroService", LogLevel.WARNING);
                sendStopOrderAck(basicProperties, replyProps);
                closeConnection();
                Log.log("Disconnected MicroService", LogLevel.WARNING);
                Thread.currentThread().stop();
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
            Log.log("Failed to init the connection point in a MicroService", e);
        }
    }

    @Override
    public void closeConnection() {
        try {
            channel.close();
            connection.close();
            Log.log("successfully disconnected microservice");
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
        channel.queueDeclare(STOP_QUEUE_NAME, false, false, false, null);
        channel.queueBind(STOP_QUEUE_NAME, QueueNames.STOP_EXCHANGE_NAME.toString(), "");
    }

    @Override
    public void consumeIncomingQueues() throws IOException {
        channel.basicConsume(registrationCallbackQueueName, true, this);
        channel.basicConsume(STOP_QUEUE_NAME, true, this);
    }

    private void consumeReceivedTaskQueue(IRegistrationAck registrationAck) {
        final String receivedTaskQueueName = registrationAck.getTaskQueueName();
        this.taskQueueName = receivedTaskQueueName;
        Log.log("successfully received acknowledgement: " + taskQueueName);
        try {
            channel.queueDeclare(taskQueueName, false, false, false, null);
            channel.basicConsume(taskQueueName, true, this);
        } catch (IOException e) {
            Log.log("couldn't declare/consume taskQueue received from server.", e);
        }
    }

    private void initRegistrationProcess() throws IOException {
        IRegistrationRequest registrationRequest = new DefaultRegistrationRequest(hostIP, microServiceID);
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

}
