package server;

import client.controller.ConnectionPoint;
import client.model.Entry;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import org.apache.commons.io.IOUtils;
import server.events.Event;
import server.events.EventListener;
import server.events.EventManager;
import server.events.FinishedCollectingResultEvent;
import server.model.Result;
import server.modules.MicroServiceManager;
import server.modules.PartialResultCollector;

import java.io.ByteArrayOutputStream;
import java.io.IOException;;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeoutException;

/**
 * @author Maximilian Schirm (denkbares GmbH)
 * @created 05.12.2016
 */

public class Server extends ConnectionPoint implements EventListener, Runnable, Consumer{

    private static final String microServicePubQueueName = "MS_PUB_QUEUE";
    private static final String microServiceSubQueueName = "MS_SUB_QUEUE";

    private ArrayList<String> invalidClientIDs = new ArrayList<>();

    private final URI adress;
    private final MicroServiceManager microServiceManager;
    private final PartialResultCollector partialResultCollector;

    public Server() throws IOException, TimeoutException {
        super();

        adress = URI.create(getHostIP());
        microServiceManager = MicroServiceManager.getInstance();
        partialResultCollector = PartialResultCollector.getInstance();

        EventManager.getInstance().registerListener(this);
    }

    public boolean sendEntryToMicroServices(Entry entry){
        try {
            //TODO : Implement properly after checking out how the rabbitmq publish works
            String microServiceKey = MicroServiceManager.getInstance().getFreeMicroServiceKey();

            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }


    }

    @Override
    public void run() {

    }

    @Override
    public void notify(Event toNotify) {
        if(toNotify instanceof FinishedCollectingResultEvent){
            Result eventResult = ((FinishedCollectingResultEvent) toNotify).getResult();
            String clientID = eventResult.getIdentifier().getClientID();
            //TODO : Publish to client
        }
    }

    @Override
    public Set<Class<? extends Event>> getEvents() {
        Set<Class<? extends Event>> evts = new HashSet<>();
        evts.add(FinishedCollectingResultEvent.class);
        return evts;
    }

    @Override
    public void handleConsumeOk(String s) {

    }

    @Override
    public void handleCancelOk(String s) {

    }

    @Override
    public void handleCancel(String s) throws IOException {

    }

    @Override
    public void handleShutdownSignal(String s, ShutdownSignalException e) {

    }

    @Override
    public void handleRecoverOk(String s) {

    }

    @Override
    public void handleDelivery(String s, Envelope envelope, AMQP.BasicProperties basicProperties, byte[] bytes) throws IOException {

    }
}
