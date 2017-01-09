package microservice.model.processor;

import global.identifiers.EntryIdentifier;

import java.io.IOException;
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
    private final String defaultCslFile;
    private final String defaultOutputFile;
    private final String defaultTemplateFile;
    private boolean useDefaultCSL = true, useDefaultTemplate = true;

    public PandocRequestBuilder(Path defaultCslFile, Path defaultTemplateFile, Path defaultOutputFile){
        if(defaultCslFile == null)
            this.defaultCslFile = "";
        else
            this.defaultCslFile = defaultCslFile.toAbsolutePath().toString();

        if(defaultTemplateFile == null)
            this.defaultTemplateFile = "";
        else
            this.defaultTemplateFile = defaultTemplateFile.toAbsolutePath().toString();

        if(defaultOutputFile == null)
            this.defaultOutputFile = "defaultOutputFile.html";
        else
            this.defaultOutputFile = defaultOutputFile.toAbsolutePath().toString();
    }

    PandocRequestBuilder csl(Path cslFile){
        this.cslFile = cslFile;
        return this;
    }

    PandocRequestBuilder template(Path templateFile){
        this.templateFile = templateFile;
        return this;
    }

    PandocRequestBuilder wrapper(Path wrapperFile) {
        this.wrapperFile = wrapperFile;
        return this;
    }

    PandocRequestBuilder outputFile(Path outputFile){
        this.outputFile = outputFile;
        return this;
    }

    PandocRequestBuilder setUseDefaultCSL(boolean use){
        useDefaultCSL = use;
        return this;
    }

    PandocRequestBuilder setUseDefaultTemplate(boolean use){
        useDefaultTemplate = use;
        return this;
    }

    private String[] createStrings() throws IllegalArgumentException {
        String[] placeHolders = new String[4];

        //Template
        placeHolders[0] = (templateFile==null)?((useDefaultTemplate)?defaultTemplateFile:""):templateFile.toAbsolutePath().toString();
        //CSL
        placeHolders[1] = (cslFile==null)?((useDefaultCSL)?defaultCslFile:""):cslFile.toAbsolutePath().toString();
        //Wrapper
        if(wrapperFile == null) throw new IllegalArgumentException("Cannot create a pandoc request without a valid wrapper. Aborted.");
        placeHolders[2] = wrapperFile.toAbsolutePath().toString();
        //Output
        placeHolders[3] = (outputFile == null)?defaultOutputFile:outputFile.toAbsolutePath().toString();

        return placeHolders;
    }


    /**
     * Builds a String to execute pandoc on the platform with correct arguments.
     *
     * @return The command String.
     * @throws IllegalArgumentException Thrown if no valid wrapper file was passed.
     */
    String buildCommandString() throws IllegalArgumentException{
        String[] placeHolders = createStrings();
        return String.format(defaultCommandString, placeHolders);
    }
}