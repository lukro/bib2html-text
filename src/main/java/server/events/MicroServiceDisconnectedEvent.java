package server.events;

import microservice.MicroService;

import java.util.Objects;

/**
 * @author Maximilian Schirm (denkbares GmbH)
 * @created 09.12.2016
 */

public class MicroServiceDisconnectedEvent implements Event {

    private final MicroService disconnectedSvc;

    public MicroServiceDisconnectedEvent(MicroService disconnectedSvc) {
        Objects.requireNonNull(disconnectedSvc);
        this.disconnectedSvc = disconnectedSvc;
    }

    public MicroService getDisconnectedSvc() {
        return disconnectedSvc;
    }
}