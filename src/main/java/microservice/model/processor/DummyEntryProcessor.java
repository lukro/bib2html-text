package microservice.model.processor;

import global.identifiers.EntryIdentifier;
import global.identifiers.IIdentifier;
import global.identifiers.PartialResultIdentifier;
import global.model.DefaultPartialResult;
import global.model.IEntry;
import global.model.IPartialResult;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Maximilian Schirm
 *         created 27.12.2016
 *         <p>
 *         This class only serves as a dummy entry converter, returning the exact same content of the entry n times.
 *         (n = #templates * #csl)
 */
public class DummyEntryProcessor implements IEntryProcessor {

    public DummyEntryProcessor() {
    }

    @Override
    public List<IPartialResult> processEntry(IEntry toConvert) {
        List<IPartialResult> result = new ArrayList<>();
        final String content = toConvert.getContent();
        final int expectedAmountOfPartials = toConvert.getAmountOfExpectedPartials();
        final IIdentifier entryIdentifier = toConvert.getEntryIdentifier();
        for (int i = 0; i < expectedAmountOfPartials; i++) {
            final PartialResultIdentifier currentPartialIdentifier = new PartialResultIdentifier(entryIdentifier, -99, -99, false);
            final IPartialResult currentPartial = new DefaultPartialResult(content, currentPartialIdentifier);
            result.add(currentPartial);
        }
        return result;
    }

}
