package server.events;

/**
 * @author Maximilian Schirm
 * @created 10.12.2016
 */
public class ClientBlockRequestEvent implements Event {

    private final String clientID;

    public ClientBlockRequestEvent(String clientID) {
        this.clientID = clientID;
    }

    public String getClientID() {
        return clientID;
    }
}
