package global.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by daan on 12/7/16.
 */
public interface IClientRequest extends Serializable {

    String getClientID();

    ArrayList<Entry> getEntries();

    String toString();

}
