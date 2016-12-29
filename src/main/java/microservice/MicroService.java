package microservice;

import com.rabbitmq.client.*;
import global.controller.IConnectionPoint;
import global.identifiers.QueueNames;
import global.logging.Log;
import global.model.IEntry;
import microservice.model.processor.DefaultEntryProcessor;
import microservice.model.processor.IEntryProcessor;
import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author Maximilian Schirm, Karsten Schaefers, daan
 *         created 05.12.2016
 *
 *         TODO Implement Remote Functionality
 */
public class MicroService implements IConnectionPoint, Runnable, Consumer {

    private final String hostIP;
    //microServiceID is technically FINAL
    private String microServiceID;
    private final String TASK_QUEUE_NAME = QueueNames.TASK_QUEUE_NAME.toString();
    private final IEntryProcessor DEFAULT_PROCESSOR = new DefaultEntryProcessor();
    private final Channel channel;

    public MicroService(Channel channel) throws IOException, TimeoutException {
        this(channel, "localhost");
    }

    public MicroService(Channel channel, String hostIP) throws IOException, TimeoutException {
        this.hostIP = hostIP;
        this.channel = channel;
        initConnectionPoint();
    }

    /*
     * use only for remote services!
     * TODO: remote services mit gleichem channel starten?
     */
    public MicroService(String hostIP) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(hostIP);
        this.hostIP = hostIP;
        this.channel = factory.newConnection().createChannel();
        initConnectionPoint();
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
        System.out.println("MicroService (ID: " + microServiceID + " received a message");

        IEntry received = SerializationUtils.deserialize(bytes);
        DEFAULT_PROCESSOR.processEntry(received).forEach(partialResult -> {
            try {
                channel.basicPublish("", basicProperties.getReplyTo(), basicProperties, SerializationUtils.serialize(partialResult));
            } catch (IOException e) {
                Log.log("Failed to send a partialresult to client",e);
            }
        });

        envelope.getDeliveryTag();
    }

    @Override
    public void run() {
        try {
            consumeIncomingQueues();
        } catch (IOException e) {
            Log.log("failed to consume incoming queues in microservice.run()", e);
        }
    }

    @Override
    public void consumeIncomingQueues() throws IOException {
        //TODO: autoAck raus, manuelles Ack rein
        this.microServiceID = channel.basicConsume(TASK_QUEUE_NAME, true, this);
    }


    @Override
    public void closeConnection() throws IOException, TimeoutException {
        channel.close();
    }

    @Override
    public void initConnectionPoint() throws IOException {
        declareQueues();
        run();
    }

    @Override
    public void declareQueues() throws IOException {
        //incoming queues
        channel.queueDeclare(TASK_QUEUE_NAME, false, false, false, null);
        //Registration Queue?
    }

    @Override
    public String getHostIP() {
        return hostIP;
    }

    @Override
    public String getID() {
        return microServiceID;
    }
}
