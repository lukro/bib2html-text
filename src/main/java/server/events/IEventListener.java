package server.events;

import java.util.Set;

public interface IEventListener {

 	void notify(IEvent toNotify);

 	Set<Class<? extends IEvent>> getEvents();

}