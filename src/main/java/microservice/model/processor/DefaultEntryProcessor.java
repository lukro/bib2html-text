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

    private static final Path WORKING_DIRECTORY_ROOT = Paths.get(System.getProperty("user.dir"));
    private static final String WORKING_SUB_DIRECTORY_NAME = "working_dir";
    private static Path WORKING_DIRECTORY = Paths.get(
            WORKING_DIRECTORY_ROOT.toAbsolutePath().toString(), WORKING_SUB_DIRECTORY_NAME + "/");

    private static final String DEFAULT_CSL_RESOURCE_NAME = "default.csl";
    private static final String DEFAULT_TEMPLATE_RESOURCE_NAME = "default_template.html";
    private static final String DEFAULT_OUTPUT_FILE_NAME = "result.html";

    private static final Path PATH_TO_DEFAULT_CSL = Paths.get(DefaultEntryProcessor.class.getClassLoader().getResource(DEFAULT_CSL_RESOURCE_NAME).getFile());
    private static final Path PATH_TO_DEFAULT_TEMPLATE = Paths.get(DefaultEntryProcessor.class.getClassLoader().getResource(DEFAULT_TEMPLATE_RESOURCE_NAME).getFile());
    private static final Path PATH_TO_DEFAULT_OUTPUT_FILE = Paths.get(
            WORKING_DIRECTORY.toAbsolutePath().toString(), DEFAULT_OUTPUT_FILE_NAME);

    private static final IValidator<File> CSL_VALIDATOR = new CslValidator();
    private static final IValidator<File> TEMPLATE_VALIDATOR = new TemplateValidator();

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
//        DEFAULT_CSL_CONTENT = getResourceContent(DEFAULT_CSL_RESOURCE_NAME);
//        DEFAULT_TEMPLATE_CONTENT = getResourceContent(DEFAULT_TEMPLATE_RESOURCE_NAME);
    }

    public DefaultEntryProcessor() {
    }

    private static int pandocDoWork(String cslName, String templateName, String wrapperName, EntryIdentifier entryIdentifier) throws IOException, InterruptedException {
        Objects.requireNonNull(cslName);
        Objects.requireNonNull(wrapperName);

        File cslFile = new File(cslName);
        File wrapperFile = new File(wrapperName);
        File template = new File(templateName);

        if (!cslFile.exists() || !wrapperFile.exists())
            throw new IllegalArgumentException("A file with that name might not exist!");

        String command = "pandoc --filter=pandoc-citeproc --template " + templateName + " --csl " + cslName + " --standalone " + wrapperName + " -o " + entryIdentifier.getBibFileIndex() + "_result.html";
        System.out.println(command);
        Process p = Runtime.getRuntime().exec(command, null);
        BufferedReader input = new BufferedReader(new
                InputStreamReader(p.getInputStream()));
        String line;
        while ((line = input.readLine()) != null) {
            System.out.println(line);
        }
        return p.waitFor();
    }

    @Override
    public List<IPartialResult> processEntry(IEntry toConvert) {
        List<IPartialResult> result = new ArrayList<>();
        final HashMap<FileType, String> fileIdentifiers = createFileIdentifiersFromIEntry(toConvert);

        final EntryIdentifier currentEntryIdentifier = toConvert.getEntryIdentifier();

//        final String mdString = "--- \nbibliography: " + fileIdentifiers.get(FileType.BIB) + "\nnocite: \"@*\" \n...";
        final String mdString = "--- \nbibliography: " + toConvert.getContent() + "\nnocite: \"@*\" \n...";


        final ArrayList<String> cslFilesToUse, templatesToUse;
        cslFilesToUse = new ArrayList<>(correctUserFileLists(toConvert.getCslFiles(), FileType.CSL));
        templatesToUse = new ArrayList<>(correctUserFileLists(toConvert.getTemplates(), FileType.TEMPLATE));

        writeFile(FileType.BIB, fileIdentifiers.get(FileType.BIB), toConvert.getContent().getBytes());
        Path pathToWrapperFile = writeFile(FileType.MD, fileIdentifiers.get(FileType.MD), mdString.getBytes());

        for (int cslFileIndex = 0; cslFileIndex < cslFilesToUse.size(); cslFileIndex++) {
            Path pathToCurrentCslFile = writeFile(FileType.CSL, fileIdentifiers.get(FileType.CSL), cslFilesToUse.get(cslFileIndex).getBytes());
            File currentCslFile = new File(pathToCurrentCslFile.toString());

            if (!CSL_VALIDATOR.validate(currentCslFile)) {
                //invalid csl-file
            }
            for (int templateIndex = 0; templateIndex < templatesToUse.size(); templateIndex++) {
                Path pathToCurrentTemplate = writeFile(FileType.TEMPLATE, fileIdentifiers.get(FileType.TEMPLATE), templatesToUse.get(templateIndex).getBytes());
                File currentTemplate = new File(pathToCurrentTemplate.toString());

                if (!TEMPLATE_VALIDATOR.validate(currentTemplate)) {
                    //invalid template
                }

                final PandocRequestBuilder pandocCommandBuilder = new PandocRequestBuilder(
                        PATH_TO_DEFAULT_CSL,
                        PATH_TO_DEFAULT_TEMPLATE,
                        Paths.get(fileIdentifiers.get(FileType.RESULT)))
                        .csl(null)
                        .template(pathToCurrentTemplate)
                        .wrapper(pathToWrapperFile)
                        .outputFile(Paths.get(fileIdentifiers.get(FileType.RESULT)))
                        .setUseDefaultCSL(false)
                        .setUseDefaultTemplate(false);

                final String pandocCommandString = pandocCommandBuilder.buildCommandString();

                IPartialResult currentPartialResult;

                try {
                    Process p = Runtime.getRuntime().exec(pandocCommandString, null);

                    PartialResultIdentifier currentPartialIdentifier =
                            new PartialResultIdentifier(currentEntryIdentifier, cslFileIndex, templateIndex);

                    final byte[] convertedContentEncoded = Files.readAllBytes(Paths.get(fileIdentifiers.get(FileType.RESULT)));
                    final String convertedContent = new String(convertedContentEncoded);

                    currentPartialResult = new DefaultPartialResult(convertedContent, currentPartialIdentifier);

                    result.add(currentPartialResult);

                } catch (IOException e) {
                    //TODO: build error flagged partial & continue
                    continue;
                } finally {

                }
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

    private static ArrayList<String> correctUserFileLists(ArrayList<String> fileList, FileType fileType) {
        if (fileList.size() != 0)
            return fileList;
        if (fileType == FileType.CSL)
            return new ArrayList<>(Arrays.asList(DEFAULT_CSL_CONTENT));
        else if (fileType == FileType.TEMPLATE)
            return new ArrayList<>(Arrays.asList(DEFAULT_TEMPLATE_CONTENT));
        return new ArrayList<String>();
    }

    private static String getResourceContent(String resourceFileName) {
        try {
            String pathToResource = DefaultEntryProcessor.class.getClassLoader().getResource(resourceFileName).getFile();
//            System.out.println(pathToResource);
            try {
                return new String
                        (Files.readAllBytes(Paths.get(pathToResource)));
            } catch (IOException e) {
                Log.log("couldn't read resource file '" + pathToResource + "'", LogLevel.ERROR);
                return null;
            }
        } catch (NullPointerException e) {
            Log.log("resource doesn't exist.", LogLevel.ERROR);
            return null;
        }
    }

    private static Path writeFile(FileType fileType, String fileName, byte[] content) {
        Path result = null;
        try {
            result = Files.write(Paths.get(fileName), content);
        } catch (IOException e) {
            Log.log("couldn't write file '" + fileName + "' to working directory.", LogLevel.ERROR);
        }
        return result;
    }

//    private static IPartialResult createPartialResultFrom
}
