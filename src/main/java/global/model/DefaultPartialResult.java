package global.model;

import global.identifiers.ResultIdentifier;

import java.util.Objects;

/**
 * @author Maximilian Schirm (denkbares GmbH)
 * @created 05.12.2016
 */

public class DefaultPartialResult implements IPartialResult {

    private final String content;
    private final ResultIdentifier identifier;
    private final String modeIdentifier;

    public DefaultPartialResult(String content, ResultIdentifier identifier, String modeIdentifier) {
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