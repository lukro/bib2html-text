package global.model;

import global.identifiers.PartialResultIdentifier;

import java.util.Collection;
import java.util.Objects;

/**
 * @author Maximilian Schirm (denkbares GmbH)
 * @created 05.12.2016
 */

public class DefaultResult implements IResult {

    private final PartialResultIdentifier identifier;
    private final String content;

    public DefaultResult(PartialResultIdentifier identifier, String content) {
        Objects.requireNonNull(identifier);
        Objects.requireNonNull(content);

        this.identifier = identifier;
        this.content = content;
    }

    public PartialResultIdentifier getIdentifier() {
        return identifier;
    }

    public String getContent() {
        return content;
    }

    /**
     * Creates a new DefaultResult from a Collection of PartialResults.
     *
     * @param partials
     * @return
     */
    public static DefaultResult fromPartials(Collection<DefaultPartialResult> partials) {
        Objects.requireNonNull(partials);

        DefaultPartialResult firstPartial = partials.stream().findFirst().get();
        PartialResultIdentifier identifier = firstPartial.getIdentifier();
        StringBuilder contentBuilder = new StringBuilder();
        partials.forEach(partial -> contentBuilder.append(partial.getContent()));

        return new DefaultResult(identifier, contentBuilder.toString());
    }
}