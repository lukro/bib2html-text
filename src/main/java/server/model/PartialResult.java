package server.model;

import java.util.Objects;
import java.util.UUID;

/**
 * @author Maximilian Schirm (denkbares GmbH)
 * @created 05.12.2016
 */

public class PartialResult {

    private final String content;
    //TODO : Replace with Entry Identifier?
    private final String clientID;
    private final String modeIdentifier;

    public PartialResult(String content, String clientID, String modeIdentifier){
        Objects.requireNonNull(content);
        Objects.requireNonNull(clientID);
        Objects.requireNonNull(modeIdentifier);

        this.content = content;
        this.clientID = clientID;
        this.modeIdentifier = modeIdentifier;
    }

    public String getContent() {
        return content;
    }

    public String getClientID() {
        return clientID;
    }

    public String getModeIdentifier() {
        return modeIdentifier;
    }
}