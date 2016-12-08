package global.controller;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author daan
 *         created on 12/7/16.
 */
public interface IConnectionPoint {

    void closeConnection() throws IOException, TimeoutException;

    void initConnectionPoint() throws IOException;

    String getHostIP();

    String getID();

}
