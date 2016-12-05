package server.model;

import global.identifiers.Identifier;
import global.identifiers.ResultIdentifier;

import java.util.Collection;
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

    /**
     * Creates a new Result from a Collection of PartialResults.
     *
     * @param partials
     * @return
     */
    public static Result fromPartials(Collection<PartialResult> partials){
        Objects.requireNonNull(partials);

        PartialResult firstPartial = partials.stream().findFirst().get();
        ResultIdentifier identifier = firstPartial.getIdentifier();
        StringBuilder contentBuilder = new StringBuilder();
        partials.forEach(partial -> contentBuilder.append(partial.getContent()));

        return new Result(identifier, contentBuilder.toString());
    }
}