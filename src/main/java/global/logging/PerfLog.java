package global.logging;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Maximilian
 * created on 23.01.2017
 */
public class PerfLog {

    private static final PerfLog INSTANCE = new PerfLog();
    private static final String DEFAULT_KEY = "defaultPerformanceLog";
    private static final char CSV_SEPERATOR = ';';
    private Map<String, LogContent> loggingKeyToLogContentMap;
    private File outputDirectory;
    private boolean loggingActive = false;

    private class LogContent{
        private boolean hasBeenChanged = true;
        private StringBuilder content = new StringBuilder();

        public LogContent(){

        }

        public LogContent(String content){
            this.content.append(content);
        }

        public void appendContent(String toAppend){
            content.append(toAppend);

            setHasBeenChanged(true);
        }

        public void overwriteContent(String newContent){
            content = new StringBuilder();
            content.append(newContent);

            setHasBeenChanged(true);
        }

        public boolean hasBeenChanged() {
            return hasBeenChanged;
        }

        private void setHasBeenChanged(boolean hasBeenChanged) {
            this.hasBeenChanged = hasBeenChanged;
        }

        @Override
        public String toString(){
            return content.toString();
        }
    }

    public PerfLog() {
        this.outputDirectory = new File(System.getProperty("user.home"));
        loggingKeyToLogContentMap = new HashMap<>();
    }

    public void setLoggingActive(boolean active){
        loggingActive = active;
    }

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
        loggingKeyToLogContentMap.forEach((key, content) -> content.setHasBeenChanged(true));
    }

    private void writeContentLog(String loggingKey, String toAddToLog, boolean append){
        LogContent theContent = loggingKeyToLogContentMap.get(loggingKey);
        //Create a new entry?
        if(theContent == null)
            theContent = new LogContent();

        //Add/overwrite the new content to the log
        if(append)
            theContent.appendContent(toAddToLog + CSV_SEPERATOR);
        else
            theContent.overwriteContent(toAddToLog);

        //Put the changed entry back into our map
        loggingKeyToLogContentMap.put(loggingKey, theContent);
    }

    private void writeToFiles() {
        if(loggingActive) loggingKeyToLogContentMap.forEach((currentKey, currentEntry) -> {
            if(currentEntry.hasBeenChanged){
                try{
                    String outputFileContent = currentKey + CSV_SEPERATOR + currentEntry.toString();
                    File writeFile = new File(outputDirectory, currentKey + ".csv");
                    Files.write(writeFile.toPath(),outputFileContent.getBytes());
                } catch (IOException e){
                    Log.log("Failed to write the log file for the key " + currentKey + "!", e);
                }
            }
        });
    }

    //Public methods (all static)

    /**
     * Will write the changed log entries to their files.
     */
    public static void writeChanges() {
        if(INSTANCE!=null)
            INSTANCE.writeToFiles();
    }

    /**
     * Logs some String to the log.
     * NOTE : Not yet written. Call writeChanges() for writing to files.
     *
     * @param loggingKey
     * @param logContent
     * @param append
     */
    public static void log(String loggingKey, String logContent, boolean append){
        if(INSTANCE!=null)
            INSTANCE.writeContentLog(loggingKey, logContent, append);
    }

    /**
     * Sets the PerfLog's output directory
     *
     * @param directory
     * @return
     */
    public static boolean setLoggingDirectory(File directory){
        if(!directory.exists() || INSTANCE == null)
            return false;
        INSTANCE.setOutputDirectory(directory);
        return true;
    }

    /**
     * Logs some String to the log using the append method (default method, generates csl output)
     *
     * @param loggingKey
     * @param logContent
     */
    public static void log(String loggingKey, String logContent){
        log(loggingKey, logContent, true);
    }

    /**
     * Logs some String to the log using the DEFAULT_KEY identifier and the append method
     * NOTE: Should be used only for general information and log outputs.
     *
     * @param logContent
     */
    public static void log(String logContent){
        log(DEFAULT_KEY, logContent);
    }

}