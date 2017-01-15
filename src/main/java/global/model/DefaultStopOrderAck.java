package global.model;

/**
 * Created by daan on 1/13/17.
 */
public class DefaultStopOrderAck implements IStopOrderAck {

    private final String stoppedMicroServiceID;
    private final String stoppedMicroServiceIP;

    public DefaultStopOrderAck(String stoppedMicroServiceID, String stoppedMicroServiceIP) {
        this.stoppedMicroServiceID = stoppedMicroServiceID;
        this.stoppedMicroServiceIP = stoppedMicroServiceIP;
    }

    @Override
    public String getStoppedMicroServiceID() {
        return stoppedMicroServiceID;
    }

    @Override
    public String getStoppedMicroServiceIP() {
        return stoppedMicroServiceIP;
    }
}
