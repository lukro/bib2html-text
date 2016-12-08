package microservice;

import client.controller.ConnectionPoint;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import global.model.DefaultEntry;
import global.model.DefaultPartialResult;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

/**
 * @author Maximilian Schirm, Karsten Schaefers
 * @created 05.12.2016
 * @edited 7.12.2016
 */
public class MicroService extends ConnectionPoint implements Consumer, Runnable {
    //TODO : Fill...
    private final String routingKey;
    private final String registerQueueName;

    public MicroService(String routingKey, String registerQueueName) throws IOException, TimeoutException {
        super();
        this.routingKey = routingKey;
        this.registerQueueName = registerQueueName;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public String getRegisterQueueName() {
        return registerQueueName;
    }

    private DefaultPartialResult convertEntry(DefaultEntry toConvert) {
        //TODO : Replace Dummy code
        return null;
    }

    private boolean[] validateTempaltes(DefaultEntry defaultEntry, HashSet<byte[]> templateFiles) {
        boolean[] output = new boolean[templateFiles.size()];
        for (int i = 0; i < templateFiles.size(); i++) {
            //TODO : Use pandocDoWork(..) to retrieve execution success.
            //Simulating 30% error rate
            int conversionResultState = (Math.random() > 0.7) ? 1 : 0;
            output[i] = (conversionResultState > 0) ? false : true;
        }
        return output;
    }

    private static int pandocDoWork(File directory, String cslName, String wrapperName) throws IOException, InterruptedException {
        Objects.requireNonNull(directory);
        Objects.requireNonNull(cslName);
        Objects.requireNonNull(wrapperName);

        File cslFile = new File(directory, cslName);
        File wrapperFile = new File(directory, wrapperName);

        if (!cslFile.exists() || !wrapperFile.exists())
            throw new IllegalArgumentException("A file with that name might not exist!");

        String command = "pandoc --filter=pandoc-citeproc --csl=" + cslName + ".csl --standalone " + wrapperName + ".md -o " + cslName + ".HTML";
        return Runtime.getRuntime().exec(command, null, directory).waitFor();
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
        System.out.println("MicroService received message");
    }

    @Override
    public void run() {
        try {
            channel.basicConsume(routingKey, true, this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
