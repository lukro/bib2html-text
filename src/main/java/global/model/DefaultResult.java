package global.model;

import global.identifiers.PartialResultIdentifier;

import java.lang.reflect.Array;
import java.util.*;

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
        //Group Results
        Map<PartialResultIdentifier, Collection<IPartialResult>> collectionMap = new HashMap<>();
        partials.forEach(partial -> {
            PartialResultIdentifier identifier = partial.getIdentifier();
            if(collectionMap.containsKey(identifier))
                collectionMap.get(identifier).add(partial);
            else
                collectionMap.put(identifier, Arrays.asList(partial));
        });

        //Build results
        Collection<String> generatedStrings = new ArrayList<>();
        collectionMap.forEach((identifier, results) -> {
            StringBuilder bldr = new StringBuilder();
            //TODO Sort results
            results.forEach(result -> bldr.append(result));
            generatedStrings.add(bldr.toString());
        });

        //Extract Client ID
        String cid = partials.stream()
                            .findFirst()
                            .get()
                            .getIdentifier()
                            .getClientID();

        return new DefaultResult(cid,generatedStrings);
    }
}