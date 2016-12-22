package microservice;

import com.rabbitmq.client.*;
import global.controller.IConnectionPoint;
import global.identifiers.EntryIdentifier;
import global.identifiers.PartialResultIdentifier;
import global.logging.Log;
import global.model.DefaultEntry;
import global.model.DefaultPartialResult;
import global.model.IEntry;
import global.model.IPartialResult;
import org.apache.commons.lang3.SerializationUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

/**
 * @author Maximilian Schirm, Karsten Schaefers, daan
 *         created 05.12.2016
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

    private List<DefaultPartialResult> convertEntry(DefaultEntry toConvert) throws InterruptedException, IOException {
        //TODO : Replace Dummy code
        List<DefaultPartialResult> convertedEntries = new ArrayList<DefaultPartialResult>();
        int identifier = toConvert.getEntryIdentifier().getBibFileIndex();
        String cslName = identifier + ".csl";
        String templateName = identifier + "_template.html";
        String bibName = identifier + ".bib";
        String mdName = identifier + ".md";
        try {
            Files.write(Paths.get(bibName), toConvert.getContent().getBytes());
            for (String cslFile : toConvert.getCslFiles()) {
                for (String templateFile : toConvert.getTemplates()) {
                    Files.write(Paths.get(cslName), cslFile.getBytes());
                    Files.write(Paths.get(templateName), templateFile.getBytes());
                    String mdString =  "--- \nbibliography: " + identifier + ".bib\nnocite: \"@*\" \n...";
                    Files.write(Paths.get(mdName), mdString.getBytes());

                    pandocDoWork(cslName, templateName, mdName, channel, 1, toConvert.getEntryIdentifier());///
                    byte[] convertedContentEncoded = Files.readAllBytes(Paths.get(identifier + "_result.html"));
                    String convertedContent = new String(convertedContentEncoded);
                    DefaultPartialResult convertedEntry = new DefaultPartialResult(convertedContent, new PartialResultIdentifier(toConvert.getEntryIdentifier(), 1, 1));
                    convertedEntries.add(convertedEntry);
                }
            }
            Files.delete(Paths.get(cslName));
            Files.delete(Paths.get(templateName));
            Files.delete(Paths.get(bibName));
            Files.delete(Paths.get(mdName));
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        channel.basicAck(currentDeliveryTag,false);
        return convertedEntries;
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

    private static int pandocDoWork(String cslName, String templateName, String wrapperName, Channel channel, long currentDeliveryTag, EntryIdentifier entryIdentifier) throws IOException, InterruptedException {
        Objects.requireNonNull(cslName);
        Objects.requireNonNull(wrapperName);

        File cslFile = new File(cslName);
        File wrapperFile = new File(wrapperName);
        File template = new File(templateName);

        if (!cslFile.exists() || !wrapperFile.exists())
            throw new IllegalArgumentException("A file with that name might not exist!");

        String command = "pandoc --filter=pandoc-citeproc --template " + templateName + " --csl " + cslName + " --standalone " + wrapperName + " -o " + entryIdentifier.getBibFileIndex() + "_result.html";
        System.out.println(command);
        Process p = Runtime.getRuntime().exec(command, null);
        BufferedReader input = new BufferedReader(new
                InputStreamReader(p.getInputStream()));
        String line;
        while ((line = input.readLine()) != null) {
            System.out.println(line);
        }
        return p.waitFor();
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
        IPartialResult dummyResult = new DefaultPartialResult(dummyContent, new PartialResultIdentifier(entryReceived.getEntryIdentifier(), 1,1,false));
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
