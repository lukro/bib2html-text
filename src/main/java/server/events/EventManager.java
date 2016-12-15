package server.events;

import global.logging.Log;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class EventManager {

    private final static EventManager INSTANCE = new EventManager();
    private ConcurrentMap<Class<? extends IEvent>, Set<IEventListener>> eventMap = new ConcurrentHashMap<>();

    private EventManager() {
    }

    public static EventManager getInstance() {
        return INSTANCE;
    }

    public void registerListener(IEventListener toRegister) {
        toRegister.getEvents().forEach(event -> {
            Set<IEventListener> entryForEvent = eventMap.get(event);
            if (entryForEvent == null) {
                Set<IEventListener> newEventSet = new HashSet<>();
                newEventSet.add(toRegister);
                eventMap.put(event, newEventSet);
            } else {
                entryForEvent.add(toRegister);
                eventMap.put(event, entryForEvent);
            }
        });
    }

    public void publishEvent(IEvent toPublish) {
        try {
            eventMap.get(toPublish.getClass()).forEach(listener -> {
                try {
                    listener.notify(toPublish);
                } catch (NullPointerException e) {
                    //TODO: Remove invalid instance?
                    Log.log("Couldn't publish the event to the listener : Listener was null", e);
                }
            });
        } catch (NullPointerException e) {
            Log.log("No classes registered for the event " + toPublish.getClass(), e);
        }
    }

}