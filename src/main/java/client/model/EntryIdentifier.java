package client.model;

/**
 * @author Maximilian Schirm (denkbares GmbH)
 * @created 05.12.2016
 *
 * Identifies an Entry precisely
 */

public class EntryIdentifier {

    private final String clientID, bibFileId, cslFileId, templateId;
    private boolean hasErrors = false;

    public EntryIdentifier(String clientID, String bibFileId, String cslFileId, String templateId) {
        this.clientID = clientID;
        this.bibFileId = bibFileId;
        this.cslFileId = cslFileId;
        this.templateId = templateId;
    }

    public boolean isHasErrors() {
        return hasErrors;
    }

    public void setHasErrors(boolean hasErrors) {
        this.hasErrors = hasErrors;
    }

    public String getClientID() {
        return clientID;
    }

    public String getBibFileId() {
        return bibFileId;
    }

    public String getCslFileId() {
        return cslFileId;
    }

    public String getTemplateId() {
        return templateId;
    }
}
