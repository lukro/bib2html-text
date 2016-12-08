package global.model;

import global.identifiers.PartialResultIdentifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * @author Maximilian Schirm, daan
 *         created on 05.12.2016
 */

public class DefaultResult implements IResult {

    private final String clientID;
    private final ArrayList<String> fileContents;

    public DefaultResult(String clientID, Collection<String> fileContents) {
        this.clientID = clientID;
        this.fileContents = new ArrayList<>(fileContents);
    }

    @Override
    public String getClientID() {
        return clientID;
    }

    @Override
    public ArrayList<String> getFileContents() {
        return fileContents;
    }

    @Override
    public String toString() {
        return ("DefaultResult " + clientID + " has " + fileContents.size() + " outputFiles.");
    }

    public static DefaultResult buildResultfromPartials(Collection<DefaultPartialResult> partials) {
        //TODO: implement build DefaultResult from Partials
        return null;
    }
}