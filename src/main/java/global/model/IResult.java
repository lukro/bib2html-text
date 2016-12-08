package global.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author daan
 *         created on 12/7/16.
 */
public interface IResult extends Serializable {

    String getClientID();

    ArrayList<String> getFileContents();

    String toString();
}
