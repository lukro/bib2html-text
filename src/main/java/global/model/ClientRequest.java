package global.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by daan on 11/30/16.
 */
public class ClientRequest implements IClientRequest, Serializable {

    private final String clientID;
    private final ArrayList<Entry> entries;

    public ClientRequest(String clientID, Collection<Entry> entries) {
        this.clientID = clientID;
        this.entries = new ArrayList<>(entries);
    }

    @Override
    public String getClientID() {
        return clientID;
    }

    @Override
    public ArrayList<Entry> getEntries() {
        return entries;
    }

    @Override
    public String toString() {
        return (System.lineSeparator() +
                "ClientRequest '" + clientID + "' has " + entries.size() + " entries."
                + System.lineSeparator());
    }

}
