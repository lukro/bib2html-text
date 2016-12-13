package server.modules;

import global.logging.Log;
import global.logging.LogLevel;
import global.model.DefaultResult;
import global.model.IPartialResult;
import server.events.*;
import server.events.EventListener;
import global.model.DefaultPartialResult;

import java.util.*;

/**
 * @author Maximilian Schirm
 *         created 05.12.2016
 *         <p>
 *         This is a Singleton class with immediate instantiation
 */
public class PartialResultCollector implements EventListener {

    private static final PartialResultCollector INSTANCE = new PartialResultCollector();
    private HashMap<String, Collection<IPartialResult>> mappingClientIDtoFinishedPartialResults;
    private HashMap<String, Integer> mappingClientIDtoExpectedResultsSize;

    private PartialResultCollector() {
        EventManager.getInstance().registerListener(this);
        mappingClientIDtoExpectedResultsSize = new HashMap<>();
        mappingClientIDtoFinishedPartialResults = new HashMap<>();

        //Starts the update loop
        TimerTask updateLoop = new TimerTask() {
            @Override
            public void run() {
                update();
                Log.log("Partial Result Collector - Update task did another run.", LogLevel.LOW);
            }
        };
        Timer timer = new Timer();
        timer.schedule(updateLoop, 0, 1000);
    }

    public static PartialResultCollector getInstance() {
        return INSTANCE;
    }

    @Override
    public void notify(Event toNotify) {
        if (toNotify instanceof ReceivedPartialResultEvent) {
            ReceivedPartialResultEvent tempEvent = (ReceivedPartialResultEvent) toNotify;
            IPartialResult partialResult = tempEvent.getPartialResult();
            String id = partialResult.getIdentifier().getClientID();
            Collection<IPartialResult> presentResults = mappingClientIDtoFinishedPartialResults.get(id);
            presentResults.add(partialResult);
            mappingClientIDtoFinishedPartialResults.put(id, presentResults);

        } else if (toNotify instanceof ReceivedErrorEvent) {
            ReceivedErrorEvent tempEvent = (ReceivedErrorEvent) toNotify;
            String id = tempEvent.getResultID();
            int currentSize = mappingClientIDtoExpectedResultsSize.get(id);
            mappingClientIDtoExpectedResultsSize.put(id, currentSize - 1);
        } else if (toNotify instanceof RequestAcceptedEvent) {
            String id = ((RequestAcceptedEvent) toNotify).getRequestID();
            int size = ((RequestAcceptedEvent) toNotify).getReqSize();
            mappingClientIDtoExpectedResultsSize.put(id, size);
            mappingClientIDtoFinishedPartialResults.remove(id);
        }
    }

    /**
     * Checks whether any of the results is finished creating yet and - if that is the case - sends out a FinishedCollectingResultEvent.
     */
    private synchronized void update() {
        synchronized (mappingClientIDtoExpectedResultsSize) {
            mappingClientIDtoExpectedResultsSize.forEach((key, size) -> {
                Collection<IPartialResult> parts = mappingClientIDtoFinishedPartialResults.get(key);
                if (parts != null)
                    if (parts.size() == size)
                        EventManager.getInstance().publishEvent(new FinishedCollectingResultEvent(DefaultResult.buildResultfromPartials(parts)));
            });
        }
    }

    @Override
    public Set<Class<? extends Event>> getEvents() {
        Set<Class<? extends Event>> evts = new HashSet<>();
        evts.add(ReceivedErrorEvent.class);
        evts.add(ReceivedPartialResultEvent.class);
        evts.add(RequestAcceptedEvent.class);
        return evts;
    }

}
