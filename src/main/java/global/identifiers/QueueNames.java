package global.identifiers;

/**
 * @author Maximilian, daan
 */
public enum  QueueNames {

    CLIENT_REQUEST_QUEUE_NAME("clientRequestQueue"),
    TASK_QUEUE_NAME("taskQueue");

    private final String nameOfQueue;
    private QueueNames(String nameOfQueue) {
        this.nameOfQueue = nameOfQueue;
    }

    @Override
    public String toString(){
        return nameOfQueue;
    }
}