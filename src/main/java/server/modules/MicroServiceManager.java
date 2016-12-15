package server.modules;

import com.rabbitmq.client.Channel;
import global.logging.Log;
import global.logging.LogLevel;
import microservice.MicroService;
import server.events.EventManager;
import server.events.MicroServiceConnectedEvent;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
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
    //The max. # of tasks per service.
    private final int MAXIMUM_UTILIZATION = 50;
    //Key : ID | Value : IP
    private HashMap<String, String> microServices = new HashMap<>();

    private final Channel channel;
    private final String TASK_QUEUE_NAME;

    private MicroServiceManager(Channel channel, String taskQueueName) {
        this.channel = channel;
        this.TASK_QUEUE_NAME = taskQueueName;

        TimerTask utilizationCheckerTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    checkUtilization();
                    Log.log("Utilization Checker Task did another round...", LogLevel.LOW);
                } catch (IOException e) {
                    Log.log("Utilization Checker ran into Problems", e);
                }
            }
        };
        Timer timer = new Timer();
        timer.schedule(utilizationCheckerTask, 1500, 1000);
    }


    /**
     * Has to be called for instance to not be null.
     */
    protected static MicroServiceManager initialize(Channel channel, String taskQueueName) {
        if (INSTANCE == null)
            INSTANCE = new MicroServiceManager(channel, taskQueueName);
        return INSTANCE;
    }

    /**
     * Returns the INSTANCE of the MicroServiceManager
     */
    protected static MicroServiceManager getInstance() {
        if (INSTANCE == null)
            Log.log("MicroServiceManager was not initialized properly", new InstantiationException("initialize() was not called before getInstance()"));
        return INSTANCE;
    }

    /**
     * Starts a new MicroService and returns the key of the newly created Service.
     * The Service will be started on the local machine (ie. the server).
     *
     * @return Key of a new Service.
     */
    private String startMicroService() {
        //TODO : Fill... Add Port to newService.getHostIP()
        try {
            Log.log("Starting microservice on server");
            MicroService newService = new MicroService();
            microServices.put(newService.getID(), newService.getHostIP());
            newService.run();
            EventManager.getInstance().publishEvent(new MicroServiceConnectedEvent(newService.getID()));
            Log.log("Successfully started microservice");
            return newService.getID();
        } catch (IOException | TimeoutException e) {
            Log.log("Failed to create a new MicroService, returned null", e);
            return null;
        }
    }

    /**
     * Disconnects and then stops the MicroService.
     *
     * @param microserviceID The ID of the Service to stop.
     * @return Whether the stopping was successful.
     */
    protected boolean stopMicroService(String microserviceID) {
        Objects.requireNonNull(microserviceID);

        return disconnectMicroService(microserviceID);
    }

    /**
     * Disconnects the MicroService with the ID.
     * A disconnection means that the remaining Tasks for that MicroService will be redistributed.
     * <p>
     * DO NOT Confuse this Method with stopMicroService(), as it will not issue a STOP Command to that Service
     * USE THIS Method for disconnecting from Services before stopping them or when communicating with externals.
     *
     * @param idToRemove The ID of the MicroService to disconnect.
     * @return A boolean whether the disconnection was successful.
     */
    private boolean disconnectMicroService(String idToRemove) {
        Log.log("Disconnecting MicroService " + idToRemove + "...");

        try {
            channel.basicCancel(idToRemove);
            String oldValue = microServices.get(idToRemove);
            microServices.remove(idToRemove, oldValue);
            Log.log("Successfully disconnected service " + idToRemove, LogLevel.WARNING);
            return true;
        } catch (IOException e) {
            Log.log("Failed to send cancel request to service " + idToRemove, e);
            return false;
        }
    }

    /**
     * Checks the utilization of the system.
     * This method will start new microservices, until the number of tasks divided by the number of running services
     * is below MAXIMUM_UTILIZATION (test-value!!)
     *
     * @return number of added services. 0, if no new services where added.
     */
    private int checkUtilization() throws IOException {

        int currTasks = channel.queueDeclarePassive(TASK_QUEUE_NAME).getMessageCount();
        int returnValue = 0;

        //To avoid dividing by 0
        if (microServices.isEmpty())
            startMicroService();

        while (currTasks / microServices.size() > MAXIMUM_UTILIZATION) {
            startMicroService();
            returnValue++;
        }

        return returnValue;
    }
}