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
    private static final Path WORKING_DIRECTORY = Paths.get(System.getProperty("user.dir"));

    public static void main(String[] args) throws Exception {
        server = new Server();
        client = new Client();
//        EventManager.getInstance().registerListener(server);
        Log.alterMinimumRequiredLevel(LogLevel.WARNING);
        initNumberOfMicroServices(1);
        testFunctionality();
//        testThousandPerSixty();
//        testBigRequest(100);
//        testSuperSpam();
    }

    public static void testFunctionality() throws Exception {
        Path bibFilePath = Paths.get(WORKING_DIRECTORY.toAbsolutePath().toString(), "test_files/mybib.bib");
        client.getClientFileModel().addBibFile(bibFilePath.toFile());
//        Path cslFilePath = Paths.get(WORKING_DIRECTORY.toAbsolutePath().toString(), "custom_default.csl");
//        client.getClientFileModel().addCslFile(cslFilePath.toFile());
//        Path templateFilePath = Paths.get(WORKING_DIRECTORY.toAbsolutePath().toString(), "custom_default_template.html");
//        client.getClientFileModel().addTemplate(templateFilePath.toFile());

        //TODO: outputDirectory anpassen!
        client.setOutputDirectory("C:\\Users\\pc\\Desktop\\SWP\\pipapo");
        client.sendClientRequest();
    }

    public static void testThousandPerSixty() throws Exception {
        Path bibFilePath = Paths.get(WORKING_DIRECTORY.toAbsolutePath().toString(), "test_files/thousand.bib");
        client.getClientFileModel().addBibFile(bibFilePath.toFile());
//        Path cslFilePath = Paths.get(WORKING_DIRECTORY.toAbsolutePath().toString(), "custom_default.csl");
//        client.getClientFileModel().addCslFile(cslFilePath.toFile());
//        Path templateFilePath = Paths.get(WORKING_DIRECTORY.toAbsolutePath().toString(), "custom_default_template.html");
//        client.getClientFileModel().addTemplate(templateFilePath.toFile());

        //TODO: outputDirectory anpassen!
        client.setOutputDirectory("C:\\Users\\pc\\Desktop\\SWP\\pipapo");
        client.sendClientRequest();
    }

    public static void testBigRequest(int entriesTimesThousand) throws Exception {
        for (int i = 0; i < entriesTimesThousand; i++) {
            Path bibFilePath = Paths.get(WORKING_DIRECTORY.toAbsolutePath().toString(), "test_files/thousand.bib");
            client.getClientFileModel().addBibFile(bibFilePath.toFile());
        }
//        Path cslFilePath = Paths.get(WORKING_DIRECTORY.toAbsolutePath().toString(), "custom_default.csl");
//        client.getClientFileModel().addCslFile(cslFilePath.toFile());
//        Path templateFilePath = Paths.get(WORKING_DIRECTORY.toAbsolutePath().toString(), "custom_default_template.html");
//        client.getClientFileModel().addTemplate(templateFilePath.toFile());

        //TODO: outputDirectory anpassen!
        client.setOutputDirectory("C:\\Users\\pc\\Desktop\\SWP\\pipapo");
        client.sendClientRequest();
    }

    public static void testSuperSpam() throws Exception {
        Path bibFilePath = Paths.get(WORKING_DIRECTORY.toAbsolutePath().toString(), "test_files/mybib.bib");
        while (true) {
            Client c = new Client();
            c.getClientFileModel().addBibFile(bibFilePath.toFile());
//        Path cslFilePath = Paths.get(WORKING_DIRECTORY.toAbsolutePath().toString(), "custom_default.csl");
//        client.getClientFileModel().addCslFile(cslFilePath.toFile());
//        Path templateFilePath = Paths.get(WORKING_DIRECTORY.toAbsolutePath().toString(), "custom_default_template.html");
//        client.getClientFileModel().addTemplate(templateFilePath.toFile());
            //TODO: outputDirectory anpassen!
            c.setOutputDirectory("C:\\Users\\pc\\Desktop\\SWP\\pipapo");
            c.sendClientRequest();
        }
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
