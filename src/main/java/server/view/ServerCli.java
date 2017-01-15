package server.view;

import global.logging.Log;
import global.logging.LogLevel;
import server.modules.Server;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Scanner;

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
            Scanner consoleScanner = new Scanner(System.in);
            Server server = new Server();
            Log.log("Started server @ " + server.getHostIP());
            Log.log("Please enter a command... (type \"help\" for help)");

            //Set the log level
            if(args.length == 0)
                Log.alterMinimumRequiredLevel(LogLevel.INFO);
            else
                Log.alterMinimumRequiredLevel(LogLevel.valueOf(args[0]));

            Log.log("Set log level to " + Log.getMinimumRequiredLevel());
            String command = "";
            while(consoleScanner.hasNextLine()){
                command = consoleScanner.nextLine();
                switch (command){
                    case "show ip":
                        try {
                            Log.log("Your IP adress is : " + Inet4Address.getLocalHost().getHostAddress());
                        } catch (UnknownHostException e) {
                            Log.log("Failed to display Host IP", e);
                        }
                        break;
                    case "list requests":
                        server.getPartialResultCollector().getOutstandingRequests().forEach(status -> Log.log(status));
                        break;
                    case "list services":
                        server.getMicroServiceManager().getMicroServices().forEach(service -> Log.log(service));
                        break;
                    case "help":
                        printHelp();
                        break;
                    case "exit":
                        Log.log("Hard shutdown.", LogLevel.SEVERE);
                        System.exit(0);
                        break;
                    default:
                        Log.log("Command not recognized.");
                        break;
                }
            }

        }
        catch (Exception e){
            Log.log("",e);
        }
    }

    private static void printHelp() {
        String helpDoc = "___BEGIN HELP___\n"
                +"bibtex2html-text Server\n"
                +"_____\n"
                +"Commands :\n"
                +"*show ip : Used for displaying the IP of the Server instance.\n"
                +"*list requests : Lists the status of all registered requests\n"
                +"*list services : Lists the status of all registered services\n"
                +"*help : Show this help (duh.)\n"
                +"*exit : Hard Shutdown of the Server.\n"
                +"___END HELP___";
        Log.log(helpDoc);
    }
}
