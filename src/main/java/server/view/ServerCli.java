package server.view;

import global.logging.Log;
import global.logging.LogLevel;
import server.modules.Server;

/**
 * @author Maximilian Schirm
 * created 05.12.2016
 *
 * A dumb CLI instance of the server. TODO Should be expanded by Console I/O to relay commands to Controller.
 */
public class ServerCli {

    public static void main(String[] args){
        try{
            Log.log("Starting server...");
            Server server = new Server();
            Log.log("Started server @ " + server.getHostIP());
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

}
