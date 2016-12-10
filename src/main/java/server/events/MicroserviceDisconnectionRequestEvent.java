package server.events;

import microservice.MicroService;

/**
 * @author Maximilian Schirm
 * @created 10.12.2016
 */

public class MicroserviceDisconnectionRequestEvent implements Event{

    private final MicroService toDisconnect;

    public MicroserviceDisconnectionRequestEvent(MicroService toDisconnect) {
        this.toDisconnect = toDisconnect;
    }

    public MicroService getToDisconnect() {
        return toDisconnect;
    }
}
