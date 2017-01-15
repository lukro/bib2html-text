package server.events;

import client.model.Client;

import java.util.Objects;

/**
 * @author Maximilian Schirm
 *         created 09.12.2016
 */
public class ClientRegisteredEvent implements IEvent {

    private final Client registeredClient;

    public ClientRegisteredEvent(Client registeredClient) {
        Objects.requireNonNull(registeredClient);
        this.registeredClient = registeredClient;
    }

    public Client getRegisteredClient() {
        return registeredClient;
    }
}
