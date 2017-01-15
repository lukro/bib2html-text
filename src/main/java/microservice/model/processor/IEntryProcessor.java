package microservice.model.processor;

import global.model.IEntry;
import global.model.IPartialResult;

import java.util.List;

/**
 * @author Maximilian
 *         on 27.12.2016
 */
public interface IEntryProcessor {

    List<IPartialResult> processEntry(IEntry toConvert);

}
