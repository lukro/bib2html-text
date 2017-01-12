package microservice.model.processor;

import global.identifiers.EntryIdentifier;
import global.identifiers.FileType;
import global.identifiers.PartialResultIdentifier;
import global.logging.Log;
import global.logging.LogLevel;
import global.model.DefaultPartialResult;
import global.model.IEntry;
import global.model.IPartialResult;
import microservice.model.validator.CslValidator;
import microservice.model.validator.TemplateValidator;
import microservice.model.validator.IValidator;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


/**
 * @author Maximilian Schirm, daan
 *         created 27.12.2016
 */
public class DefaultEntryProcessor implements IEntryProcessor {

    private static final Path WORKING_DIRECTORY = Paths.get(System.getProperty("user.dir"));

    /*
    defaults: future work, expandability
     */
    private static final String CUSTOM_DEFAULT_CSL_NAME = "custom_default.csl";
    private static final String CUSTOM_DEFAULT_TEMPLATE_NAME = "custom_default_template.html";
//    private static final Path PATH_TO_CUSTOM_DEFAULT_CSL = Paths.get(DefaultEntryProcessor.class.getClassLoader().getResource(CUSTOM_DEFAULT_CSL_NAME).getFile());
//    private static final Path PATH_TO_CUSTOM_DEFAULT_TEMPLATE = Paths.get(DefaultEntryProcessor.class.getClassLoader().getResource(CUSTOM_DEFAULT_TEMPLATE_NAME).getFile());
    private static String CUSTOM_DEFAULT_CSL_CONTENT;
    private static String CUSTOM_DEFAULT_TEMPLATE_CONTENT;

    private static final IValidator<File> CSL_VALIDATOR = new CslValidator();
    private static final IValidator<File> TEMPLATE_VALIDATOR = new TemplateValidator();

    private static final String FAILED_PARTIAL_ERROR_CONTENT = System.lineSeparator() + "ERROR!" + System.lineSeparator();

    private HashMap<FileType, String> fileIdentifiers;

    /*
   init defaults: future work, expandability
    */
    static {
        initCustomDefaults();
    }

    private static void initCustomDefaults() {
        final Path defaultCslTarget =
                Paths.get(WORKING_DIRECTORY.toAbsolutePath().toString(), CUSTOM_DEFAULT_CSL_NAME);
        final Path defaultTemplateTarget =
                Paths.get(WORKING_DIRECTORY.toAbsolutePath().toString(), CUSTOM_DEFAULT_TEMPLATE_NAME);

        copyCustomDefaultsToWorkingDir(defaultCslTarget, defaultTemplateTarget);

        try {
            CUSTOM_DEFAULT_CSL_CONTENT = new String(Files.readAllBytes(defaultCslTarget));
            CUSTOM_DEFAULT_TEMPLATE_CONTENT = new String(Files.readAllBytes(defaultTemplateTarget));
        } catch (IOException e) {
            Log.log("couldn't read defaults content.", LogLevel.ERROR);
        }
    }

    private static void copyCustomDefaultsToWorkingDir(Path defaultCslTarget, Path defaultTemplateTarget) {
//        try {
//            Files.copy(PATH_TO_CUSTOM_DEFAULT_CSL, defaultCslTarget);
//        } catch (IOException e) {
//            Log.log("couldn't init default csl.", LogLevel.ERROR);
//        }
//        try {
//            Files.copy(PATH_TO_CUSTOM_DEFAULT_TEMPLATE, defaultTemplateTarget);
//        } catch (IOException e) {
//            Log.log("couldn't init default template.", LogLevel.ERROR);
//        }
    }

    public DefaultEntryProcessor() {
    }

    @Override
    public List<IPartialResult> processEntry(IEntry toConvert) {
        fileIdentifiers = createFileIdentifiersFromIEntry(toConvert);

        ArrayList<IPartialResult> result = new ArrayList<>();

        final String wrapperFileName = fileIdentifiers.get(FileType.MD);
        final String bibFileName = fileIdentifiers.get(FileType.BIB);
        final String resultName = fileIdentifiers.get(FileType.RESULT);
        String cslFileName = fileIdentifiers.get(FileType.CSL);
        String templateName = fileIdentifiers.get(FileType.TEMPLATE);

        final String mdString = "--- \nbibliography: " + bibFileName + "\nnocite: \"@*\" \n...";
        try {
            Files.write(Paths.get(WORKING_DIRECTORY.toAbsolutePath().toString(), wrapperFileName), mdString.getBytes());
            Files.write(Paths.get(WORKING_DIRECTORY.toAbsolutePath().toString(), bibFileName), toConvert.getContent().getBytes());
        } catch (IOException e) {
            Log.log("couldn't write required file(s) in working dir");
            return handleAbortionCausedByMissingRequiredFiles(toConvert, toConvert.getAmountOfExpectedPartials());
        }

        final HashMap<String, PartialResultIdentifier> commands = buildPandocCommands(toConvert, wrapperFileName);

//        DEBUG: check commands
//        for (Map.Entry currentEntry : commands.entrySet()) {
//            System.out.println(((String) currentEntry.getKey()));
//        }

        IPartialResult currentPartialResult = null;

        for (Map.Entry currentEntry : commands.entrySet()) {

            PartialResultIdentifier currentPartialIdentifier = ((PartialResultIdentifier) currentEntry.getValue());

            final int currentCslIndex = currentPartialIdentifier.getCslFileIndex();
            final int currentTemplateIndex = currentPartialIdentifier.getTemplateFileIndex();

            if (currentCslIndex != -1) {
                String cslContentToWrite;
                cslContentToWrite = toConvert.getCslFiles().get(currentCslIndex);
                try {
                    Files.write(Paths.get(cslFileName), cslContentToWrite.getBytes());
                } catch (IOException e) {
                    Log.log("failed to write csl-file.", LogLevel.ERROR);
                }
            }

            if (currentTemplateIndex != -1) {
                String templateContentToWrite;
                templateContentToWrite = toConvert.getTemplates().get(currentTemplateIndex);
                try {
                    Files.write(Paths.get(templateName), templateContentToWrite.getBytes());
                } catch (IOException e) {
                    Log.log("failed to write template.", LogLevel.ERROR);
                }
            }

            String currentCommand = (String) currentEntry.getKey();

            try {
                Log.log("execute: '" + currentCommand + "'.", LogLevel.INFO);
                Process p = Runtime.getRuntime().exec(currentCommand);
                Log.log("pandoc terminal command executed.", LogLevel.INFO);
                try {
                    p.waitFor();
                } catch (InterruptedException e) {
                    Log.log("error while waiting for finishing pandoc terminal command.", LogLevel.ERROR);
                }
            } catch (IOException e) {
                Log.log("error while executing pandoc command: '" + currentCommand + "'.", e);
                currentPartialResult = createErrorPartial(currentPartialIdentifier);
                continue;
            }
            try {
                byte[] currentResultBytes = Files.readAllBytes(new File(resultName).toPath());
                String currentResultContent = new String(currentResultBytes);
                currentPartialResult = new DefaultPartialResult(currentResultContent, commands.get(currentCommand));
            } catch (IOException e) {
                Log.log("couldn't read result-file '" + resultName + "' from disk. ", LogLevel.ERROR);
                currentPartialResult = createErrorPartial(currentPartialIdentifier);
                continue;
            } finally {
                result.add(currentPartialResult);
                if (currentCslIndex != -1) {
                    try {
                        Files.delete(Paths.get(cslFileName));
                    } catch (IOException e) {
                        Log.log("couldn't delete csl-file.", LogLevel.ERROR);
                    }
                }
                if (currentTemplateIndex != -1) {
                    try {
                        Files.delete(Paths.get(templateName));
                    } catch (IOException e) {
                        Log.log("couldn't delete template-file.", LogLevel.ERROR);
                    }
                }
                try {
                    Files.delete(Paths.get(resultName));
                } catch (IOException e) {
                    Log.log("couldn't delete result.", LogLevel.ERROR);
                    continue;
                }
            }
        }
        try {
            Files.delete(Paths.get(bibFileName));
            Files.delete(Paths.get(wrapperFileName));
        } catch (IOException e) {
            Log.log("couldn't delete bib-file or wrapper.", LogLevel.ERROR);
        }
//        Log.log("finished convert method!", LogLevel.INFO);
        return result;
    }

    /**
     * builds all pandoc commands
     *
     * @param toConvert
     * @param wrapperFileName
     * @return a list with pandoc commands
     */
    private HashMap<String, PartialResultIdentifier> buildPandocCommands(IEntry toConvert, String wrapperFileName) {
        HashMap<String, PartialResultIdentifier> result = new HashMap<>();

        final EntryIdentifier entryIdentifier = toConvert.getEntryIdentifier();

        final String resultFileName = fileIdentifiers.get(FileType.RESULT);

        int startIndexCsl, startIndexTemplate;

        if (toConvert.getCslFiles().size() == 0) {
            startIndexCsl = -1;
        } else {
            startIndexCsl = 0;
        }

        if (toConvert.getTemplates().size() == 0) {
            startIndexTemplate = -1;
        } else {
            startIndexTemplate = 0;
        }

        for (int cslFileIndex = startIndexCsl; cslFileIndex < toConvert.getCslFiles().size(); cslFileIndex++) {
            final String cslFileName;

            if (cslFileIndex == -1) {
                cslFileName = PandocCommandCreator.PandocCommandCreatorBuilder.PANDOC_DEFAULT_CSL_NAME;
            } else {
                cslFileName = fileIdentifiers.get(FileType.CSL);
            }

            for (int templateFileIndex = startIndexTemplate; templateFileIndex < toConvert.getTemplates().size(); templateFileIndex++) {
                final String templateFileName;

                if (templateFileIndex == -1) {
                    templateFileName = PandocCommandCreator.PandocCommandCreatorBuilder.PANDOC_DEFAULT_TEMPLATE_NAME;
                } else {
                    templateFileName = fileIdentifiers.get(FileType.TEMPLATE);
                }

                PandocCommandCreator commandCreator =
                        new PandocCommandCreator.PandocCommandCreatorBuilder
                                (wrapperFileName, resultFileName, cslFileName, templateFileName)
                                .useCustomDefaultCsl(false)
                                .useCustomDefaultTemplate(false)
                                .defaultCslName(CUSTOM_DEFAULT_CSL_NAME)
                                .defaultTemplateName(CUSTOM_DEFAULT_TEMPLATE_NAME)
                                .usePandocDefaultCsl(false)
                                .usePandocDefaultTemplate(false)
                                .build();

                PartialResultIdentifier currentPartialResultIdentifier =
                        new PartialResultIdentifier(entryIdentifier, cslFileIndex, templateFileIndex);

                result.put(commandCreator.buildCommandString(), currentPartialResultIdentifier);
            }
        }
        return result;
    }

    private static HashMap<FileType, String> createFileIdentifiersFromIEntry(IEntry iEntry) {
        HashMap<FileType, String> result = new HashMap<>();
        String hashCode = Integer.toString(Math.abs(iEntry.hashCode()));
        result.put(FileType.BIB, hashCode + ".bib");
        result.put(FileType.CSL, hashCode + ".csl");
        result.put(FileType.TEMPLATE, hashCode + "_template.html");
        result.put(FileType.MD, hashCode + ".md");
        result.put(FileType.RESULT, hashCode + "_result.html");
        return result;
    }

    private static List<IPartialResult> handleAbortionCausedByMissingRequiredFiles(IEntry failedEntry, int expectedAmountOfPartials) {
        ArrayList<IPartialResult> result = new ArrayList<>();

        EntryIdentifier failedEntryIdentifier = failedEntry.getEntryIdentifier();

        IPartialResult currentErrorPartialResult;

        PartialResultIdentifier errorIdentifier =
                new PartialResultIdentifier
                        (failedEntryIdentifier, -1337, -1337);

        for (int i = 0; i < expectedAmountOfPartials; i++) {

            currentErrorPartialResult =
                    createErrorPartial(errorIdentifier);

            result.add(currentErrorPartialResult);
        }
        return result;
    }

    private static IPartialResult createErrorPartial(PartialResultIdentifier errorIdentifier) {
        errorIdentifier.setHasErrors(true);
        return new DefaultPartialResult(FAILED_PARTIAL_ERROR_CONTENT, errorIdentifier);
    }

}
