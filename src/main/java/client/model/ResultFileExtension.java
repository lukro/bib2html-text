package client.model;

/**
 * Created by daan on 1/15/17.
 * Enum for File Extension of the result file. It is used for display of the file type in the GUI
 * and for saving the result file in the proper file type.
 */
public enum ResultFileExtension {

    HTML(".html"),
    TXT(".txt");

    private final String resultFileExtension;

    ResultFileExtension(String resultFileExtension) {
        this.resultFileExtension = resultFileExtension;
    }

    @Override
    public String toString() {
        return resultFileExtension;
    }
}
