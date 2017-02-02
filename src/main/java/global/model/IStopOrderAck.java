package global.model;

import java.io.Serializable;

/**
 * Created by daan on 1/13/17.
 * A MicroService publishes a Stop Order Ackknowledgement if it successfully received a Stop Order Request.
 * After that, the MicroService is deregistered and terminated.
 */
public interface IStopOrderAck extends Serializable {

    String getStoppedMicroServiceID();

    String getStoppedMicroServiceIP();

}
