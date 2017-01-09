package microservice.model.processor;

import global.identifiers.EntryIdentifier;
import global.identifiers.FileType;
import global.logging.Log;
import global.model.IEntry;
import global.model.IPartialResult;
import microservice.model.validator.CSLDummyValidator;
import microservice.model.validator.TemplateValidator;
import microservice.model.validator.IValidator;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


/**
 * @author Maximilian Schirm, daan
 *         created 27.12.2016
 */
public class DefaultEntryProcessor implements IEntryProcessor {

    private static final IValidator<String> CSL_VALIDATOR = new CSLDummyValidator();
    private static final IValidator<String> TEMPLATE_VALIDATOR = new TemplateValidator();

    private HashMap<FileType, String> fileIdentifiers;

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

        final String mdString = "--- \nbibliography: " + fileIdentifiers.get(FileType.BIB) + ".bib\nnocite: \"@*\" \n...";

        final ArrayList<String> cslFilesToUse, templatesToUse;

        cslFilesToUse = new ArrayList<>(correctUserFileLists(toConvert.getCslFiles(), FileType.CSL));
        templatesToUse = new ArrayList<>(correctUserFileLists(toConvert.getTemplates(), FileType.TEMPLATE));

        writeUserFiles(cslFilesToUse, FileType.CSL);
        writeUserFiles(templatesToUse, FileType.TEMPLATE);

        try {
            Files.write(Paths.get(fileIdentifiers.get(FileType.BIB)), toConvert.getContent().getBytes());
            Files.write(Paths.get(fileIdentifiers.get(FileType.MD)), mdString.getBytes());



        } catch (IOException e) {


        } finally {

        }


        return result;
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

    private ArrayList<String> correctUserFileLists(ArrayList<String> fileList, FileType fileType) {
        if (fileList.size() != 0)
            return fileList;
        if (fileType == FileType.CSL)
            return new ArrayList<>(Arrays.asList("DEFAULT CSL"));
        return new ArrayList<>(Arrays.asList("DEFAULT TEMPLATE"));
    }

    private void writeUserFiles(ArrayList<String> fileList, FileType fileType) {
        String fileName = "";
        for (int i = 0; i < fileList.size(); i++) {
            try {
                fileName = i + "_" + fileIdentifiers.get(fileType);
                Files.write(Paths.get(fileName), fileList.get(i).getBytes());
            } catch (IOException e) {
                Log.log("failed to write file '" + fileName + "' to filesystem.");
            }
        }
    }

}
