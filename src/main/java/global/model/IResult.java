package global.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author daan
 *         created on 12/7/16.
 * Represents a result that is ready for being published back to the client Queue.
 */
public interface IResult extends Serializable {

    String getClientID();

    ArrayList<String> getFileContents();

    String toString();
}
