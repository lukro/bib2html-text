package client.model;

/**
 * Created by daan on 1/15/17.
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
