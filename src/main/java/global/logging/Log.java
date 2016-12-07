package global.logging;

import java.io.OutputStream;
import java.io.PrintStream;

/**
 * @author Maximilian Schirm (denkbares GmbH)
 * @created 07.12.2016
 */

public class Log {

    private static final Log INSTANCE = new Log();
    private static LogLevel minimumRequiredLevel = LogLevel.LOW;
    private PrintStream outputStream;

    public Log() {
        this.outputStream = System.out;
    }

    public void log(String message){
        log(message, LogLevel.INFO);
    }

    public void log(String message, LogLevel level){
        int ordLev = level.ordinal();
        int maxLev = minimumRequiredLevel.ordinal();

        if(ordLev <= maxLev)
            outputStream.println(level + ": " + message);
    }

    public static Log getInstance() {
        return INSTANCE;
    }

    public static LogLevel getMinimumRequiredLevel() {
        return minimumRequiredLevel;
    }

    public static void setMinimumRequiredLevel(LogLevel minimumRequiredLevel) {
        Log.minimumRequiredLevel = minimumRequiredLevel;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(PrintStream outputStream) {
        this.outputStream = outputStream;
    }
}