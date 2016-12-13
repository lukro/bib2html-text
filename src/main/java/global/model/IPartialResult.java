package global.model;

import global.identifiers.PartialResultIdentifier;

import java.io.Serializable;

/**
 * @author daan, Maximilian
 *         created on 12/7/16.
 */
public interface IPartialResult extends Serializable {
    public PartialResultIdentifier getIdentifier();
}
