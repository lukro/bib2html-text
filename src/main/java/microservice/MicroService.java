package microservice;

import com.rabbitmq.client.*;
import global.controller.IConnectionPoint;
import global.identifiers.EntryIdentifier;
import global.identifiers.PartialResultIdentifier;
import global.model.DefaultEntry;
import global.model.DefaultPartialResult;
import global.model.IEntry;
import global.model.IPartialResult;
import org.apache.commons.lang3.SerializationUtils;
import server.modules.Server;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeoutException;

/**
 * @author Maximilian Schirm, Karsten Schaefers, daan
 *         created 05.12.2016
 *         edited 9.12.2016
 */
public class MicroService implements IConnectionPoint, Runnable, Consumer {

    private final String microServiceID, hostIP;
    private final String TASK_QUEUE_NAME = "taskQueue";
    private final String REGISTER_QUEUE_NAME = "registerQueue";

    private final Connection connection;
    private final Channel channel;

    private long currentDeliveryTag;


    public MicroService() throws IOException, TimeoutException {
        this("localhost");
    }

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        MicroService test = new MicroService();
        Collection<String> cslFiles = new ArrayList<String>();
        Collection<String> templates = new ArrayList<String>();
        String csl = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<style xmlns=\"http://purl.org/net/xbiblio/csl\" version=\"1.0\" default-locale=\"en-US\">\n" +
                "  <info>\n" +
                "    <title>AAPG Bulletin</title>\n" +
                "    <id>http://www.zotero.org/styles/aapg-bulletin</id>\n" +
                "    <link href=\"http://www.zotero.org/styles/aapg-bulletin\" rel=\"self\"/>\n" +
                "    <link href=\"http://www.zotero.org/styles/american-association-of-petroleum-geologists\" rel=\"independent-parent\"/>\n" +
                "    <link href=\"http://www.aapg.org/bulletin/reference.cfm\" rel=\"documentation\"/>\n" +
                "    <category citation-format=\"author-date\"/>\n" +
                "    <category field=\"geology\"/>\n" +
                "    <issn>0149-1423</issn>\n" +
                "    <updated>2013-03-29T23:50:45+00:00</updated>\n" +
                "    <rights license=\"http://creativecommons.org/licenses/by-sa/3.0/\">This work is licensed under a Creative Commons Attribution-ShareAlike 3.0 License</rights>\n" +
                "  </info>\n" +
                "</style>";
                cslFiles.add (csl);
        String template = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\"$if(lang)$ lang=\"$lang$\" xml:lang=\"$lang$\"$endif$$if(dir)$ dir=\"$dir$\"$endif$>\n" +
                "<head>\n" +
                "  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n" +
                "  <meta http-equiv=\"Content-Style-Type\" content=\"text/css\" />\n" +
                "  <meta name=\"generator\" content=\"pandoc\" />\n" +
                "$for(author-meta)$\n" +
                "  <meta name=\"author\" content=\"$author-meta$\" />\n" +
                "$endfor$\n" +
                "$if(date-meta)$\n" +
                "  <meta name=\"date\" content=\"$date-meta$\" />\n" +
                "$endif$\n" +
                "$if(keywords)$\n" +
                "  <meta name=\"keywords\" content=\"$for(keywords)$$keywords$$sep$, $endfor$\" />\n" +
                "$endif$\n" +
                "  <title>$if(title-prefix)$$title-prefix$ – $endif$$pagetitle$</title>\n" +
                "  <style type=\"text/css\">code{white-space: pre;}</style>\n" +
                "$if(quotes)$\n" +
                "  <style type=\"text/css\">q { quotes: \"“\" \"”\" \"‘\" \"’\"; }</style>\n" +
                "$endif$\n" +
                "$if(highlighting-css)$\n" +
                "  <style type=\"text/css\">\n" +
                "$highlighting-css$\n" +
                "  </style>\n" +
                "$endif$\n" +
                "$for(css)$\n" +
                "  <link rel=\"stylesheet\" href=\"$css$\" type=\"text/css\" />\n" +
                "$endfor$\n" +
                "$if(math)$\n" +
                "  $math$\n" +
                "$endif$\n" +
                "$for(header-includes)$\n" +
                "  $header-includes$\n" +
                "$endfor$\n" +
                "</head>\n" +
                "<body>\n" +
                "$for(include-before)$\n" +
                "$include-before$\n" +
                "$endfor$\n" +
                "$if(title)$\n" +
                "<div id=\"$idprefix$header\">\n" +
                "<h1 class=\"title\">$title$</h1>\n" +
                "$if(subtitle)$\n" +
                "<h1 class=\"subtitle\">$subtitle$</h1>\n" +
                "$endif$\n" +
                "$for(author)$\n" +
                "<h2 class=\"author\">$author$</h2>\n" +
                "$endfor$\n" +
                "$if(date)$\n" +
                "<h3 class=\"date\">$date$</h3>\n" +
                "$endif$\n" +
                "</div>\n" +
                "$endif$\n" +
                "$if(toc)$\n" +
                "<div id=\"$idprefix$TOC\">\n" +
                "$toc$\n" +
                "</div>\n" +
                "$endif$\n" +
                "$body$\n" +
                "$for(include-after)$\n" +
                "$include-after$\n" +
                "$endfor$\n" +
                "</body>\n" +
                "</html>";
        templates.add(template);
        String bibEntry = "@article{bkns-blol-10,\n" +
            "  author =\t {Michael A. Bekos and Michael Kaufmann and Martin\n" +
                    "                  N{\\\"o}llenburg and Antonios Symvonis},\n" +
                    "  title =\t {Boundary Labeling with Octilinear Leaders},\n" +
                    "  journal =\t {Algorithmica},\n" +
                    "  volume =\t 57,\n" +
                    "  number =\t 3,\n" +
                    "  year =\t 2010,\n" +
                    "  pages =\t {436-461},\n" +
                    "  ee =\t\t {http://dx.doi.org/10.1007/s00453-009-9283-6},\n" +
                    "}";

        DefaultEntry toConvert = new DefaultEntry("1",bibEntry,1,1, cslFiles, templates);
        test.convertEntry(toConvert);
    }

    public MicroService(String hostIP) throws IOException, TimeoutException {
        this(hostIP, UUID.randomUUID().toString());
    }

    public MicroService(String hostIP, String microServiceID) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(hostIP);
        this.hostIP = hostIP;
        this.microServiceID = microServiceID;
        this.connection = factory.newConnection();
        this.channel = connection.createChannel();
        initConnectionPoint();
    }

    private DefaultPartialResult convertEntry(DefaultEntry toConvert) throws InterruptedException {
        //TODO : Replace Dummy code
        int identifier = toConvert.getEntryIdentifier().getBibFileIndex();
        String cslName = identifier + ".csl";
        String templateName = identifier + "_template.html";
        String bibName = identifier + ".bib";
        String mdName = identifier + ".md";
        // directoryName = "temp";
        try {
            PrintWriter csl = new PrintWriter(cslName, "UTF-8");
            PrintWriter template = new PrintWriter(templateName, "UTF-8");
            PrintWriter bib = new PrintWriter(bibName, "UTF-8");
            PrintWriter md = new PrintWriter(mdName);
            bib.write(toConvert.getContent());
            csl.write(toConvert.getCslFiles().get(0));
            template.write(toConvert.getTemplates().get(0));
            md.write("--- \nbibliography: " + identifier + ".bib\nnocite: \"@*\" \n...");
            pandocDoWork(cslName, templateName, mdName, channel, 1, toConvert.getEntryIdentifier());///
            byte[] convertedContentEncoded = Files.readAllBytes(Paths.get(identifier + "_result.html"));
            String convertedContent = new String(convertedContentEncoded);
            DefaultPartialResult convertedEntry = new DefaultPartialResult(convertedContent, new PartialResultIdentifier(toConvert.getEntryIdentifier(),1,1));
            Files.delete(Paths.get(identifier + ".csl"));
            Files.delete(Paths.get(identifier + "_template.html"));
            Files.delete(Paths.get(identifier + ".bib"));
            Files.delete(Paths.get(identifier + ".md"));
            return convertedEntry;
        }
        catch(IOException e) {
            e.printStackTrace();
            return null;
        }
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
        channel.basicAck(currentDeliveryTag,false);
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
            e.printStackTrace();
        }
    }

    @Override
    public void consumeIncomingQueues() throws IOException {
        channel.basicConsume(TASK_QUEUE_NAME, false, this);
    }


    @Override
    public void closeConnection() throws IOException, TimeoutException {
        channel.close();
        connection.close();
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