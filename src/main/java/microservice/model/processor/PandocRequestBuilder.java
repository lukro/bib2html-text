package microservice.model.processor;

import global.identifiers.EntryIdentifier;

import java.nio.file.Path;

/**
 * Created by Maximilian on 29.12.2016.
 */
public class PandocRequestBuilder {

    EntryIdentifier entryIdentifier;
    //1 : Template, 2 : CSL, 3 : Wrapper, 4 : Output
    final static String defaultCommandString = "pandoc --filter=pandoc-citeproc %1$s  %2$s --standalone %3$s -o %4$s";
    private Path cslFile;
    private Path templateFile;
    private Path outputFile;
    private Path wrapperFile;
    private final String absolutePathToDefaultCsl;
    private final String absolutePathToOutputFile;
    private final String absolutePathtoDefaultTemplate;
    private boolean useDefaultCSL = true, useDefaultTemplate = true;

    public PandocRequestBuilder(Path AbsolutePathToDefaultCsl, Path absolutePathtoDefaultTemplate, Path absolutePathToOutputFile) {
        if (AbsolutePathToDefaultCsl == null)
            this.absolutePathToDefaultCsl = "";
        else
            this.absolutePathToDefaultCsl = AbsolutePathToDefaultCsl.toAbsolutePath().toString();

        if (absolutePathtoDefaultTemplate == null)
            this.absolutePathtoDefaultTemplate = "";
        else
            this.absolutePathtoDefaultTemplate = absolutePathtoDefaultTemplate.toAbsolutePath().toString();

        if (absolutePathToOutputFile == null)
            this.absolutePathToOutputFile = "absolutePathToOutputFile.html";
        else
            this.absolutePathToOutputFile = absolutePathToOutputFile.toAbsolutePath().toString();
    }

    PandocRequestBuilder csl(Path cslFile) {
        this.cslFile = cslFile;
        return this;
    }

    PandocRequestBuilder template(Path templateFile) {
        this.templateFile = templateFile;
        return this;
    }

    PandocRequestBuilder wrapper(Path wrapperFile) {
        this.wrapperFile = wrapperFile;
        return this;
    }

    PandocRequestBuilder outputFile(Path outputFile) {
        this.outputFile = outputFile;
        return this;
    }

    PandocRequestBuilder setUseDefaultCSL(boolean use) {
        useDefaultCSL = use;
        return this;
    }

    PandocRequestBuilder setUseDefaultTemplate(boolean use) {
        useDefaultTemplate = use;
        return this;
    }

    private String[] createStrings() throws IllegalArgumentException {
        String[] placeHolders = new String[4];

        //Template
        placeHolders[0] = (templateFile == null) ? ((useDefaultTemplate) ? absolutePathtoDefaultTemplate : "") : templateFile.toAbsolutePath().toString();
        //CSL
        placeHolders[1] = (cslFile == null) ? ((useDefaultCSL) ? absolutePathToDefaultCsl : "") : cslFile.toAbsolutePath().toString();
        //Wrapper
        if (wrapperFile == null)
            throw new IllegalArgumentException("Cannot create a pandoc request without a valid wrapper. Aborted.");
        placeHolders[2] = wrapperFile.toAbsolutePath().toString();
        //Output
        placeHolders[3] = (outputFile == null) ? absolutePathToOutputFile : outputFile.toAbsolutePath().toString();

        return placeHolders;
    }


    /**
     * Builds a String to execute pandoc on the platform with correct arguments.
     *
     * @return The command String.
     * @throws IllegalArgumentException Thrown if no valid wrapper file was passed.
     */
    String buildCommandString() throws IllegalArgumentException {
        String[] placeHolders = createStrings();
        return String.format(defaultCommandString, placeHolders);
    }
}