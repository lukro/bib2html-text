package microservice.model.processor;

import global.identifiers.EntryIdentifier;
import global.identifiers.FileType;
import global.model.IEntry;
import global.model.IPartialResult;
import microservice.model.validator.CSLDummyValidator;
import microservice.model.validator.TemplateValidator;
import microservice.model.validator.IValidator;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


/**
 * @author Maximilian Schirm, daan
 *         created 27.12.2016
 */
public class DefaultEntryProcessor implements IEntryProcessor {

    private static final IValidator<String> CSL_VALIDATOR = new CSLDummyValidator();
    private static final IValidator<String> TEMPLATE_VALIDATOR = new TemplateValidator();

    private final Path pathToDefaultCsl = DefaultFilesInformation.pathToDefaultCsl;
    private final Path pathToDefaultTemplate = DefaultFilesInformation.pathToDefaultTemplate;

    public DefaultEntryProcessor() {
    }

    private static class DefaultFilesInformation {

        private static final Path CUSTOMIZED_WORKING_PATH = FileSystems.getDefault().getPath("").toAbsolutePath();  //is current working path

        private static final String CUSTOMIZED_CSL_NAME = "default.csl";
        private static final String CUSTOMIZED_TEMPLATE_NAME = "default_template.html";

        private static final Path CUSTOMIZED_PATH_TO_DEFAULT_CSL = Paths.get(CUSTOMIZED_WORKING_PATH.toAbsolutePath().toString(), CUSTOMIZED_CSL_NAME);
        private static final Path CUSTOMIZED_PATH_TO_DEFAULT_TEMPLATE = Paths.get(CUSTOMIZED_WORKING_PATH.toAbsolutePath().toString(), CUSTOMIZED_TEMPLATE_NAME);

        private static final String RESOURCE_CSL_FILE_NAME = "default.csl";
        private static final String RESOURCE_TEMPLATE_NAME = "default_template.html";

        private static Path pathToDefaultCsl;
        private static Path pathToDefaultTemplate;

        static {
            Path cslSource, cslTarget, templateSource, templateTarget;
            try {
                cslSource = CUSTOMIZED_PATH_TO_DEFAULT_CSL;
                cslTarget = Paths.get(CUSTOMIZED_WORKING_PATH.toString(), RESOURCE_CSL_FILE_NAME);

                templateSource = CUSTOMIZED_PATH_TO_DEFAULT_TEMPLATE;
                templateTarget = Paths.get(CUSTOMIZED_WORKING_PATH.toString(), RESOURCE_TEMPLATE_NAME);

                Files.copy(cslSource, cslTarget);
                Files.copy(templateSource, templateTarget);

                pathToDefaultCsl = cslTarget;
                pathToDefaultTemplate = templateTarget;

            } catch (IOException e) {
                System.out.println("couldn't read customized default.csl/default_template.html from filesystem. Default files from resources will be used.");
//                Log.log("couldn't read customized default.csl/default_template.html from filesystem. Default files from resources will be used.", LogLevel.ERROR);
                try {
                    cslSource = Paths.get(DefaultFilesInformation.class.getClassLoader().getResource(RESOURCE_CSL_FILE_NAME).getPath());
                    cslTarget = Paths.get(CUSTOMIZED_WORKING_PATH.toString(), RESOURCE_CSL_FILE_NAME);

                    templateSource = Paths.get(DefaultFilesInformation.class.getClassLoader().getResource(RESOURCE_TEMPLATE_NAME).getPath());
                    templateTarget = Paths.get(CUSTOMIZED_WORKING_PATH.toString(), RESOURCE_TEMPLATE_NAME);

                    Files.copy(cslSource, cslTarget);
                    Files.copy(templateSource, templateTarget);

                    pathToDefaultCsl = cslTarget;
                    pathToDefaultTemplate = templateTarget;

                } catch (IOException e1) {
                    System.out.println("couldn't read default.csl/default_template.html from resources.");
//                    Log.log("couldn't read default.csl/default_template.html from resources.", LogLevel.SEVERE);
                }
            }
        }
    }

    private HashMap<FileType, String> createFileIdentifiersFromIEntry(IEntry iEntry) {
        HashMap<FileType, String> result = new HashMap<>();
        String hashCode = Integer.toString(Math.abs(iEntry.hashCode()));
        result.put(FileType.BIB, hashCode + ".bib");
        result.put(FileType.CSL, hashCode + ".csl");
        result.put(FileType.TEMPLATE, hashCode + "_template.html");
        result.put(FileType.MD, hashCode + ".md");
        result.put(FileType.RESULT, hashCode + "_result.html");
        return result;
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
//        try {
        return null;
//            return convertEntry(toConvert);
//        } catch (InterruptedException e) {
//            //TODO : Handle Properly.
//            Log.log("MicroService got interrupted, returned null", e);
//            return null;
//        }
    }
}
