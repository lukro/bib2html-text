package microservice;

import global.identifiers.ResultIdentifier;
import global.model.DefaultEntry;
import global.model.DefaultPartialResult;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

/**
 * @author Maximilian Schirm (denkbares GmbH)
 * @created 05.12.2016
 */
//TODO: Extends ConnectionPoint, Implements Consumer
public class MicroService{
    //TODO : Fill...
    private final String routingKey;
    private final String registerQueueName;

    public MicroService(String routingKey, String registerQueueName) throws IOException, TimeoutException {
        super();
        this.routingKey = routingKey;
        this.registerQueueName = registerQueueName;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public String getRegisterQueueName() {
        return registerQueueName;
    }

    private DefaultPartialResult convertEntry(DefaultEntry toConvert){
        //TODO : Replace Dummy code
        String
                conversionResult = toConvert.getContent(),
                fauxClientId = "0123456789",
                bibFileId = "b",
                cslFileId = "c",
                templateId = "0";
        //Simulating 20% error rate
        boolean hasErrors = (Math.random() > 0.8)?true:false;
        ResultIdentifier partialIdentifier = new ResultIdentifier(fauxClientId, bibFileId, cslFileId, templateId, hasErrors);
        return new DefaultPartialResult(conversionResult, partialIdentifier, "HTML");
    }

    private boolean[] validateTempaltes(DefaultEntry defaultEntry, HashSet<byte[]> templateFiles){
        boolean[] output = new boolean[templateFiles.size()];
        for(int i = 0; i < templateFiles.size(); i++){
            //TODO : Use pandocDoWork(..) to retrieve execution success.
            //Simulating 30% error rate
            int conversionResultState = (Math.random() > 0.7)?1:0;
            output[i] =  (conversionResultState > 0)?false:true;
        }
        return output;
    }

    private static int pandocDoWork(File directory, String cslName, String wrapperName) throws IOException, InterruptedException {
        Objects.requireNonNull(directory);
        Objects.requireNonNull(cslName);
        Objects.requireNonNull(wrapperName);

        File cslFile = new File(directory, cslName);
        File wrapperFile = new File(directory, wrapperName);

        if(!cslFile.exists() || !wrapperFile.exists())
            throw new IllegalArgumentException("A file with that name might not exist!");

        String command = "pandoc --filter=pandoc-citeproc --csl="+cslName+".csl --standalone " + wrapperName + ".md -o "+cslName+".HTML";
        return Runtime.getRuntime().exec(command, null, directory).waitFor();
    }
}
