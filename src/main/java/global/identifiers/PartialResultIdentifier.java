package global.identifiers;

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
        return entryIdentifier.getClientID();
    }

    @Override
    public int getBibFileIndex() {
        return entryIdentifier.getBibFileIndex();
    }

    @Override
    public int getPositionInBibFile() {
        return entryIdentifier.getPositionInBibFile();
    }

    public int getCslFileIndex() {
        return cslFileIndex;
    }

    public int getTemplateFileIndex() {
        return templateFileIndex;
    }

    @Override
    public String toString() {
        return ("PartialResult for client " + entryIdentifier.getClientID() +
                " belongs to .bib-file " + entryIdentifier.getBibFileIndex() +
                " at position " + entryIdentifier.getPositionInBibFile() +
                "and was converted with .csl-file " + cslFileIndex +
                " and templateFile " + templateFileIndex);
    }


    /**
     * Ignores EntryIdentifier aside from the bib file index
     *
     * @param o The object to compare.
     * @return A boolean for equality.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PartialResultIdentifier that = (PartialResultIdentifier) o;

        if (cslFileIndex != that.cslFileIndex) return false;
        if (templateFileIndex != that.templateFileIndex) return false;
        if (isHasErrors() != that.isHasErrors()) return false;
        return getBibFileIndex() == that.getBibFileIndex();
    }

    @Override
    public int hashCode() {
        int result = cslFileIndex;
        result = 31 * result + templateFileIndex;
        result = 31 * result + (isHasErrors() ? 1 : 0);
        return result;
    }

    public boolean isHasErrors() {
        return hasErrors;
    }

    public void setHasErrors(boolean hasErrors) {
        this.hasErrors = hasErrors;
    }
}