package global.model;

import global.identifiers.EntryIdentifier;
import global.identifiers.IIdentifier;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author daan
 *         created on 12/7/16.
 * Represents one BibTeX Entry. It is used by the BibTeXFileSplitter on the client and is part of a client request.
 * It can be assigned to one specific original file by its EntryIdentifier.
 */
public interface IEntry extends Serializable {

    IIdentifier getEntryIdentifier();

    String getContent();

    ArrayList<String> getCslFiles();

    ArrayList<String> getTemplates();

    int getAmountOfExpectedPartials();

    String toString();

}
