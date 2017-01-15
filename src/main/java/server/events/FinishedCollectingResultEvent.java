package server.events;

import global.model.IResult;

/**
 * @author Maximilian Schirm
 *         created 05.12.2016
 */
public class FinishedCollectingResultEvent implements IEvent {

    private final IResult result;

    public FinishedCollectingResultEvent(IResult result) {
        this.result = result;
    }

    public IResult getResult() {
        return result;
    }
}
