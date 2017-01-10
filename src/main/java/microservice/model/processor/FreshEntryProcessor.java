package microservice.model.processor;

import global.logging.Log;
import global.logging.LogLevel;
import global.model.IEntry;
import global.model.IPartialResult;
import microservice.model.validator.CslValidator;
import microservice.model.validator.IValidator;
import microservice.model.validator.TemplateValidator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Maximilian, daan
 * created on 09.01.2017
 */
public class FreshEntryProcessor
        implements IEntryProcessor
{

    private static final File WORKING_DIRECTORY = new File("\"", "ProcessorWorkingDirectory");

    private static final Path WORKING_DIRECTORY = null;
    private static final String WORKING_SUB_DIRECTORTY_NAME = "";
    private static final String DEFAULT_CSL_RESOURCE_NAME = "default.csl";
    private static final String DEFAULT_TEMPLATE_RESOURCE_NAME = "default_template.html";
    private static final String DEFAULT_OUTPUT_FILE_NAME = "result.html";
    private static final Path PATH_TO_DEFAULT_CSL = Paths.get(DefaultEntryProcessor.class.getClassLoader().getResource(DEFAULT_CSL_RESOURCE_NAME).getFile());
    private static final Path PATH_TO_DEFAULT_TEMPLATE = Paths.get(DefaultEntryProcessor.class.getClassLoader().getResource(DEFAULT_TEMPLATE_RESOURCE_NAME).getFile());
    private static final Path PATH_TO_DEFAULT_OUTPUT_FILE = Paths.get(DEFAULT_OUTPUT_FILE_NAME);

    private static final IValidator<File> CSL_VALIDATOR = new CslValidator();
    private static final IValidator<File> TEMPLATE_VALIDATOR = new TemplateValidator();

    static {
        initDefaults();
    }

    private static void initDefaults() {
        final Path defaultCslTarget =
                Paths.get(WORKING_DIRECTORY.toAbsolutePath().toString()
                        + WORKING_SUB_DIRECTORTY_NAME + DEFAULT_CSL_RESOURCE_NAME);
        final Path defaultTemplateTarget =
                Paths.get(WORKING_DIRECTORY.toAbsolutePath().toString()
                        + WORKING_SUB_DIRECTORTY_NAME + DEFAULT_TEMPLATE_RESOURCE_NAME);
        try {
            Files.copy(PATH_TO_DEFAULT_CSL, defaultCslTarget);
        } catch (IOException e) {
            Log.log("couldn't init default csl.", LogLevel.ERROR);
        }
        try {
            Files.copy(PATH_TO_DEFAULT_TEMPLATE, defaultTemplateTarget);
        } catch (IOException e) {
            Log.log("couldn't init default template.", LogLevel.ERROR);
        }
    }

    public FreshEntryProcessor() {
    }

    @Override
    public List<IPartialResult> processEntry(IEntry toConvert) {
        Collection<String> commands = buildPandocCommands(toConvert);



    }

    private static Collection<String> buildPandocCommands(IEntry toConvert) {
        Collection<String> returner = new ArrayList<>();
        PandocRequestBuilder pandocCommandBuilder =
                new PandocRequestBuilder(
                        PATH_TO_DEFAULT_CSL, PATH_TO_DEFAULT_TEMPLATE, PATH_TO_DEFAULT_OUTPUT_FILE);

        toConvert.getCslFiles().forEach(cslFile -> {

            toConvert.getTemplates().forEach(templateFile -> {


                //TODO: create paths.
                //TODO: check if we have to use the defaults (ie. has no CSL / templates)
                returner.add(pandocCommandBuilder.csl(cslFile).template(templateFile).buildCommandString());
            });
        });

        return returner;
    }
}
