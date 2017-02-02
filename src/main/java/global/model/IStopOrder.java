package global.model;

import java.io.Serializable;

/**
 * Created by Maximilian on 12.01.2017.
 * Used when a MicroService ist stopped by the user of the server. It is published on the queue of the MicroService.
 */
public interface IStopOrder extends Serializable {

    String getMicroServiceID();

}