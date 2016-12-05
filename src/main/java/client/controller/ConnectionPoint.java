package client.controller;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

/**
 * Created by daan on 11/30/16.
 */
public abstract class ConnectionPoint {

    protected final String hostIP;
    protected final String routingKey;
    protected final Connection connection;
    protected final Channel channel;
    protected final String QUEUE_TO_SERVER_NAME = "queue0";
    protected final String callbackQueueName;
//    protected final String CENTRAL_EXCHANGE_NAME = "centralExchange";
//    protected final String EXCHANGE_TYPE = "direct";


    public ConnectionPoint() throws IOException, TimeoutException {
        this("localhost");
    }

    public ConnectionPoint(String hostIP) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(hostIP);
        this.hostIP = hostIP;
        this.connection = factory.newConnection();
        this.channel = connection.createChannel();
        this.callbackQueueName = channel.queueDeclare().getQueue();
        this.routingKey = UUID.randomUUID().toString();
//        initConnectionPoint();
    }

    public void closeConnection() throws IOException, TimeoutException {
        this.channel.close();
        this.connection.close();
    }

    private void initConnectionPoint() throws IOException {
//        channel.exchangeDeclare(CENTRAL_EXCHANGE_NAME, EXCHANGE_TYPE);
        channel.queueDeclare(callbackQueueName, false, false, false, null);
//        channel.queueBind(callbackQueueName, CENTRAL_EXCHANGE_NAME, routingKey);
    }

    public String getHostIP() {
        return hostIP;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public String getQUEUE_TO_SERVER_NAME() {
        return QUEUE_TO_SERVER_NAME;
    }

    public String getCallbackQueueName() {
        return callbackQueueName;
    }
}
