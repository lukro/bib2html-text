package global.model;

import global.identifiers.EntryIdentifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * Created by daan on 11/30/16.
 */
public class DefaultEntry implements IEntry {

    private final EntryIdentifier entryIdentifier;
    private final String content;
    private final ArrayList<String> cslFiles, templateFiles;

    public DefaultEntry(String clientID, String content, int bibFileIndex, int positionInBibFile, Collection<String> cslFiles, Collection<String> templateFiles) {
        this.entryIdentifier = new EntryIdentifier(clientID, bibFileIndex, positionInBibFile);
        this.content = content;
        this.cslFiles = new ArrayList<>(cslFiles);
        this.templateFiles = new ArrayList<>(templateFiles);
    }

    @Override
    public EntryIdentifier getEntryIdentifier() {
        return this.entryIdentifier;
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
    public ArrayList<String> getTemplateFiles() {
        return templateFiles;
    }

    @Override
    public String toString() {
        return (System.lineSeparator() + "Entry with clientID '" +
                this.entryIdentifier.getClientID() + "' and content: "
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
                Objects.equals(getTemplateFiles(), that.getTemplateFiles());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEntryIdentifier(), getContent(), getCslFiles(), getTemplateFiles());
    }
}
