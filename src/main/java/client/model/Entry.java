package client.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by daan on 11/30/16.
 */
public class Entry implements Serializable {

    //TODO : Replace with EntryIdentifier?
    private final String clientID;
    private final String content;
    private final ArrayList<String> cslFiles;
    private final ArrayList<String> templateFiles;

    public Entry(String clientID, String content, Collection<String> cslFiles, Collection<String> templateFiles) {
        this.clientID = clientID;
        this.content = content;
        this.cslFiles = new ArrayList<>(cslFiles);
        this.templateFiles = new ArrayList<>(templateFiles);
    }

    public String getClientID() {
        return clientID;
    }

    public String getContent() {
        return content;
    }

    public ArrayList<String> getCslFiles() {
        return cslFiles;
    }

    public ArrayList<String> getTemplateFiles() {
        return templateFiles;
    }

    public String toString() {
        return (System.lineSeparator() + "Entry with clientID '" + clientID + "' and content: "
                + System.lineSeparator() + content + System.lineSeparator() +
                "must be converted with " + cslFiles.size() + " cslFiles and " +
                templateFiles.size() + " templateFiles." + System.lineSeparator());
    }
}
