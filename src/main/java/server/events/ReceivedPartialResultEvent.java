package server.events;

import global.model.DefaultPartialResult;

/**
 * @author Maximilian Schirm
 * @created 05.12.2016
 */

public class ReceivedPartialResultEvent implements Event {

    private final DefaultPartialResult result;

    public ReceivedPartialResultEvent(DefaultPartialResult result) {
        this.result = result;
    }

    public DefaultPartialResult getPartialResult() {
        return result;
    }
}
