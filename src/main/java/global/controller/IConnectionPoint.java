package global.controller;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author daan
 *         created on 12/7/16.
 * Interface for all Classes using RabbitMQ (Client, Server, MicroService)
 * It builds a connection and then
 */
public interface IConnectionPoint {

    void closeConnection() throws IOException, TimeoutException;

    void initConnectionPoint() throws IOException;

    void declareQueues() throws IOException;

    void consumeIncomingQueues() throws IOException;

    String getHostIP();

    String getID();

}
