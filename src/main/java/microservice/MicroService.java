package microservice;

import com.rabbitmq.client.*;
import global.controller.IConnectionPoint;
import global.identifiers.EntryIdentifier;
import global.identifiers.FileType;
import global.identifiers.PartialResultIdentifier;
import global.logging.Log;
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
import java.util.*;
import java.util.concurrent.TimeoutException;

/**
 * @author Maximilian Schirm, Karsten Schaefers, daan
 *         created 05.12.2016
 */
public class MicroService implements IConnectionPoint, Runnable, Consumer {

    private final String hostIP;
    //microServiceID is technically FINAL
    private String microServiceID;
    private final String TASK_QUEUE_NAME = "taskQueue";

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

    private List<IPartialResult> convertEntry(IEntry toConvert) throws InterruptedException {
        List<IPartialResult> result = new ArrayList<IPartialResult>();
        final HashMap<FileType, String> fileIdentifiers = createFileIdentifiersFromIEntry(toConvert);

        int expectedAmountOfPartials, finishedPartialsCounter;
        expectedAmountOfPartials = finishedPartialsCounter = 0;

        //TODO: check defaultCsl and defaultTemplate and INIT correct pls!
        final String defaultCsl, defaultTemplate;
        defaultCsl = "";
        defaultTemplate = "";

        try {
            //create .bib-file with entry-content
            Files.write(Paths.get(fileIdentifiers.get(FileType.BIB)), toConvert.getContent().getBytes());

            //declare csl/template-lists we want to use
            final ArrayList<String> cslFilesToUse, templatesToUse;

            //BEGIN: check if entry contains at least 1 .csl-file AND/OR at least 1 template
            if (toConvert.getCslFiles().size() == 0) {
                //use defaultCslFile
                cslFilesToUse = new ArrayList<>(Arrays.asList(defaultCsl));
            } else
                cslFilesToUse = new ArrayList<>(toConvert.getCslFiles());
            if (toConvert.getTemplates().size() == 0) {
                //use defaultTemplate
                templatesToUse = new ArrayList<>(Arrays.asList(defaultTemplate));
            } else
                templatesToUse = new ArrayList<>(toConvert.getTemplates());
            //END: check if entry contains at least 1 .csl-file AND/OR at least 1 template

            final String mdString = "--- \nbibliography: " + toConvert.hashCode() + ".bib\nnocite: \"@*\" \n...";
            expectedAmountOfPartials = cslFilesToUse.size() * templatesToUse.size();

            final boolean[] templateValidator = validateTemplates(toConvert, fileIdentifiers);
            boolean firstInvalidTemplateReplacedByDefaultTemplate = false;

            //BEGIN: iterate over all .csl-files and templates and do pandoc work
            for (int cslFileIndex = 0; cslFileIndex < cslFilesToUse.size(); cslFileIndex++) {
                Files.write(Paths.get(fileIdentifiers.get(FileType.CSL)), cslFilesToUse.get(cslFileIndex).getBytes());

                for (int templateFileIndex = 0; templateFileIndex < templatesToUse.size(); templateFileIndex++) {
                    if (!templateValidator[templateFileIndex]) {
                        //currentTemplate is invalid
                        if (!firstInvalidTemplateReplacedByDefaultTemplate) {
                            Files.write(Paths.get(fileIdentifiers.get(FileType.TEMPLATE)), defaultTemplate.getBytes());
                            firstInvalidTemplateReplacedByDefaultTemplate = true;
                        } else {
                            //don't use invalid template and don't replace it, defaultTemplate is already in use
                            expectedAmountOfPartials--;
                            break;
                        }
                    } else {
                        //currentTemplate is valid
                        Files.write(Paths.get(fileIdentifiers.get(FileType.TEMPLATE)), templatesToUse.get(templateFileIndex).getBytes());
                    }
                    Files.write(Paths.get(fileIdentifiers.get(FileType.MD)), mdString.getBytes());
                    pandocDoWork(
                            fileIdentifiers.get(FileType.CSL),
                            fileIdentifiers.get(FileType.TEMPLATE),
                            fileIdentifiers.get(FileType.MD),
                            toConvert.getEntryIdentifier()
                    );
                    final byte[] convertedContentEncoded = Files.readAllBytes(Paths.get(fileIdentifiers.get(FileType.RESULT)));
                    final String convertedContent = new String(convertedContentEncoded);
                    final IPartialResult currentPartialResult = new DefaultPartialResult(
                            convertedContent,
                            new PartialResultIdentifier(toConvert.getEntryIdentifier(), cslFileIndex, templateFileIndex)
                    );
                    result.add(currentPartialResult);
                    finishedPartialsCounter++;
                }
            }
            //END: iterate over all .csl-files and templates and do pandoc work
            //delete all temporary files except resultFile
            for (Map.Entry currentEntryInHashMap : fileIdentifiers.entrySet()) {
                if (currentEntryInHashMap.getKey() != FileType.RESULT)
                    Files.delete(Paths.get((String) currentEntryInHashMap.getValue()));
            }
        } catch (IOException e) {
            final int amountOfPartialsWithErrors = expectedAmountOfPartials - finishedPartialsCounter;
            //TODO: reduce expected result-size in partialresult-collector by 'amountOfPartialsWithErrors'
            Log.log("Error in microservice.convertEntry(). " + finishedPartialsCounter + "/ " + expectedAmountOfPartials + " Partials succesfully created.", e);
        }
        //TODO: check Acknowledgement
//        channel.basicAck(currentDeliveryTag, false);
        return result;
    }

    private HashMap<FileType, String> createFileIdentifiersFromIEntry(IEntry iEntry) {
        HashMap<FileType, String> result = new HashMap<>();
        String hashCodeAsString = Integer.toString(iEntry.getEntryIdentifier().hashCode());
        result.put(FileType.BIB, hashCodeAsString + ".bib");
        result.put(FileType.CSL, hashCodeAsString + ".csl");
        result.put(FileType.TEMPLATE, hashCodeAsString + "_template.html");
        result.put(FileType.MD, hashCodeAsString + ".md");
        result.put(FileType.RESULT, hashCodeAsString + "_result.html");
        return result;
    }


    private boolean[] validateTemplates(IEntry iEntry, HashMap<FileType, String> fileIdentifiers) {
        boolean[] output = new boolean[iEntry.getTemplates().size()];
        for (int i = 0; i < iEntry.getTemplates().size(); i++) {
            //TODO : Use pandocDoWork(..) to retrieve execution success.
            //Simulating 30% error rate
            int conversionResultState = (Math.random() > 0.7) ? 1 : 0;
            output[i] = (conversionResultState > 0) ? false : true;
        }
        return output;
    }

    private static int pandocDoWork(String cslName, String templateName, String wrapperName, EntryIdentifier entryIdentifier) throws IOException, InterruptedException {
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
