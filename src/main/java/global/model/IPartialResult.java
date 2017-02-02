package global.model;

import global.identifiers.IIdentifier;
import global.identifiers.PartialResultIdentifier;

import java.io.Serializable;

/**
 * @author daan, Maximilian
 *         created on 12/7/16.
 * Represents a partial result processed by one MicroService. PartialResults are later combined to Results by the PartialResultCollector.
 * It can be assigned to one specific original file by its PartialResultIdentifier.
 */
public interface IPartialResult extends Serializable, Comparable<IPartialResult> {

    IIdentifier getIdentifier();

    String getContent();
}
