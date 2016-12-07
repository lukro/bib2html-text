package server.events;

import global.model.DefaultResult;

/**
 * @author Maximilian Schirm (denkbares GmbH)
 * @created 05.12.2016
 */

public class FinishedCollectingResultEvent implements Event {

    private final DefaultResult result;

    public FinishedCollectingResultEvent(DefaultResult result) {
        this.result = result;
    }

    public DefaultResult getResult() {
        return result;
    }
}
