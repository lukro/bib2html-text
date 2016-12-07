package global.model;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by daan on 11/30/16.
 */
public class DefaultEntry implements IEntry {

    //TODO : Replace with EntryIdentifier?
    private final String clientID;
    private final String content;
    private final ArrayList<String> cslFiles;
    private final ArrayList<String> templateFiles;

    public DefaultEntry(String clientID, String content, Collection<String> cslFiles, Collection<String> templateFiles) {
        this.clientID = clientID;
        this.content = content;
        this.cslFiles = new ArrayList<>(cslFiles);
        this.templateFiles = new ArrayList<>(templateFiles);
    }

    public String getClientID() {
        return clientID;
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
        return (System.lineSeparator() + "DefaultEntry with clientID '" + clientID + "' and content: "
                + System.lineSeparator() + content + System.lineSeparator() +
                "must be converted with " + cslFiles.size() + " cslFiles and " +
                templateFiles.size() + " templateFiles." + System.lineSeparator());
    }
}
