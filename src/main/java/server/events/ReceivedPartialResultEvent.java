package server.events;

import server.model.PartialResult;

/**
 * @author Maximilian Schirm
 * @created 05.12.2016
 */

public class ReceivedPartialResultEvent implements Event {

    private final PartialResult result;

    public ReceivedPartialResultEvent(PartialResult result) {
        this.result = result;
    }

    public PartialResult getResult() {
        return result;
    }
}
