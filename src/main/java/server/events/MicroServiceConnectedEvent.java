package server.events;

import microservice.MicroService;

import java.util.Objects;

/**
 * @author Maximilian Schirm
 *         created 09.12.2016
 */
public class MicroServiceConnectedEvent implements Event {

    private final String connectedSvcID;

    public MicroServiceConnectedEvent(String connectedSvcID) {
        this.connectedSvcID = connectedSvcID;
    }

    public String getConnectedSvcID() {
        return connectedSvcID;
    }
}
