package global.identifiers;

/**
 * @author Maximilian, daan
 */
public enum QueueNames {

    CLIENT_REQUEST_QUEUE_NAME("clientRequestQueue"),
    TASK_QUEUE_NAME("taskQueue"),
    MICROSERVICE_REGISTRATION_QUEUE_NAME("registrationQueue"),
    MICROSERVICE_STOP_QUEUE_NAME("stopQueueName"),
    STOP_EXCHANGE_NAME("stopExchange");

    private final String nameOfQueue;

    QueueNames(String nameOfQueue) {
        this.nameOfQueue = nameOfQueue;
    }

    @Override
    public String toString() {
        return nameOfQueue;
    }
}