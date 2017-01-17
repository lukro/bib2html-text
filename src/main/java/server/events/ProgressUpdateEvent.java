package server.events;

/**
 * Created by Maximilian on 17.01.2017.
 */
public class ProgressUpdateEvent implements IEvent {

    private final String clientID;
    private final double progress;


    public ProgressUpdateEvent(String clientID, double progress) {
        this.clientID = clientID;
        this.progress = progress;
    }

    public String getClientID() {
        return clientID;
    }

    public double getProgress() {
        return progress;
    }
}
