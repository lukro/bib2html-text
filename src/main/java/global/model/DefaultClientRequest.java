package global.model;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author daan
 *         created on 11/30/16.
 */
public class DefaultClientRequest implements IClientRequest {

    private final String clientID;
    private final ArrayList<IEntry> entries;

    public DefaultClientRequest(String clientID, Collection<IEntry> entries) {
        this.clientID = clientID;
        this.entries = new ArrayList<>(entries);
    }

    @Override
    public String getClientID() {
        return clientID;
    }

    @Override
    public ArrayList<IEntry> getEntries() {
        return entries;
    }

    @Override
    public String toString() {
        return ("DefaultClientRequest " + clientID + " has " + entries.size() + " entries.");
    }

}
