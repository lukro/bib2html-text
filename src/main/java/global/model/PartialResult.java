package global.model;

import global.identifiers.ResultIdentifier;

import java.util.Objects;
import java.util.UUID;

/**
 * @author Maximilian Schirm (denkbares GmbH)
 * @created 05.12.2016
 */

public class PartialResult {

    private final String content;
    private final ResultIdentifier identifier;
    private final String modeIdentifier;

    public PartialResult(String content, ResultIdentifier identifier, String modeIdentifier){
        Objects.requireNonNull(content);
        Objects.requireNonNull(identifier);
        Objects.requireNonNull(modeIdentifier);

        this.content = content;
        this.identifier = identifier;
        this.modeIdentifier = modeIdentifier;
    }

    public ResultIdentifier getIdentifier() {
        return identifier;
    }

    public String getContent() {
        return content;
    }

    public String getModeIdentifier() {
        return modeIdentifier;
    }
}