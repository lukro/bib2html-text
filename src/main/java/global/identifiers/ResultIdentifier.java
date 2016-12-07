package global.identifiers;

/**
 * @author Maximilian Schirm (denkbares GmbH)
 * @created 05.12.2016
 *
 * Identifies a DefaultResult
 */

public class ResultIdentifier implements Identifier {

    private final String clientID, bibFileID, cslFileID, templateID;
    private final boolean hasErrors;

    public ResultIdentifier(String clientID, String bibFileID, String cslFileID, String templateID, boolean hasErrors) {
        this.clientID = clientID;
        this.bibFileID = bibFileID;
        this.cslFileID = cslFileID;
        this.templateID = templateID;
        this.hasErrors = hasErrors;
    }

    public boolean isHasErrors() {
        return hasErrors;
    }

    public String getClientID() {
        return clientID;
    }

    public String getBibFileID() {
        return bibFileID;
    }

    public String getCslFileID() {
        return cslFileID;
    }

    public String getTemplateID() {
        return templateID;
    }

    @Override
    public String getIdentificationSequence() {
        return clientID+bibFileID+cslFileID+templateID+((hasErrors)?"YES":"NO");
    }
}