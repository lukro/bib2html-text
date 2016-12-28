package microservice.model.processor;

import global.identifiers.EntryIdentifier;
import global.identifiers.FileType;
import global.identifiers.PartialResultIdentifier;
import global.logging.Log;
import global.model.DefaultPartialResult;
import global.model.IEntry;
import global.model.IPartialResult;
import microservice.model.validator.CSLDummyValidator;
import microservice.model.validator.TemplateValidator;
import microservice.model.validator.Validator;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author Maximilian Schirm
 *         created 27.12.2016
 */

public class DefaultEntryProcessor implements EntryProcessor {

    private static final Validator<String> CSL_VALIDATOR = new CSLDummyValidator();
    private static final Validator<String> TEMPLATE_VALIDATOR = new TemplateValidator();

    public DefaultEntryProcessor() {
    }

    private List<IPartialResult> convertEntry(IEntry toConvert) throws InterruptedException {
        List<IPartialResult> result = new ArrayList<>();
        final HashMap<FileType, String> fileIdentifiers = createFileIdentifiersFromIEntry(toConvert);

        int expectedAmountOfPartials, finishedPartialsCounter;
        expectedAmountOfPartials = finishedPartialsCounter = 0;

        //Could be realized with files instead of Strings. TODO : Choose defaults and move into resources.
        //TODO: check defaultCsl and defaultTemplate and INIT!
        final String defaultCsl, defaultTemplate;
        defaultCsl = "";
        defaultTemplate = "";

        try {
            //create .bib-file with entry-content
            Files.write(Paths.get(fileIdentifiers.get(FileType.BIB)), toConvert.getContent().getBytes());

            //declare csl/template-lists we want to use
            final ArrayList<String> cslFilesToUse, templatesToUse;

            //BEGIN: check if entry contains at least 1 .csl-file AND/OR at least 1 template
            if (toConvert.getCslFiles().size() == 0) {
                //use defaultCslFile
                cslFilesToUse = new ArrayList<>(Arrays.asList(defaultCsl));
            } else
                cslFilesToUse = new ArrayList<>(toConvert.getCslFiles());
            if (toConvert.getTemplates().size() == 0) {
                //use defaultTemplate
                templatesToUse = new ArrayList<>(Arrays.asList(defaultTemplate));
            } else
                templatesToUse = new ArrayList<>(toConvert.getTemplates());
            //END: check if entry contains at least 1 .csl-file AND/OR at least 1 template

            final String mdString = "--- \nbibliography: " + fileIdentifiers.get(FileType.BIB) + ".bib\nnocite: \"@*\" \n...";

            expectedAmountOfPartials = cslFilesToUse.size() * templatesToUse.size();
            //TODO : dis is buggy. why do we even save it into a map mapping type -> String in the first place?
            final boolean[] validatedTemplates = {TEMPLATE_VALIDATOR.validate(fileIdentifiers.get(FileType.TEMPLATE))};
            boolean firstInvalidTemplateReplacedByDefaultTemplate = false;

            //BEGIN: iterate over all .csl-files and templates and do pandoc work
            for (int cslFileIndex = 0; cslFileIndex < cslFilesToUse.size(); cslFileIndex++) {
                Files.write(Paths.get(fileIdentifiers.get(FileType.CSL)), cslFilesToUse.get(cslFileIndex).getBytes());

                for (int templateFileIndex = 0; templateFileIndex < templatesToUse.size(); templateFileIndex++) {
//                    if (!validatedTemplates[templateFileIndex]) {
//                        //currentTemplate is invalid
//                        if (!firstInvalidTemplateReplacedByDefaultTemplate) {
//                            Files.write(Paths.get(fileIdentifiers.get(FileType.TEMPLATE)), defaultTemplate.getBytes());
//                            firstInvalidTemplateReplacedByDefaultTemplate = true;
//                        } else {
//                            //don't use invalid template and don't replace it, defaultTemplate is already in use
//                            expectedAmountOfPartials--;
//                            break;
//                        }
//                    } else {
                    //currentTemplate is valid
                    Files.write(Paths.get(fileIdentifiers.get(FileType.TEMPLATE)), templatesToUse.get(templateFileIndex).getBytes());

                    Files.write(Paths.get(fileIdentifiers.get(FileType.MD)), mdString.getBytes());
                    pandocDoWork(
                            fileIdentifiers.get(FileType.CSL),
                            fileIdentifiers.get(FileType.TEMPLATE),
                            fileIdentifiers.get(FileType.MD),
                            toConvert.getEntryIdentifier()
                    );
                    final byte[] convertedContentEncoded = Files.readAllBytes(Paths.get(toConvert.hashCode() + fileIdentifiers.get(FileType.RESULT)));
                    final String convertedContent = new String(convertedContentEncoded);
                    final IPartialResult currentPartialResult = new DefaultPartialResult(
                            convertedContent,
                            new PartialResultIdentifier(toConvert.getEntryIdentifier(), cslFileIndex, templateFileIndex)
                    );
                    result.add(currentPartialResult);
                    finishedPartialsCounter++;
                }
            }
            //END: iterate over all .csl-files and templates and do pandoc work
            //delete all temporary files except resultFile
            for (Map.Entry currentEntryInHashMap : fileIdentifiers.entrySet()) {
                if (currentEntryInHashMap.getKey() != FileType.RESULT)
                    Files.delete(Paths.get(Integer.toString(toConvert.hashCode()) + currentEntryInHashMap.getValue()));
            }
        } catch (IOException e) {
            final int amountOfPartialsWithErrors = expectedAmountOfPartials - finishedPartialsCounter;
            //TODO: reduce expected result-size in partialresult-collector by 'amountOfPartialsWithErrors' <-- How are we supposed to do that? Return # of errors?
            Log.log("Error in microservice.convertEntry(). " + finishedPartialsCounter + "/ " + expectedAmountOfPartials + " Partials succesfully created.", e);
        }
        //TODO: check Acknowledgement
//        channel.basicAck(currentDeliveryTag, false);
        return result;
    }

    //TODO : This will fail at multiple CSL/TEMPLATES for an entry. FIX!!
    private HashMap<FileType, String> createFileIdentifiersFromIEntry(IEntry iEntry) {
        HashMap<FileType, String> result = new HashMap<>();
        String hashCodeAsString = Integer.toString(iEntry.getEntryIdentifier().hashCode());
        result.put(FileType.BIB, hashCodeAsString + ".bib");
        result.put(FileType.CSL, hashCodeAsString + ".csl");
        result.put(FileType.TEMPLATE, hashCodeAsString + "_template.html");
        result.put(FileType.MD, hashCodeAsString + ".md");
        result.put(FileType.RESULT, hashCodeAsString + "_result.html");
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
        try {
            return convertEntry(toConvert);
        } catch (InterruptedException e) {
            //TODO : Handle Properly.
            Log.log("MicroService got interrupted, returned null", e);
            return null;
        }
    }
}
