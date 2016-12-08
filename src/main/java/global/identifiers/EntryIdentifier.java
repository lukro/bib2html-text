package global.identifiers;

import java.util.Objects;

/**
 * @author Maximilian Schirm, daan
 *         created: 05.12.2016
 *         <p>
 *         Identifies an DefaultEntry precisely
 */

public class EntryIdentifier implements IIdentifier {

    private final String clientID;
    private final int bibFileIndex, positionInBibFile;

    public EntryIdentifier(String clientID, int bibFileIndex, int positionInBibFile) {
        this.clientID = clientID;
        this.bibFileIndex = bibFileIndex;
        this.positionInBibFile = positionInBibFile;
    }

    @Override
    public String getClientID() {
        return clientID;
    }

    @Override
    public int getBibFileIndex() {
        return bibFileIndex;
    }

    @Override
    public int getPositionInBibFile() {
        return positionInBibFile;
    }

    @Override
    public String toString() {
        return ("Entry from client " + clientID +
                " in .bib-file " + bibFileIndex + " at position " + positionInBibFile + ".");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EntryIdentifier)) return false;
        EntryIdentifier that = (EntryIdentifier) o;
        return getBibFileIndex() == that.getBibFileIndex() &&
                getPositionInBibFile() == that.getPositionInBibFile() &&
                Objects.equals(getClientID(), that.getClientID());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClientID(), getBibFileIndex(), getPositionInBibFile());
    }
}
