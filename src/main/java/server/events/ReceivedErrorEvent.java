package server.events;

/**
 * @author Maximilian Schirm
 *         created 05.12.2016
 */
public class ReceivedErrorEvent implements IEvent {

    private final String requestID;

    public ReceivedErrorEvent(String requestID) {
        this.requestID = requestID;
    }

    public String getResultID() {
        return requestID;
    }
}
