package server.events;

import global.model.IRegistrationRequest;

/**
 * Created by pc on 12.01.2017.
 */
public class ReceivedRegistrationRequestEvent implements IEvent{

    private final IRegistrationRequest receivedRequest;


    public ReceivedRegistrationRequestEvent(IRegistrationRequest receivedRequest) {
        this.receivedRequest = receivedRequest;
    }

    public IRegistrationRequest getReceivedRequest(){
        return receivedRequest;
    }
}
