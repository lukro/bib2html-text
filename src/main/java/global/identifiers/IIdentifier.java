package global.identifiers;

import java.io.Serializable;

/**
 * Created by Maximilian on 05.12.2016.
 *
 * @author Maximilian Schirm, daan
 * Interface for Identifiers used in the project.
 */
public interface IIdentifier extends Serializable {

    String getClientID();

    int getBibFileIndex();

    int getPositionInBibFile();

    String toString();

}