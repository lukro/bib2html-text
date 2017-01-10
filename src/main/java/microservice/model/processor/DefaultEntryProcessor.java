package microservice.model.processor;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
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

    private static final Path WORKING_DIRECTORY_ROOT = Paths.get(System.getProperty("user.dir"));
    private static final String WORKING_SUB_DIRECTORY_NAME = "working_dir";
    private static Path WORKING_DIRECTORY = Paths.get(
            WORKING_DIRECTORY_ROOT.toAbsolutePath().toString(), WORKING_SUB_DIRECTORY_NAME);

    private static final String DEFAULT_CSL_RESOURCE_NAME = "default.csl";
    private static final String DEFAULT_TEMPLATE_RESOURCE_NAME = "default_template.html";
    private static final String DEFAULT_OUTPUT_FILE_NAME = "result.html";

    private static final Path PATH_TO_DEFAULT_CSL = Paths.get(DefaultEntryProcessor.class.getClassLoader().getResource(DEFAULT_CSL_RESOURCE_NAME).getFile());
    private static final Path PATH_TO_DEFAULT_TEMPLATE = Paths.get(DefaultEntryProcessor.class.getClassLoader().getResource(DEFAULT_TEMPLATE_RESOURCE_NAME).getFile());
    private static final Path PATH_TO_DEFAULT_OUTPUT_FILE = Paths.get(
            WORKING_DIRECTORY.toAbsolutePath().toString(), DEFAULT_OUTPUT_FILE_NAME);

    private static final IValidator<File> CSL_VALIDATOR = new CslValidator();
    private static final IValidator<File> TEMPLATE_VALIDATOR = new TemplateValidator();

    private HashMap<FileType, String> fileIdentifiers;

    private static String DEFAULT_CSL_CONTENT = "";
    private static String DEFAULT_TEMPLATE_CONTENT = "";

    static {
        initDefaults();
    }

    private static void initDefaults() {
        if (Files.exists(WORKING_DIRECTORY) && Files.isDirectory(WORKING_DIRECTORY))
            Log.log("working directory already exists.", LogLevel.INFO);
        else {
            new File(WORKING_DIRECTORY.toAbsolutePath().toString()).mkdir();
        }

        final Path defaultCslTarget =
                Paths.get(WORKING_DIRECTORY.toAbsolutePath().toString(), DEFAULT_CSL_RESOURCE_NAME);
        final Path defaultTemplateTarget =
                Paths.get(WORKING_DIRECTORY.toAbsolutePath().toString(), DEFAULT_TEMPLATE_RESOURCE_NAME);

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
        try {
            DEFAULT_CSL_CONTENT = new String(Files.readAllBytes(defaultCslTarget));
            DEFAULT_TEMPLATE_CONTENT = new String(Files.readAllBytes(defaultTemplateTarget));
        } catch (IOException e) {
            Log.log("couldn't read defaults content.", e);
        }


    }

    public DefaultEntryProcessor() {
    }

//    private static int pandocDoWork(String cslName, String templateName, String wrapperName, EntryIdentifier entryIdentifier) throws IOException, InterruptedException {
//        Objects.requireNonNull(cslName);
//        Objects.requireNonNull(wrapperName);
//
//        File cslFile = new File(cslName);
//        File wrapperFile = new File(wrapperName);
//        File template = new File(templateName);
//
//        if (!cslFile.exists() || !wrapperFile.exists())
//            throw new IllegalArgumentException("A file with that name might not exist!");
//
//        String command = "pandoc --filter=pandoc-citeproc --template " + templateName + " --csl " + cslName + " --standalone " + wrapperName + " -o " + entryIdentifier.getBibFileIndex() + "_result.html";
//        System.out.println(command);
//        Process p = Runtime.getRuntime().exec(command, null);
//        BufferedReader input = new BufferedReader(new
//                InputStreamReader(p.getInputStream()));
//        String line;
//        while ((line = input.readLine()) != null) {
//            System.out.println(line);
//        }
//        return p.waitFor();
//    }

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

    @Override
    public List<IPartialResult> processEntry(IEntry toConvert) {
        fileIdentifiers = createFileIdentifiersFromIEntry(toConvert);

        ArrayList<IPartialResult> result = new ArrayList<>();
        final Path pathToWrapper;

        final String mdString = "--- \nbibliography: " + fileIdentifiers.get(FileType.BIB) + "\nnocite: \"@*\" \n...";
        try {
            pathToWrapper = Files.write(Paths.get(WORKING_DIRECTORY.toAbsolutePath().toString(), fileIdentifiers.get(FileType.MD)), mdString.getBytes());
            Files.write(Paths.get(WORKING_DIRECTORY.toAbsolutePath().toString(), fileIdentifiers.get(FileType.BIB)), toConvert.getContent().getBytes());
        } catch (IOException e) {
            Log.log("couldn't write required file(s) in working dir");
            //TODO: handle error, return list with correct amount of partials flagged with error
            return null;
        }

        final HashMap<String, PartialResultIdentifier> commands = buildPandocCommands(toConvert, pathToWrapper);

        IPartialResult currentPartialResult = null;

        for (Map.Entry currentEntry : commands.entrySet()) {

            final int currentCslIndex = commands.get(currentEntry).getCslFileIndex();
            final int currentTemplateIndex = commands.get(currentEntry).getTemplateFileIndex();

            String cslContentToWrite, templateContentToWrite;
            if (currentCslIndex == -1) {
                cslContentToWrite = DEFAULT_CSL_CONTENT;
            } else {
                cslContentToWrite = toConvert.getCslFiles().get(currentCslIndex);
            }
            if (currentTemplateIndex == -1) {
                templateContentToWrite = DEFAULT_TEMPLATE_CONTENT;
            } else {
                templateContentToWrite = toConvert.getTemplates().get(currentTemplateIndex);
            }

            Path cslWritePath = Paths.get(WORKING_DIRECTORY.toAbsolutePath().toString(), fileIdentifiers.get(FileType.CSL));
            Path templateWritePath = Paths.get(WORKING_DIRECTORY.toAbsolutePath().toString(), fileIdentifiers.get(FileType.TEMPLATE));

            try {
                Files.write(cslWritePath, cslContentToWrite.getBytes());
                Files.write(templateWritePath, templateContentToWrite.getBytes());
            } catch (IOException e) {
                Log.log("Failed to do some shit (write csl/template)", e);
            }

            String currentCommand = (String) currentEntry.getKey();
            try {
                Runtime.getRuntime().exec(currentCommand);

                byte[] currentResultBytes = Files.readAllBytes(new File(DEFAULT_OUTPUT_FILE_NAME).toPath());
                String currentResultContent = new String(currentResultBytes);
                currentPartialResult = new DefaultPartialResult(currentResultContent, commands.get(currentCommand));

            } catch (IOException e) {
                Log.log("error while executing pandoc command: '" + currentCommand + "'.", e);

                String errorContent = System.lineSeparator() + "ERROR" + System.lineSeparator();
                currentPartialResult = new DefaultPartialResult(errorContent, commands.get(currentCommand));

                continue;

            } finally {
                result.add(currentPartialResult);

            }
        }
        return result;
    }

    private HashMap<String, PartialResultIdentifier> buildPandocCommands(IEntry toConvert, Path pathToWrapper) {
        HashMap<String, PartialResultIdentifier> result = new HashMap<>();

        final EntryIdentifier entryIdentifier = toConvert.getEntryIdentifier();

        PandocRequestBuilder pandocCommandBuilder =
                new PandocRequestBuilder(
                        PATH_TO_DEFAULT_CSL, PATH_TO_DEFAULT_TEMPLATE, PATH_TO_DEFAULT_OUTPUT_FILE)
                        .wrapper(pathToWrapper);

        int startIndexCsl, startIndexTemplate;
        if (toConvert.getCslFiles().size() == 0)
            startIndexCsl = -1;
        else
            startIndexCsl = 0;
        if (toConvert.getTemplates().size() == 0)
            startIndexTemplate = -1;
        else
            startIndexTemplate = 0;

        for (int cslFileIndex = startIndexCsl; cslFileIndex < toConvert.getCslFiles().size(); cslFileIndex++) {
            final Path pathToCslFile;

            if (cslFileIndex == -1)
                pathToCslFile = null;
            else
                pathToCslFile = Paths.get(
                        WORKING_DIRECTORY.toAbsolutePath().toString(), fileIdentifiers.get(FileType.CSL));

            for (int templateFileIndex = startIndexTemplate; templateFileIndex < toConvert.getTemplates().size(); templateFileIndex++) {
                final Path pathToTemplate;

                if (templateFileIndex == -1)
                    pathToTemplate = null;
                else
                    pathToTemplate = Paths.get(
                            WORKING_DIRECTORY.toAbsolutePath().toString(), fileIdentifiers.get(FileType.TEMPLATE));

                pandocCommandBuilder
                        .csl(pathToCslFile)
                        .template(pathToTemplate);

                PartialResultIdentifier currentPartialResultIdentifier =
                        new PartialResultIdentifier(entryIdentifier, cslFileIndex, templateFileIndex);

                result.put(pandocCommandBuilder.buildCommandString(), currentPartialResultIdentifier);
            }
        }
        return result;
    }


}
