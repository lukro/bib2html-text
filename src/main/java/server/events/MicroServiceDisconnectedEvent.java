package server.events;

import microservice.MicroService;

/**
 * @author Maximilian Schirm
 *         created 09.12.2016
 */
public class MicroServiceDisconnectedEvent implements IEvent {

    private final String disconnectedSvcID;

    public MicroServiceDisconnectedEvent(String disconnectedSvcID) {
        this.disconnectedSvcID = disconnectedSvcID;
    }

    public String getDisconnectedSvcID(){
        return disconnectedSvcID;
    }
}