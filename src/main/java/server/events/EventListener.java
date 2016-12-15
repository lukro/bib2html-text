package server.events;

import java.util.Set;

public interface EventListener{

 	void notify(Event toNotify);

 	Set<Class<? extends Event>> getEvents();

}