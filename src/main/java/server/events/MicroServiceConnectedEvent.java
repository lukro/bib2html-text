package server.events;

/**
 * @author Maximilian Schirm
 *         created 09.12.2016
 */
public class MicroServiceConnectedEvent implements IEvent {

    private final String connectedSvcID;

    public MicroServiceConnectedEvent(String connectedSvcID) {
        this.connectedSvcID = connectedSvcID;
    }

    public String getConnectedSvcID() {
        return connectedSvcID;
    }
}
