package client.view;

import client.controller.Client;
import client.controller.ClientFileHandler;
import global.model.ClientRequest;
import org.jbibtex.*;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by daan on 11/30/16.
 */
public class Gui {

    public static void main(String[] args) throws IOException, TimeoutException, ParseException {
//        Entry entry1 = Entry.buildEntry("1", "content", new ArrayList<>(), new ArrayList<>());
//        Entry entry2 = Entry.buildEntry("1", "content", new ArrayList<>(), new ArrayList<>());
//        entry2 = entry1;
//        System.out.println("equals: " + entry1.equals(entry2));

//        String serverIPv6 = "2a02:810d:9180:3e05:1542:55db:14c0:bd0c";
//        String serverIPv4 = "192.168.0.3";
//        String serverRoutingKey = "SERVER";
//
//
//        Client serverClient = new Client(serverIPv6);
//
//        Client senderClient = new Client("localhost");
//        Client receiverClient = new Client("localhost");
//
//        receiverClient.run();

//        senderClient.sendClientRequest(receiverClient.getRoutingKey(), "routingTest");

        File bibFile1 = new File("/home/daan/Coding/Java/Client/test_data/mybib2.bib");
        File bibFile2 = new File("/home/daan/Coding/Java/Client/test_data/xampl.bib");
//        ClientFileHandler.readStringFromFile(null);

        System.out.println("isvalid(): " + ClientFileHandler.isValidBibFile(bibFile1));
        System.out.println("isvalid(): " + ClientFileHandler.isValidBibFile(bibFile2));


        Client testClient = new Client();
        testClient.getClientFileModel().addBibFile(bibFile1);
        testClient.getClientFileModel().addBibFile(bibFile2);
        ClientRequest clientRequest = testClient.createClientRequest();

        System.out.println(clientRequest.toString());
        System.out.println(testClient.getClientFileModel().toString());

//        for (Entry currentEntry : clientRequest.getEntries()) {
//            System.out.println(currentEntry.toString());
//        }
//        ArrayList<Entry> testList = testClient.getBibTeXEntryFormatter().createBibTeXEntryObjectListFromClientFileModel(testClient.getClientFileModel());
//        for (int i = 0; i < testList.size(); i++) {
//            System.out.println("BEGIN " + (i + 1) + ". Entry: ");
//            System.out.println("clientID: " + testList.get(i).getClientID());
//            System.out.println("content: " + testList.get(i).getContent());
//            System.out.println("cslFiles.size: " + testList.get(i).getCslFiles().size());
//            System.out.println("templateFiles.size: " + testList.get(i).getTemplateFiles().size());
//            System.out.println("END " + (i + 1) + ". Entry: ");
//        }


//        System.out.println("output: " + writer.toString());

//        System.out.println("valid: " + ClientFileHandler.getBibTeXDatabaseObjectFromFile(testFile));

//        serverClient.sendClientRequest("queue0", "penis!");
//        System.out.println("end");
    }
}
