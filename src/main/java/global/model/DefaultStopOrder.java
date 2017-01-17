package global.model;

/**
 * Created by Maximilian on 12.01.2017.
 */
public class DefaultStopOrder implements IStopOrder {

    private final String microServiceID;

    public DefaultStopOrder(String microServiceID) {
        this.microServiceID = microServiceID;
    }

    @Override
    public String getMicroServiceID() {
        return microServiceID;
    }
}
