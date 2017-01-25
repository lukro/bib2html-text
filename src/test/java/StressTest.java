import client.model.Client;
import global.logging.Log;
import global.logging.LogLevel;
import global.logging.PerfLog;
import server.events.EventManager;
import server.events.StartMicroServiceEvent;
import server.modules.Server;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by pc on 24.01.2017.
 */
public abstract class StressTest {

    private static Server server;
    private static Client client;

    public static void main(String[] args) throws Exception {
        server = new Server();
        client = new Client();
        EventManager.getInstance().registerListener(server);
        Log.alterMinimumRequiredLevel(LogLevel.INFO);
        initNumberOfMicroServices(1);
        testFunctionality();
//        testThousandPerSixty();
//        testUnlimitedSpam();
    }

    public static void testFunctionality() throws Exception {
        Path bibFilePath = Paths.get("C:\\Users\\pc\\IdeaProjects\\bib2html-text\\bib2html-text\\test_files\\mybib.bib");
        client.getClientFileModel().addBibFile(bibFilePath.toFile());
        Path cslFilePath = Paths.get("C:\\Users\\pc\\IdeaProjects\\bib2html-text\\bib2html-text\\custom_default.csl");
        client.getClientFileModel().addCslFile(cslFilePath.toFile());
        Path templateFilePath = Paths.get("C:\\Users\\pc\\IdeaProjects\\bib2html-text\\bib2html-text\\custom_default_template.html");
        client.getClientFileModel().addTemplate(templateFilePath.toFile());
//        client.setOutputDirectory("C:\\Users\\pc\\Desktop\\SWP\\pipapo");
        client.sendClientRequest();
    }

    public static void testThousandPerSixty() throws Exception {
        Path bibFilePath = Paths.get("C:\\Users\\pc\\IdeaProjects\\bib2html-text\\bib2html-text\\test_files\\mybib2.bib");
        client.getClientFileModel().addBibFile(bibFilePath.toFile());
        Path cslFilePath = Paths.get("C:\\Users\\pc\\IdeaProjects\\bib2html-text\\bib2html-text\\custom_default.csl");
        client.getClientFileModel().addCslFile(cslFilePath.toFile());
        Path templateFilePath = Paths.get("C:\\Users\\pc\\IdeaProjects\\bib2html-text\\bib2html-text\\custom_default_template.html");
        client.getClientFileModel().addTemplate(templateFilePath.toFile());
//        client.setOutputDirectory("C:\\Users\\pc\\Desktop\\SWP\\pipapo");
        client.sendClientRequest();
    }

    public static void testUnlimitedSpam() throws Exception {
        Path bibFilePath = Paths.get("\\test_files\\rfc.bib");
        client.getClientFileModel().addBibFile(bibFilePath.toFile());
        Path cslFilePath = Paths.get("\\test_files\\harvard_cite_style.csl");
        client.getClientFileModel().addCslFile(cslFilePath.toFile());
        Path templateFilePath = Paths.get("\\custom_default_template.html");
        client.getClientFileModel().addTemplate(templateFilePath.toFile());
//        client.setOutputDirectory("C:\\Users\\pc\\Desktop\\SWP\\pipapo");
        client.sendClientRequest();
    }

    public static void initNumberOfMicroServices(int numberOfServices) throws Exception {
        for (int i = 0; i < numberOfServices; i++) {
            EventManager.getInstance().publishEvent(new StartMicroServiceEvent());
        }
    }

    private int getLastTimeTakenForClientID(String clientID){
        final String suffix = "-LastTimeTakenSeconds";
        int secondsTaken;
        try{
            secondsTaken = Integer.parseInt(PerfLog.obtainLogEntryForKey(clientID+suffix));
        }
        catch (NullPointerException e){
            return 0;
        }
        return secondsTaken;
    }

}
