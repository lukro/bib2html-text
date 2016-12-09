package server.events;

import microservice.MicroService;

import java.util.Objects;

/**
 * @author Maximilian Schirm (denkbares GmbH)
 * @created 09.12.2016
 */

public class MicroServiceConnectedEvent implements Event {

    private final MicroService connectedSvc;

    public MicroServiceConnectedEvent(MicroService connectedSvc) {
        Objects.requireNonNull(connectedSvc);
        this.connectedSvc = connectedSvc;
    }

    public MicroService getConnectedSvc() {
        return connectedSvc;
    }
}
