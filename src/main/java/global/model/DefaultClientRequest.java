package global.model;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author daan
 *         created on 11/30/16.
 */
public class DefaultClientRequest implements IClientRequest {

    private final String clientID;
    private final ArrayList<DefaultEntry> entries;

    public DefaultClientRequest(String clientID, Collection<DefaultEntry> entries) {
        this.clientID = clientID;
        this.entries = new ArrayList<>(entries);
    }

    @Override
    public String getClientID() {
        return clientID;
    }

    @Override
    public ArrayList<DefaultEntry> getEntries() {
        return entries;
    }

    @Override
    public String toString() {
        return ("DefaultClientRequest " + clientID + " has " + entries.size() + " entries.");
    }

}
