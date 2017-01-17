package global.model;

import global.identifiers.EntryIdentifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * @author daan
 *         created on 11/30/16.
 */
public class DefaultEntry implements IEntry {

    private final EntryIdentifier entryIdentifier;
    private final String content;
    private final ArrayList<String> cslFiles, templateFiles;

    public static final class Builder {

        private String clientID, content;
        private ArrayList<String> cslFiles, templatesFiles;
        private int bibFileIndex, positionInBibFile;

        public Builder(String clientID) {
            this.clientID = clientID;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder cslFiles(ArrayList<String> cslFiles) {
            this.cslFiles = cslFiles;
            return this;
        }

        public Builder templateFiles(ArrayList<String> templatesFiles) {
            this.templatesFiles = templatesFiles;
            return this;
        }

        public Builder bibFileIndex(int bibFileIndex) {
            this.bibFileIndex = bibFileIndex;
            return this;
        }

        public Builder positionInBibFile(int positionInBibFile) {
            this.positionInBibFile = positionInBibFile;
            return this;
        }

        public DefaultEntry build() {
            return new DefaultEntry(this);
        }
    }

    private DefaultEntry(Builder builder) {
        this.entryIdentifier = new EntryIdentifier(
                builder.clientID, builder.bibFileIndex, builder.positionInBibFile);
        this.content = builder.content;
        this.cslFiles = builder.cslFiles;
        this.templateFiles = builder.templatesFiles;
    }

    @Override
    public EntryIdentifier getEntryIdentifier() {
        return entryIdentifier;
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public ArrayList<String> getCslFiles() {
        return cslFiles;
    }

    @Override
    public ArrayList<String> getTemplates() {
        return templateFiles;
    }

    @Override
    public int getAmountOfExpectedPartials() {
        int amountOfCsl = this.getCslFiles().size();
        int amountOfTemplates = this.getTemplates().size();

        if (amountOfCsl == 0)
            amountOfCsl = 1;
        if (amountOfTemplates == 0)
            amountOfTemplates = 1;

        return amountOfCsl * amountOfTemplates;
    }

    @Override
    public String toString() {
        return (System.lineSeparator() + "Entry with clientID '" +
                entryIdentifier.getClientID() + "' and content: "
                + System.lineSeparator() + content + System.lineSeparator() +
                "from .bib-file " + entryIdentifier.getBibFileIndex() + " at position "
                + entryIdentifier.getPositionInBibFile() + " must be converted with " + cslFiles.size() +
                " cslFiles and " + templateFiles.size() + " templateFiles." + System.lineSeparator());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DefaultEntry)) return false;
        DefaultEntry that = (DefaultEntry) o;
        return Objects.equals(getEntryIdentifier(), that.getEntryIdentifier()) &&
                Objects.equals(getContent(), that.getContent()) &&
                Objects.equals(getCslFiles(), that.getCslFiles()) &&
                Objects.equals(getTemplates(), that.getTemplates());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEntryIdentifier(), getContent(), getCslFiles(), getTemplates());
    }
}
