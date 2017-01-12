package global.model;

/**
 * Created by Maximilian on 12.01.2017.
 */
public class DefaultStopOrder implements IStopOrder {

    private final String microserviceID;

    public DefaultStopOrder(String microserviceID) {
        this.microserviceID = microserviceID;
    }

    @Override
    public String getMicroServiceID() {
        return microserviceID;
    }
}
