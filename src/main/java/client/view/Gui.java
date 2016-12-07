package client.view;

import client.controller.Client;
import microservice.MicroService;
import org.jbibtex.*;
import server.modules.Server;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by daan on 11/30/16.
 */
public class Gui {

    public static void main(String[] args) throws IOException, TimeoutException, ParseException {
//        String serverIPv4_2 = "192.168.137.171";
        //TODO: change path to test_files
        File bibFile1 = new File("C:\\Users\\pc\\IdeaProjects\\bib2html-text\\bib2html-text\\test_files\\xampl.bib");
        File bibFile2 = new File("C:\\Users\\pc\\IdeaProjects\\bib2html-text\\bib2html-text\\test_files\\mybib2.bib");
        Client testClient = new Client();
        Server testServer = new Server();
        MicroService testService = new MicroService("msPubQueueName", "regQueue");
        testServer.run();
        testService.run();
        testClient.getClientFileModel().addBibFile(bibFile1);
        testClient.getClientFileModel().addBibFile(bibFile2);
        testClient.sendClientRequest();
        System.out.println("end of main().");
    }
}
