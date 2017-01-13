package server.modules;

import com.rabbitmq.client.Channel;
import global.identifiers.QueueNames;
import global.logging.Log;
import global.logging.LogLevel;
import global.model.DefaultStopOrder;
import global.model.IStopOrder;
import microservice.MicroService;
import org.apache.commons.lang3.SerializationUtils;
import server.events.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Maximilian Schirm
 *         created 05.12.2016
 *         <p>
 *         Singleton class with greedy init
 *         Creates and stops MicroServices for a Server
 */
public class MicroServiceManager implements IEventListener {

    private static MicroServiceManager INSTANCE;
    //The max. # of tasks per service.
    public final static int MAXIMUM_UTILIZATION = 25;
    private final static int UTIL_START_DELAY = 1500;
    private final static int UTIL_FREQ = 500;
    //Key : ID | Value : IP
    private HashMap<String, String> microServices = new HashMap<>();

    private final Channel channel;
    private final String TASK_QUEUE_NAME;
    private final String STOP_QUEUE_NAME = QueueNames.MICROSERVICE_STOP_QUEUE_NAME.toString();

    private MicroServiceManager(Channel channel, String taskQueueName) {
        this.channel = channel;
        this.TASK_QUEUE_NAME = taskQueueName;
        EventManager.getInstance().registerListener(this);

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
        timer.schedule(utilizationCheckerTask, UTIL_START_DELAY, UTIL_FREQ);
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
    private void startMicroService() {
        Log.log("Starting microservice on server", LogLevel.LOW);
        MicroService.main("localhost");
    }

    /**
     * Checks the utilization of the system.
     * This method will start new microservices, until the number of tasks divided by the number of running services
     * is below MAXIMUM_UTILIZATION (test-value!!)
     *
     * @return number of added services. 0, if no new services where added.
     */
    private void checkUtilization() throws IOException {
        int currTasks = channel.queueDeclarePassive(TASK_QUEUE_NAME).getMessageCount();
        Log.log("currentAmountOfTasks: " + currTasks, LogLevel.LOW);
        int runningServicesCount = microServices.size();

        //To avoid dividing by 0
        if (runningServicesCount == 0) {
            startMicroService();
            runningServicesCount++;
        }

        if (currTasks / runningServicesCount > MAXIMUM_UTILIZATION)
            startMicroService();
    }

    public Collection<String> getMicroservices() {
        return microServices.keySet().stream().map(serviceID -> serviceID + " : " + microServices.get(serviceID)).collect(Collectors.toList());
    }

    @Override
    public void notify(IEvent toNotify) {
        if(toNotify instanceof MicroServiceConnectedEvent){
            String connectedServiceID = ((MicroServiceConnectedEvent) toNotify).getConnectedSvcID();
            String connectedServiceIP = ((MicroServiceConnectedEvent) toNotify).getConnectedSvcIP();
            microServices.put(connectedServiceID, connectedServiceIP);
        } else if(toNotify instanceof MicroServiceDisconnectedEvent){
            String disconnectedServiceID = ((MicroServiceDisconnectedEvent) toNotify).getDisconnectedSvcID();
            microServices.remove(disconnectedServiceID);
        }
    }

    @Override
    public Set<Class<? extends IEvent>> getEvents() {
        return new HashSet<>(Arrays.asList(MicroServiceConnectedEvent.class, MicroServiceDisconnectedEvent.class));
    }
}