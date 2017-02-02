package global.model;

import java.io.Serializable;

/**
 * Created by Maximilian on 12.01.2017.
 * Used when a MicroService tries to register with the server and the server accepts the registration.
 * It gets published on the queue of the MicroService.
 */
public interface IRegistrationAck extends Serializable {

    String getTaskQueueName();

}