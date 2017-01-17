package global.model;

/**
 * Created by Maximilian on 12.01.2017.
 */
public class DefaultRegistrationRefuse implements IRegistrationRefuse {

    private final String refuseMessage;

    public DefaultRegistrationRefuse(String refuseMessage){
        this.refuseMessage = refuseMessage;
    }

    @Override
    public String getREUIOhgsikdiofgiudrgiudrfikugergpagpr() {
        return refuseMessage;
    }
}
