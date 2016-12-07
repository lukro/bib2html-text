package global.controller;

/**
 * Created by daan on 12/7/16.
 */
public interface IConnectionPoint {

    void closeConnection();

    String getHostIP();

    String getCallbackQueueName();

    String getQUEUE_TO_SERVER_NAME();

}
