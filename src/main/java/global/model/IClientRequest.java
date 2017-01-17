package global.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author daan
 *         created on 12/7/16.
 */
public interface IClientRequest extends Serializable {

    String getClientID();

    String getSecretKey();

    ArrayList<IEntry> getEntries();

    String toString();

}
