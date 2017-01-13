package global.model;

/**
 * Created by daan on 1/13/17.
 */
public class DefaultStopOrderAck implements IStopOrderAck {

    private final String stoppedMicroServiceID;
    private final String stoppedMicroServieIP;

    public DefaultStopOrderAck(String stoppedMicroServiceID, String stoppedMicroServiceIP) {
        this.stoppedMicroServiceID = stoppedMicroServiceID;
        this.stoppedMicroServieIP = stoppedMicroServiceIP;
    }

    @Override
    public String getStoppedMicroServiceID() {
        return stoppedMicroServiceID;
    }

    @Override
    public String getStoppedMicroServiceIP() {
        return stoppedMicroServieIP;
    }
}
