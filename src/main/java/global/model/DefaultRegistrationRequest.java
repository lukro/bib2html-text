package global.model;

/**
 * Created by Maximilian on 12.01.2017.
 */
public class DefaultRegistrationRequest implements IRegistrationRequest {

    private final String id, ip;

    public DefaultRegistrationRequest(String ip, String id){
        this.ip = ip;
        this.id = id;
    }

    @Override
    public String getIP() {
        return ip;
    }

    @Override
    public String getID() {
        return id;
    }
}
