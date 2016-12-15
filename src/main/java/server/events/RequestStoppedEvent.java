package server.events;

/**
 * @author Maximilian Schirm
 * @created 10.12.2016
 */
public class RequestStoppedEvent implements Event {

    private final String clientIDofStoppedRequest;

    public RequestStoppedEvent(String requestClientID) {
        clientIDofStoppedRequest = requestClientID;
    }

    public String getStoppedRequestClientID() {
        return clientIDofStoppedRequest;
    }
}