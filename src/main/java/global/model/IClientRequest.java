package global.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author daan
 *         created on 12/7/16.
 *
 * Represents a Client Request. Client Requests are created on the client and then get published to the queue.
 * They are made of single entries stored in a list.
 */
public interface IClientRequest extends Serializable {

    String getClientID();

    String getSecretKey();

    ArrayList<IEntry> getEntries();

    String toString();

}
