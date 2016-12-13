package server.events;

import microservice.MicroService;

/**
 * @author Maximilian Schirm
 * @created 10.12.2016
 */

public class MicroserviceDisconnectionRequestEvent implements Event{

    private final String toDisconnectID;

    public MicroserviceDisconnectionRequestEvent(String toDisconnectID) {
        this.toDisconnectID = toDisconnectID;
    }

    public String getToDisconnectID() {
        return toDisconnectID;
    }
}
