package client.view;

import client.controller.Client;
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
        File bibFile1 = new File("/home/daan/Coding/Java/Client/test_data/mybib2.bib");
        File bibFile2 = new File("/home/daan/Coding/Java/Client/test_data/xampl.bib");
        Client testClient = new Client();
        Server testServer = new Server();
        testServer.run();
        testClient.getClientFileModel().addBibFile(bibFile1);
        testClient.getClientFileModel().addBibFile(bibFile2);
        testClient.sendClientRequest();
        System.out.println("request sent!");
        System.out.printf("end of main().");
    }
}
