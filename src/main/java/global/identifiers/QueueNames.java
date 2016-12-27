package global.identifiers;

/**
 * Created by daan on 27.12.2016.
 */
public final class QueueNames {

    public final String CLIENT_REQUEST_QUEUE_NAME = "clientRequestQueue";
    public final String TASK_QUEUE_NAME = "taskQueue";

    private QueueNames() {
        throw new AssertionError("QueueNames can't be instantiated.");
    }

}
