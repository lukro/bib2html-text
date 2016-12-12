package server.events;

import global.model.IClientRequest;

/**
 * @author Maximilian Schirm (denkbares GmbH)
 * @created 10.12.2016
 */

public class RequestStoppedEvent implements Event {

    private final IClientRequest stoppedRequest;

    public RequestStoppedEvent(IClientRequest request) {
        stoppedRequest = request;
    }

    public IClientRequest getStoppedRequest() {
        return stoppedRequest;
    }
}