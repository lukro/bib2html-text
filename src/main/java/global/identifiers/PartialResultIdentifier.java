package global.identifiers;

import java.util.Objects;

/**
 * @author Maximilian Schirm, daan
 *         created: 05.12.2016
 *         <p>
 *         Identifies a DefaultResult
 */

public class PartialResultIdentifier implements IIdentifier {

    private final EntryIdentifier entryIdentifier;
    private final int cslFileIndex, templateFileIndex;
    private boolean hasErrors;

    public PartialResultIdentifier(EntryIdentifier entryIdentifier, int cslFileIndex, int templateFileIndex) {
        this(entryIdentifier, cslFileIndex, templateFileIndex, false);
    }

    public PartialResultIdentifier(EntryIdentifier entryIdentifier, int cslFileIndex, int templateFileIndex, boolean hasErrors) {
        this.entryIdentifier = entryIdentifier;
        this.cslFileIndex = cslFileIndex;
        this.templateFileIndex = templateFileIndex;
        this.hasErrors = hasErrors;
    }

    @Override
    public String getClientID() {
        return this.entryIdentifier.getClientID();
    }

    @Override
    public int getBibFileIndex() {
        return this.entryIdentifier.getBibFileIndex();
    }

    @Override
    public int getPositionInBibFile() {
        return this.entryIdentifier.getPositionInBibFile();
    }

    @Override
    public String toString() {
        return ("PartialResult for client " + entryIdentifier.getClientID() +
                " belongs to .bib-file " + entryIdentifier.getBibFileIndex() +
                " at position " + entryIdentifier.getPositionInBibFile() +
                "and was converted with .csl-file " + cslFileIndex +
                " and templateFile " + templateFileIndex);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PartialResultIdentifier)) return false;
        PartialResultIdentifier that = (PartialResultIdentifier) o;
        return hasErrors == that.hasErrors &&
                Objects.equals(entryIdentifier, that.entryIdentifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entryIdentifier, hasErrors);
    }

    public boolean isHasErrors() {
        return hasErrors;
    }

    public void setHasErrors(boolean hasErrors) {
        this.hasErrors = hasErrors;
    }
}