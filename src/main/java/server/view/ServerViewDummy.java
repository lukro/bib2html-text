package server.view;

import server.modules.Server;

/**
 * @author Maximilian Schirm (denkbares GmbH)
 * @created 05.12.2016
 *
 * A dummy for debugging. Will be replaced by a proper UI later on.
 * TODO deleteme
 */

public class ServerViewDummy {

    public static void main(String[] args){
        try{
            log("Starting server...");
            Server server = new Server();
            log("Started server @ " + server.getHostIP());
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void log(String msg){
        System.out.println(msg);
    }
}
