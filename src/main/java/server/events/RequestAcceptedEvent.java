package server.events;

/**
 * @author Maximilian Schirm
 *         created 05.12.2016
 */
public class RequestAcceptedEvent implements Event {

    private final String requestID;
    private final int reqSize;

    public RequestAcceptedEvent(String requestID, int reqSize) {
        this.requestID = requestID;
        this.reqSize = reqSize;
    }

    public String getRequestID() {
        return requestID;
    }

    public int getReqSize() {
        return reqSize;
    }
}
