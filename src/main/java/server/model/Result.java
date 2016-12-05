package server.model;

import global.identifiers.ResultIdentifier;

import java.util.Objects;

/**
 * @author Maximilian Schirm (denkbares GmbH)
 * @created 05.12.2016
 */

public class Result {

    private final ResultIdentifier identifier;
    private final String content;

    public Result(ResultIdentifier identifier, String content) {
        Objects.requireNonNull(identifier);
        Objects.requireNonNull(content);

        this.identifier = identifier;
        this.content = content;
    }

    public ResultIdentifier getIdentifier() {
        return identifier;
    }

    public String getContent() {
        return content;
    }
}