package server.events;

import global.model.DefaultPartialResult;
import global.model.IPartialResult;

/**
 * @author Maximilian Schirm
 *         created 05.12.2016
 */

public class ReceivedPartialResultEvent implements Event {

    private final IPartialResult result;

    public ReceivedPartialResultEvent(IPartialResult result) {
        this.result = result;
    }

    public IPartialResult getPartialResult() {
        return result;
    }
}
