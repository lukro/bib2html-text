package server.modules;

import microservice.MicroService;
import java.util.HashSet;
import java.util.Objects;

/**
 * @author Maximilian Schirm (denkbares GmbH)
 * @created 05.12.2016
 *
 * Singleton class with greedy init
 * Creates and stops MicroServices for a Server
 */

public class MicroServiceManager {

    private static MicroServiceManager INSTANCE;
    private HashSet<MicroService> microServices;

    private MicroServiceManager(){}

    public static MicroServiceManager getInstance(){
        if(INSTANCE == null)
            INSTANCE = new MicroServiceManager();
        return INSTANCE;
    }

    public boolean startMicroService(){
        //TODO : Fill...
        return true;
    }

    public boolean stopMicroService(String routingKey){
        Objects.requireNonNull(routingKey);
        //TODO : Fill...
        return true;
    }

    public String getFreeMicroServiceKey(){
        //TODO : Fill...
        return "banana";
    }

}
