package microservice.model.processor;

import global.identifiers.EntryIdentifier;
import global.identifiers.FileType;
import global.logging.Log;
import global.logging.LogLevel;
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

    private static final IValidator<File> CSL_VALIDATOR = new CslValidator();
    private static final IValidator<File> TEMPLATE_VALIDATOR = new TemplateValidator();

    private static final String DEFAULT_CSL_RESOURCE_NAME = "default.csl";
    private static final String DEFAULT_TEMPLATE_RESOURCE_NAME = "default_template.html";

    private static String DEFAULT_CSL_CONTENT = "";
    private static String DEFAULT_TEMPLATE_CONTENT = "";

    private HashMap<FileType, String> fileIdentifiers;

    static {
        initDefaults();
    }

    private static void initDefaults() {
        DEFAULT_CSL_CONTENT = getResourceContent(DEFAULT_CSL_RESOURCE_NAME);
        DEFAULT_TEMPLATE_CONTENT = getResourceContent(DEFAULT_TEMPLATE_RESOURCE_NAME);
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
        this.fileIdentifiers = createFileIdentifiersFromIEntry(toConvert);



        final String mdString = "--- \nbibliography: " + fileIdentifiers.get(FileType.BIB) + "\nnocite: \"@*\" \n...";

        final ArrayList<String> cslFilesToUse, templatesToUse;
        cslFilesToUse = new ArrayList<>(correctUserFileLists(toConvert.getCslFiles(), FileType.CSL));
        templatesToUse = new ArrayList<>(correctUserFileLists(toConvert.getTemplates(), FileType.TEMPLATE));

        writeFile(FileType.BIB, fileIdentifiers.get(FileType.BIB), toConvert.getContent().getBytes());
        writeFile(FileType.MD, fileIdentifiers.get(FileType.MD), mdString.getBytes());

        for (int i = 0; i < cslFilesToUse.size(); i++) {
            File currentCslFile = new File(writeFile(FileType.CSL, fileIdentifiers.get(FileType.CSL), cslFilesToUse.get(i).getBytes()).toString());

            if (!CSL_VALIDATOR.validate(currentCslFile)) {
                //invalid csl-file
            }
            for (int j = 0; j < templatesToUse.size(); j++) {
                File currentTemplate = new File(writeFile(FileType.TEMPLATE, fileIdentifiers.get(FileType.TEMPLATE), templatesToUse.get(j).getBytes()).toString());

                if (!TEMPLATE_VALIDATOR.validate(currentTemplate)) {
                    //invalid template
                }



            }

        }

        try {


        } catch (Exception e) {


        }
        finally {

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

}
