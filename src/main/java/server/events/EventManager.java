package server.events;

import global.logging.Log;

import java.util.*;

public class EventManager{

	private final static EventManager INSTANCE = new EventManager();
	private Map<Class<? extends Event>, Set<EventListener>> eventMap;

	private EventManager(){
		eventMap = new HashMap<>();
	}

	public static EventManager getInstance(){
		return INSTANCE;
	}

	public void registerListener(EventListener toRegister){
		toRegister.getEvents().forEach(event -> {
			Set<EventListener> entryForEvent = eventMap.get(event);
			if (entryForEvent == null){
				Set<EventListener> newEventSet = new HashSet<>();
				newEventSet.add(toRegister);
				eventMap.put(event, newEventSet);
			}
			else {
				entryForEvent.add(toRegister);
				eventMap.put(event, entryForEvent);
			}
		});
	}

	public void publishEvent(Event toPublish){
		eventMap.get(toPublish.getClass()).forEach(listener -> {
			try {
				listener.notify(toPublish);
			} catch (NullPointerException e) {
				//TODO: Remove invalid instance?
				Log.log("Couldn't publish the event to the listener : Listener was null",e);
			}
		});
	}

}