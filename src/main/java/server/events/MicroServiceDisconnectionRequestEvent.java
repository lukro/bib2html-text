package server.events;

/**
 * @author Maximilian Schirm
 *         created 10.12.2016
 */
public class MicroServiceDisconnectionRequestEvent implements IEvent {

    private final String toDisconnectID;

    public MicroServiceDisconnectionRequestEvent(String toDisconnectID) {
        this.toDisconnectID = toDisconnectID;
    }

    public String getToDisconnectID() {
        return toDisconnectID;
    }
}
