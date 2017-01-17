package server.events;

/**
 * @author Maximilian Schirm
 *         created 09.12.2016
 */
public class MicroServiceConnectedEvent implements IEvent {

    private final String connectedSvcID;
    private final String connectedSvcIP;

    public MicroServiceConnectedEvent(String connectedSvcID, String connectedSvcIP) {
        this.connectedSvcID = connectedSvcID;
        this.connectedSvcIP = connectedSvcIP;
    }

    public String getConnectedSvcID() {
        return connectedSvcID;
    }

    public String getConnectedSvcIP() {
        return connectedSvcIP;
    }
}
