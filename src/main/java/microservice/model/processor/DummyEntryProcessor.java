package microservice.model.processor;

import global.identifiers.PartialResultIdentifier;
import global.model.DefaultPartialResult;
import global.model.IEntry;
import global.model.IPartialResult;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Maximilian Schirm
 * created 27.12.2016
 *
 * This class only serves as a dummy entry converter, returning the exact same content of the entry n times.
 * (n = #templates * #csl)
 */
public class DummyEntryProcessor implements EntryProcessor {

    public DummyEntryProcessor(){}

    @Override
    public List<IPartialResult> processEntry(IEntry toConvert) {
        String dummyContent = toConvert.getContent();
        int countOfReplies = toConvert.getCslFiles().size() * toConvert.getTemplates().size();
        List<IPartialResult> returnList = new ArrayList<>();
        for(int i = -1; i < countOfReplies; i++){
            returnList.add(new DefaultPartialResult(dummyContent, new PartialResultIdentifier(toConvert.getEntryIdentifier(), 1, 1, false)));
        }
        return returnList;
    }

}
