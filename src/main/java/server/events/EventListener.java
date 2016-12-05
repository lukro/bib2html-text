package server.events;

import java.util.Set;

public interface EventListener{

 	public void notify(Event toNotify);

 	public Set<Class<? extends Event>> getEvents();

}