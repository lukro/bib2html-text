package microservice;

import com.rabbitmq.client.*;
import global.controller.IConnectionPoint;
import global.identifiers.QueueNames;
import global.logging.Log;
import global.logging.LogLevel;
import global.model.IEntry;
import global.model.IStopOrder;
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
 *         TODO Implement Remote Functionality
 */
public class MicroService implements IConnectionPoint, Runnable, Consumer {

    private final String hostIP;
    //microServiceID is technically FINAL
    private final String microServiceID;
    private final String TASK_QUEUE_NAME = QueueNames.TASK_QUEUE_NAME.toString();
    private final String STOP_QUEUE_NAME = QueueNames.MICROSERVICE_STOP_QUEUE_NAME.toString();
    private final IEntryProcessor DEFAULT_PROCESSOR = new DefaultEntryProcessor();
    private final Channel channel;
    private final Connection connection;

    /**
     * use only for services, running on the same device as the server
     * @throws IOException
     * @throws TimeoutException
     */
    public MicroService() throws IOException, TimeoutException {
        this("localhost");
    }

    /**
     * use only for remote services
     * @param hostIP: ipv4 adress of the device, the server is running on
     * @throws IOException
     * @throws TimeoutException
     */
    public MicroService(String hostIP) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        this.connection = factory.newConnection();
        this.channel = connection.createChannel();
        this.microServiceID = UUID.randomUUID().toString();
        this.hostIP = hostIP;
        initConnectionPoint();
    }

    /*
     * use only for remote services!
     * TODO: remote services mit gleichem channel starten?
     */
//    public MicroService(String hostIP) throws IOException, TimeoutException {
//        this.microServiceID = UUID.randomUUID().toString();
//        ConnectionFactory factory = new ConnectionFactory();
//        factory.setHost(hostIP);
//        this.hostIP = hostIP;
//        this.channel = factory.newConnection().createChannel();
//        initConnectionPoint();
//    }

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

        Object receivedObject = SerializationUtils.deserialize(bytes);

        if(receivedObject instanceof IStopOrder) {
            if (((IStopOrder) receivedObject).getMicroServiceID().equals(microServiceID)) {
                closeConnection();
            }
        } else {
            IEntry received = SerializationUtils.deserialize(bytes);
            AMQP.BasicProperties replyProps = getReplyProps(basicProperties);
            DEFAULT_PROCESSOR.processEntry(received).forEach(partialResult -> {
                try {
                    channel.basicPublish("", basicProperties.getReplyTo(), replyProps, SerializationUtils.serialize(partialResult));
                } catch (IOException e) {
                    Log.log("Failed to send a partialresult to server", e);
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
            Log.log("Failed to init the connection point in a MicroService",e);
        }
    }

    @Override
    public void consumeIncomingQueues() throws IOException {
        channel.basicConsume(TASK_QUEUE_NAME, true, this);
        channel.basicConsume(microServiceID, true, this);
    }


    @Override
    public void closeConnection() {
        try {
            channel.close();
            Log.log("successfully disconnected microservice");
        } catch (IOException | TimeoutException e) {
            Log.log("failed to close channel on MicroService: " + microServiceID, e);
        }
    }

    @Override
    public void initConnectionPoint() throws IOException {
        declareQueues();
        consumeIncomingQueues();
    }

    @Override
    public void declareQueues() throws IOException {
        //incoming queues
        channel.queueDeclare(TASK_QUEUE_NAME, false, false, false, null);
        channel.queueDeclare(microServiceID, false, false, false, null);
        channel.queueBind(microServiceID, STOP_QUEUE_NAME, "");
    }

    @Override
    public String getHostIP() {
        return hostIP;
    }

    @Override
    public String getID() {
        return microServiceID;
    }

    private AMQP.BasicProperties getReplyProps(AMQP.BasicProperties basicProperties) {
        return new AMQP.BasicProperties
                .Builder()
                .correlationId(basicProperties.getCorrelationId())
                .build();
    }
}
