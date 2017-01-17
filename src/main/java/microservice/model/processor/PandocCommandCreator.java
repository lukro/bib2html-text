package microservice.model.processor;

/**
 * Created by Maximilian, daan
 * on 29.12.2016.
 */
public class PandocCommandCreator {

    private final String defaultCommandString = "pandoc --filter=pandoc-citeproc --template=%1$s --csl=%2$s --standalone %3$s -o %4$s";
    private final String wrapperFileName, resultName, cslFileName, templateFileName;

    /*
     defaults: future work, expandability
      */
    private final String customDefaultCslName, customDefaultTemplateName;
    private final boolean useCustomDefaultCsl, useCustomDefaultTemplate;

    private final boolean usePandocDefaultTemplate, usePandocDefaultCsl;

    public static final String PANDOC_DEFAULT_TEMPLATE_NAME = "default.html";
    /*
    future work, expandability: maybe pandoc provides a default csl file in the future
     */
    public static final String PANDOC_DEFAULT_CSL_NAME = "";

    private PandocCommandCreator(PandocCommandCreatorBuilder builder) {

        this.wrapperFileName = builder.wrapperFileName;
        this.resultName = builder.resultName;
        this.cslFileName = builder.cslFileName;
        this.templateFileName = builder.templateFileName;

        this.customDefaultCslName = builder.defaultCslName;
        this.customDefaultTemplateName = builder.defaultTemplateName;
        this.useCustomDefaultCsl = builder.useCustomDefaultCsl;
        this.useCustomDefaultTemplate = builder.useCustomDefaultTemplate;

        this.usePandocDefaultTemplate = builder.usePandocDefaultTemplate;
        this.usePandocDefaultCsl = builder.usePandocDefaultCsl;
    }

    public static final class PandocCommandCreatorBuilder {

        private String wrapperFileName, resultName, cslFileName, templateFileName;
        /*
        defaults: future work, expandability
         */
        private String defaultCslName = "", defaultTemplateName = "";
        private boolean useCustomDefaultCsl = false, useCustomDefaultTemplate = false;

        private boolean usePandocDefaultTemplate = true;
        /*
        future work, expandability: maybe pandoc provides a default csl file in the future
         */
        private boolean usePandocDefaultCsl = false;

        public PandocCommandCreatorBuilder(String wrapperFileName, String resultName, String cslFileName, String templateFileName) {
            this.wrapperFileName = wrapperFileName;
            this.resultName = resultName;
            cslFileName(cslFileName);
            templateFileName(templateFileName);
        }

        public PandocCommandCreatorBuilder cslFileName(String cslFileName) {
            if (cslFileName.equals("")) {
                useCustomDefaultCsl = false;
                usePandocDefaultCsl = true;
            }
            this.cslFileName = cslFileName;
            return this;
        }

        public PandocCommandCreatorBuilder templateFileName(String templateFileName) {
            if (templateFileName.equals("")) {
                useCustomDefaultTemplate = false;
                usePandocDefaultTemplate = true;
            }
            this.templateFileName = templateFileName;
            return this;
        }

        public PandocCommandCreatorBuilder useCustomDefaultCsl(boolean useCustomDefaultCsl) {
            if (useCustomDefaultCsl == true)
                usePandocDefaultCsl = false;
            this.useCustomDefaultCsl = useCustomDefaultCsl;
            return this;
        }

        public PandocCommandCreatorBuilder useCustomDefaultTemplate(boolean useCustomDefaultTemplate) {
            if (useCustomDefaultTemplate == true)
                usePandocDefaultTemplate = false;
            this.useCustomDefaultTemplate = useCustomDefaultTemplate;
            return this;
        }

        public PandocCommandCreatorBuilder defaultCslName(String customDefaultCslName) {
            if (customDefaultCslName.equals("")) {
                useCustomDefaultCsl = false;
                usePandocDefaultCsl = true;
            }
            this.defaultCslName = customDefaultCslName;
            return this;
        }

        public PandocCommandCreatorBuilder defaultTemplateName(String customDefaultTemplateName) {
            if (customDefaultTemplateName.equals("")) {
                useCustomDefaultTemplate = false;
                usePandocDefaultTemplate = true;
            }
            this.defaultTemplateName = customDefaultTemplateName;
            return this;
        }

        public PandocCommandCreatorBuilder usePandocDefaultCsl(boolean usePandocDefaultCsl) {
            if (usePandocDefaultCsl == true)
                useCustomDefaultCsl = false;
            this.usePandocDefaultCsl = usePandocDefaultCsl;
            return this;
        }

        public PandocCommandCreatorBuilder usePandocDefaultTemplate(boolean usePandocDefaultTemplate) {
            if (usePandocDefaultTemplate == true)
                useCustomDefaultTemplate = false;
            this.usePandocDefaultTemplate = usePandocDefaultTemplate;
            return this;
        }

        public PandocCommandCreator build() {
            return new PandocCommandCreator(this);
        }
    }

    private String[] createStrings() throws IllegalArgumentException {
        String[] placeHolders = new String[4];
        //template
        if (useCustomDefaultTemplate)
            placeHolders[0] = customDefaultTemplateName;
        else if (usePandocDefaultTemplate)
            placeHolders[0] = PANDOC_DEFAULT_TEMPLATE_NAME;
        else
            placeHolders[0] = templateFileName;

        //csl:
        if (useCustomDefaultCsl)
            placeHolders[1] = customDefaultCslName;
        else if (usePandocDefaultCsl)
            placeHolders[1] = PANDOC_DEFAULT_CSL_NAME;
        else
            placeHolders[1] = cslFileName;

        //wrapper
        placeHolders[2] = wrapperFileName;
        //result
        placeHolders[3] = resultName;
        return placeHolders;
    }

    /**
     * Builds a String to execute pandoc on the platform with correct arguments.
     *
     * @return the command String
     */
    String buildCommandString() {
        String[] placeHolders = createStrings();
        return String.format(defaultCommandString, placeHolders);
    }

}