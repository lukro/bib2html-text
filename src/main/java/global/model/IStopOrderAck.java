package global.model;

import java.io.Serializable;

/**
 * Created by daan on 1/13/17.
 */
public interface IStopOrderAck extends Serializable {

    String getStoppedMicroServiceID();

    String getStoppedMicroServiceIP();

}
