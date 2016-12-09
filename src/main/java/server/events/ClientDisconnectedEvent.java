package server.events;

import client.controller.Client;

import java.util.Objects;

/**
 * @author Maximilian Schirm (denkbares GmbH)
 * @created 09.12.2016
 */

public class ClientDisconnectedEvent implements Event {

    private final Client disconnectedClient;

    public ClientDisconnectedEvent(Client disconnectedClient) {
        Objects.requireNonNull(disconnectedClient);
        this.disconnectedClient = disconnectedClient;
    }

    public Client getDisconnectedClient() {
        return disconnectedClient;
    }
}
