package server.modules;

import server.events.Event;
import server.events.EventListener;
import server.events.ReceivedErrorEvent;
import server.events.ReceivedPartialResultEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Maximilian Schirm (denkbares GmbH)
 * @created 05.12.2016
 *
 * This is a Singleton class.
 */
public class PartialResultCollector implements EventListener {

    private static final PartialResultCollector INSTANCE = new PartialResultCollector();

    private PartialResultCollector(){}

    public PartialResultCollector getInstance(){
        return INSTANCE;
    }



    @Override
    public void notify(Event toNotify) {
        if(toNotify instanceof ReceivedPartialResultEvent){
            toNotify = (ReceivedPartialResultEvent)toNotify;

        }
        else if(toNotify instanceof ReceivedErrorEvent){
            toNotify = (ReceivedErrorEvent)toNotify;

        }
    }

    @Override
    public Set<Class<? extends Event>> getEvents() {
        Set<Class<? extends Event>> evts = new HashSet<>();
        evts.add(ReceivedErrorEvent.class);
        evts.add(ReceivedPartialResultEvent.class);
        return evts;
    }
}
