package server.view;

import server.Server;

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
            Server server = new Server();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
