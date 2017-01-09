package microservice.model.processor;

import global.model.IEntry;
import global.model.IPartialResult;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Maximilian, daan
 * created on 09.01.2017
 */
public class FreshEntryProcessor implements IEntryProcessor{

    private final static File workingDirectory = new File("\"", "ProcessorWorkingDirectory");
    private final static Path pathToDefaultCSL = ;
    private final static Path pathToDefaultTemplate = ;
    private final static Path pathToDefaultOutputFile = ;

    public FreshEntryProcessor(){
        //TODO: Copy default files to the respective paths
        //ie. defaultCSLFromRessources.copyTo(pathToDefaultCSL) etc.
    }

    @Override
    public List<IPartialResult> processEntry(IEntry toConvert) {
        Collection<String> commands = buildPandocCommands(toConvert);
    }

    private static Collection<String> buildPandocCommands(IEntry toConvert) {
        Collection<String> returner = new ArrayList<>();

        PandocRequestBuilder pandocCommandBuilder = new PandocRequestBuilder(pathToDefaultCSL, pathToDefaultTemplate, pathToDefaultOutputFile);
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
