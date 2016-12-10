package server.events;

import global.model.IClientRequest;

/**
 * @author Maximilian Schirm
 * @created 10.12.2016
 */

public class StopRequestEvent implements Event {

    private final IClientRequest request;

    public StopRequestEvent(IClientRequest request) {
        this.request = request;
    }

    public IClientRequest getRequest() {
        return request;
    }
}
