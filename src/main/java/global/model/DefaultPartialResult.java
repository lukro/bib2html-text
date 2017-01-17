package global.model;

import global.identifiers.PartialResultIdentifier;

import java.util.Objects;

/**
 * @author Maximilian Schirm, daan
 *         created on 05.12.2016
 */

public class DefaultPartialResult implements IPartialResult {

    private final String content;
    private final PartialResultIdentifier identifier;

    public DefaultPartialResult(String content, PartialResultIdentifier identifier) {
        Objects.requireNonNull(content);
        Objects.requireNonNull(identifier);
        this.content = content;
        this.identifier = identifier;
    }

    public PartialResultIdentifier getIdentifier() {
        return identifier;
    }

    public String getContent() {
        return content;
    }

    @Override
    public int compareTo(IPartialResult o) {
        return Integer.signum(this.getIdentifier().getPositionInBibFile() - o.getIdentifier().getPositionInBibFile());
    }
}