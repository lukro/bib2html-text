package server.modules;

import com.rabbitmq.client.Channel;
import global.logging.Log;
import global.logging.LogLevel;
import server.events.*;

import java.io.*;
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

    private static final int MAXIMUM_AUTOMATIC_SERVICES = 4;
    private static MicroServiceManager INSTANCE;
    //Decides whether to use utilisation checking.
    private static boolean USE_LOAD_BALANCING = false;
    //The max. # of tasks per service.
    private final static int UTIL_START_DELAY = 1500;
    private final static int UTIL_FREQ = 3000;
    public final static int MAXIMUM_UTILIZATION = 250;

    //Key : ID | Value : IP
    private HashMap<String, String> microServices = new HashMap<>();
    private int runningServicesCount;
    private final Channel channel;
    private final String TASK_QUEUE_NAME;
    static {
        //TODO : eventually add automatic extraction of microservice.jar from working dir.
        //copyJarToWorkingDir();
    }

    private MicroServiceManager(Channel channel, String taskQueueName) {
        this.runningServicesCount = 0;
        this.channel = channel;
        this.TASK_QUEUE_NAME = taskQueueName;
        EventManager.getInstance().registerListener(this);

        TimerTask utilizationCheckerTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    if(USE_LOAD_BALANCING) {
                        checkUtilization();
                        Log.log("Utilization Checker Task did another round...", LogLevel.LOW);
                    }
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
        final String MICROSERVICE_JAR_FILE_NAME = "microservice.jar";

        //Check whether service exists.
        File serviceJarFile = new File(MICROSERVICE_JAR_FILE_NAME);
        if(!serviceJarFile.exists()){
            Log.log("Cannot launch a new MicroService : " + MICROSERVICE_JAR_FILE_NAME + " is missing in current working directory.", LogLevel.SEVERE);
            return;
        }

        //Start a new Service in seperate runtime.
        try {
            String[] cmd = { "java.exe", "-jar", MICROSERVICE_JAR_FILE_NAME};
            ProcessBuilder processBuilder = new ProcessBuilder(cmd);
            Process process = processBuilder.start();
        } catch (IOException e) {
            Log.log("Could not start MicroService",e);
        }
    }

    /**
     * Checks the utilization of the system.
     * This method will start new microservices, until the number of tasks divided by the number of running services
     * is below MAXIMUM_UTILIZATION (test-value!!)
     *
     * @return number of added services. 0, if no new services where added.
     */
    private void checkUtilization() throws IOException {
        final int currTasks = channel.queueDeclarePassive(TASK_QUEUE_NAME).getMessageCount();
        Log.log("currentAmountOfTasks: " + currTasks, LogLevel.LOW);
        Log.log("currently running services: " + runningServicesCount, LogLevel.LOW);

        //To avoid dividing by 0
        if (runningServicesCount == 0) {
            startMicroService();
            runningServicesCount++;
        }

        if (currTasks / runningServicesCount > MAXIMUM_UTILIZATION && !(runningServicesCount > MAXIMUM_AUTOMATIC_SERVICES)) {
            runningServicesCount++;
            startMicroService();
        }
    }

    public Collection<String> getMicroServices() {
        return microServices.keySet().stream().map(serviceID -> serviceID + " : " + microServices.get(serviceID)).collect(Collectors.toList());
    }

    @Override
    public void notify(IEvent toNotify) {
        if (toNotify instanceof MicroServiceConnectedEvent) {
            String connectedServiceID = ((MicroServiceConnectedEvent) toNotify).getConnectedSvcID();
            String connectedServiceIP = ((MicroServiceConnectedEvent) toNotify).getConnectedSvcIP();
            microServices.put(connectedServiceID, connectedServiceIP);
        } else if (toNotify instanceof MicroServiceDisconnectedEvent) {
            String disconnectedServiceID = ((MicroServiceDisconnectedEvent) toNotify).getDisconnectedSvcID();
            microServices.remove(disconnectedServiceID);
        } else if (toNotify instanceof StartMicroServiceEvent) {
            startMicroService();
        } else if (toNotify instanceof SwitchUtilisationCheckingEvent) {
            USE_LOAD_BALANCING = ((SwitchUtilisationCheckingEvent) toNotify).isUseChecking();
        }
    }

    @Override
    public Set<Class<? extends IEvent>> getEvents() {
        return new HashSet<>(Arrays.asList(MicroServiceConnectedEvent.class, MicroServiceDisconnectedEvent.class, StartMicroServiceEvent.class, SwitchUtilisationCheckingEvent.class));
    }

    public static void copyJarToWorkingDir() {
        InputStream stream = null;
        OutputStream resStreamOut = null;
        String jarFolder;
        String resourceName = "/microservice.jar";

        try {
            stream = MicroServiceManager.class.getResourceAsStream(resourceName);
            if(stream == null)
                throw new Exception("Cannot get resource \"" + resourceName + "\" from Jar file.");

            int readBytes;
            byte[] buffer = new byte[4096];
            jarFolder = new File(MicroServiceManager.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getPath().replace('\\', '/');
            resStreamOut = new FileOutputStream(jarFolder + resourceName);
            while ((readBytes = stream.read(buffer)) > 0) {
                resStreamOut.write(buffer, 0, readBytes);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}