package global.model;

import java.io.Serializable;

/**
 * Created by Maximilian on 12.01.2017.
 * Used by MicroService when it tries to register with the server.
 */
public interface IRegistrationRequest extends Serializable {

    String getIP();

    String getID();

}