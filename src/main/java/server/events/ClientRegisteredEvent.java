package server.events;

import client.controller.Client;

import java.util.Objects;

/**
 * @author Maximilian Schirm
 *         created 09.12.2016
 */
public class ClientRegisteredEvent implements Event {

    final Client registeredClient;

    public ClientRegisteredEvent(Client registeredClient) {
        Objects.requireNonNull(registeredClient);
        this.registeredClient = registeredClient;
    }

    public Client getRegisteredClient() {
        return registeredClient;
    }
}
