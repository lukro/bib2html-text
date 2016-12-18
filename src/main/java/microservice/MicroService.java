package microservice;

import com.rabbitmq.client.*;
import global.controller.IConnectionPoint;
import global.identifiers.PartialResultIdentifier;
import global.logging.Log;
import global.model.DefaultEntry;
import global.model.DefaultPartialResult;
import global.model.IEntry;
import global.model.IPartialResult;
import org.apache.commons.lang3.SerializationUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

/**
 * @author Maximilian Schirm, Karsten Schaefers, daan
 *         created 05.12.2016
 *         edited 9.12.2016
 */
public class MicroService implements IConnectionPoint, Runnable, Consumer {

    private final String hostIP;
    private String microServiceID;
    private final String TASK_QUEUE_NAME = "taskQueue";
    private final String REGISTER_QUEUE_NAME = "registerQueue";

//    private final Connection connection;
    private final Channel channel;

    private long currentDeliveryTag;

    public MicroService(Channel channel) throws IOException, TimeoutException {
        this(channel,"localhost");
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

    private DefaultPartialResult convertEntry(DefaultEntry toConvert) {
        //TODO : Replace Dummy code
        return null;
    }

    private boolean[] validateTemplates(DefaultEntry defaultEntry, HashSet<byte[]> templateFiles) {
        boolean[] output = new boolean[templateFiles.size()];
        for (int i = 0; i < templateFiles.size(); i++) {
            //TODO : Use pandocDoWork(..) to retrieve execution success.
            //Simulating 30% error rate
            int conversionResultState = (Math.random() > 0.7) ? 1 : 0;
            output[i] = (conversionResultState > 0) ? false : true;
        }
        return output;
    }

    private static int pandocDoWork(File directory, String cslName, String wrapperName, Channel channel, long currentDeliveryTag) throws IOException, InterruptedException {
        Objects.requireNonNull(directory);
        Objects.requireNonNull(cslName);
        Objects.requireNonNull(wrapperName);

        File cslFile = new File(directory, cslName);
        File wrapperFile = new File(directory, wrapperName);

        if (!cslFile.exists() || !wrapperFile.exists())
            throw new IllegalArgumentException("A file with that name might not exist!");

        String command = "pandoc --filter=pandoc-citeproc --csl=" + cslName + ".csl --standalone " + wrapperName + ".md -o " + cslName + ".HTML";
        channel.basicAck(currentDeliveryTag, false);
        return Runtime.getRuntime().exec(command, null, directory).waitFor();

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
        System.out.println("MicroService received message " + microServiceID);
        doHardWorkEfficiently(basicProperties, bytes);
        envelope.getDeliveryTag();
    }

    private void doHardWorkEfficiently(AMQP.BasicProperties basicProperties, byte[] bytes) throws IOException {
        IEntry entryReceived = SerializationUtils.deserialize(bytes);
        String dummyContent = entryReceived.getContent();
        AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                .Builder()
                .correlationId(basicProperties.getCorrelationId())
                .build();
        IPartialResult dummyResult = new DefaultPartialResult(dummyContent, new PartialResultIdentifier(entryReceived.getEntryIdentifier(), 1, 1, false));
        channel.basicPublish("", basicProperties.getReplyTo(), replyProps, SerializationUtils.serialize(dummyResult));
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
