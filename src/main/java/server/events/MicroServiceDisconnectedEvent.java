package server.events;

import microservice.MicroService;

/**
 * @author Maximilian Schirm
 *         created 09.12.2016
 */
public class MicroServiceDisconnectedEvent implements IEvent {

    private final MicroService disconnectedSvc;

    public MicroServiceDisconnectedEvent(MicroService disconnectedSvc) {
        this.disconnectedSvc = disconnectedSvc;
    }

    public MicroService getDisconnectedSvc() {
        return disconnectedSvc;
    }
}