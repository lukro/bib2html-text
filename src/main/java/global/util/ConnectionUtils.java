package global.util;

import com.rabbitmq.client.AMQP;

/**
 * Created by daan on 1/13/17.
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
