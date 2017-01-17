package global.model;

/**
 * Created by Maximilian on 12.01.2017.
 */
public class DefaultRegistrationRequest implements IRegistrationRequest {

    private final String ID, hostIP;

    public DefaultRegistrationRequest(String ID, String hostIP) {
        this.ID = ID;
        this.hostIP = hostIP;
    }

    @Override
    public String getIP() {
        return hostIP;
    }

    @Override
    public String getID() {
        return ID;
    }
}
