package global.util;

import com.rabbitmq.client.AMQP;

/**
 * Created by daan on 1/13/17.
 * Just a utility class for RabbitMQ properties.
 */
public final class ConnectionUtils {

    private ConnectionUtils() {
        throw new AssertionError("ConnectionUtils is a static class.");
    }

    public static AMQP.BasicProperties getReplyProps(AMQP.BasicProperties basicProperties) {
        return new AMQP.BasicProperties
                .Builder()
                .correlationId(basicProperties.getCorrelationId())
                .build();
    }

}
