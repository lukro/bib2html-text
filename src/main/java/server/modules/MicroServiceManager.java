package server.modules;

import com.rabbitmq.client.Channel;
import global.logging.Log;
import global.logging.LogLevel;
import microservice.MicroService;

import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

/**
 * @author Maximilian Schirm
 *         created 05.12.2016
 *         <p>
 *         Singleton class with greedy init
 *         Creates and stops MicroServices for a Server
 */

public class MicroServiceManager {

    private static MicroServiceManager INSTANCE;
    private HashSet<MicroService> microServices;
    private Channel channel;


    private MicroServiceManager() {
    }

    public static MicroServiceManager getInstance() throws IOException, TimeoutException {
        if (INSTANCE == null)
            INSTANCE = new MicroServiceManager();
        return INSTANCE;
    }

    public boolean startMicroService() {
        try {
            Log.log("starting microservice on server", LogLevel.LOW);
            MicroService ms = new MicroService();
            microServices.add(ms);
            ms.run();
            Log.log("successfully started microservice", LogLevel.LOW);
        } catch (IOException | TimeoutException e) {
            Log.log("failed to start microservice", e);
            return false;
        }
        return true;
    }

    public boolean stopMicroService(String routingKey) {
        Objects.requireNonNull(routingKey);
        //TODO : Fill...
        return true;
    }

    public String getFreeMicroServiceKey() {

        //TODO : How to check queue sizes?...

        return "banana";
    }

    public void disconnectMicroservice(MicroService toDisconnect) {

        if (!microServices.contains(toDisconnect)) {
            throw new IllegalArgumentException("MicroService does not exist");
        }
        try {
            toDisconnect.closeConnection();
            Log.log("microservice successfully disconnected");
        } catch (IOException | TimeoutException e) {
            Log.log("failed to disconnect microservice", e);
        }
        //TODO : Fill... And throw new MicroServiceDisconnectedEvent on Success.
    }
}
