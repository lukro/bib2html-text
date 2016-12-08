package global.model;

import global.identifiers.EntryIdentifier;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author daan
 *         created on 12/7/16.
 */
public interface IEntry extends Serializable {

    EntryIdentifier getEntryIdentifier();

    String getContent();

    ArrayList<String> getCslFiles();

    ArrayList<String> getTemplateFiles();

    String toString();

}
