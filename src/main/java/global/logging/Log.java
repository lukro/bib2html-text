package global.logging;

import java.io.OutputStream;
import java.io.PrintStream;

/**
 * @author Maximilian Schirm
 * @created 07.12.2016
 */

public class Log {

    private static final Log INSTANCE = new Log();
    private static LogLevel minimumRequiredLevel = LogLevel.LOW;
    private PrintStream outputStream;

    public Log() {
        this.outputStream = System.out;
    }

    /**
     * Logs the message and the exception at the ERROR level - if the minimum level permits it.
     *
     * @param message
     * @param t
     */
    public static void log(String message, Throwable t){
        log(message, LogLevel.ERROR);
        log(t.getMessage(), LogLevel.ERROR);
    }

    /**
     * Logs the message at the default (INFO) level  - if the minimum level permits it.
     *
     * @param message
     */
    public static void log(String message){
        if(INSTANCE != null)
            INSTANCE.printOut(message, LogLevel.INFO);
    }

    /**
     * Logs the message at the supplied level - if the minimum level permits it.
     *
     * @param message
     * @param level
     */
    public static void log(String message, LogLevel level){
        if(INSTANCE != null)
            INSTANCE.printOut(message, level);
    }

    /**
     * Prints the String if the level is at least at the minimum required level.
     *
     * @param message
     * @param level
     */
    private void printOut(String message, LogLevel level){
        int ordLev = level.ordinal();
        int minLev = minimumRequiredLevel.ordinal();

        if(minLev <= ordLev)
            outputStream.println(level + ": " + message);
    }

    public static Log getInstance() {
        return INSTANCE;
    }

    public static LogLevel getMinimumRequiredLevel() {
        return minimumRequiredLevel;
    }

    private void setMinimumRequiredLevel(LogLevel minimumRequiredLevel){
        this.minimumRequiredLevel = minimumRequiredLevel;
    }

    public static void alterMinimumRequiredLevel(LogLevel minimumRequiredLevel) {
        if(INSTANCE != null)
            INSTANCE.setMinimumRequiredLevel(minimumRequiredLevel);
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(PrintStream outputStream) {
        this.outputStream = outputStream;
    }
}