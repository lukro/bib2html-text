package global.model;

/**
 * Created by Maximilian on 12.01.2017.
 */
public class DefaultRegistrationAck implements IRegistrationAck{

    private final String taskQueueName;

    public DefaultRegistrationAck(String taskQueueName){
        this.taskQueueName = taskQueueName;
    }

    @Override
    public String getTaskQueueName() {
        return null;
    }
}
