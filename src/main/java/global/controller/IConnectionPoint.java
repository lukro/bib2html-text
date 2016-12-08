package global.controller;

/**
 * Created by daan on 12/7/16.
 */
public interface IConnectionPoint {

    void closeConnection();

    void initConnectionPoint();

    String getHostIP();

    String getID();

}
