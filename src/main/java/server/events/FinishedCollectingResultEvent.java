package server.events;

import server.model.Result;

/**
 * @author Maximilian Schirm (denkbares GmbH)
 * @created 05.12.2016
 */

public class FinishedCollectingResultEvent implements Event {

    private final Result result;

    public FinishedCollectingResultEvent(Result result) {
        this.result = result;
    }

    public Result getResult() {
        return result;
    }
}
