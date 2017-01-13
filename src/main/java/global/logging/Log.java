package global.logging;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author Maximilian Schirm
 * @created 07.12.2016
 */

public class Log {

    private static final Log INSTANCE = new Log();
    private static LogLevel minimumRequiredLevel = LogLevel.LOW;
    private OutputStream outputStream;

    private Log() {
        this.outputStream = System.out;
    }

    /**
     * Logs the message and the exception at the ERROR level - if the minimum level permits it.
     *
     * @param message
     * @param t
     */
    public static void log(String message, Throwable t) {
        log(message, LogLevel.ERROR);
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        String exceptionAsString = t.getMessage() + "\n" + sw.toString();
        log(exceptionAsString, LogLevel.ERROR);
    }

    /**
     * Logs the message at the default (INFO) level  - if the minimum level permits it.
     *
     * @param message
     */
    public static void log(String message) {
        if (INSTANCE != null)
            INSTANCE.printOut(message, LogLevel.INFO);
    }

    /**
     * Logs the message at the supplied level - if the minimum level permits it.
     *
     * @param message
     * @param level
     */
    public static void log(String message, LogLevel level) {
        if (INSTANCE != null)
            INSTANCE.printOut(message, level);
    }

    /**
     * Prints the String if the level is at least at the minimum required level.
     *
     * @param message
     * @param level
     */
    private void printOut(String message, LogLevel level) {
        Thread thread = new Thread(() -> {
            synchronized (outputStream) {
                int ordLev = level.ordinal();
                int minLev = minimumRequiredLevel.ordinal();

                if (minLev <= ordLev) {
                    try {
                        for (char c : message.toCharArray())
                            outputStream.write(c);
                        outputStream.write("\n".getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.run();
    }

    public static LogLevel getMinimumRequiredLevel() {
        if (INSTANCE != null)
            return INSTANCE.minimumRequiredLevel;
        return minimumRequiredLevel;
    }

    private void setMinimumRequiredLevel(LogLevel minimumRequiredLevel) {
        this.minimumRequiredLevel = minimumRequiredLevel;
    }

    public static void alterMinimumRequiredLevel(LogLevel minimumRequiredLevel) {
        if (INSTANCE != null)
            INSTANCE.setMinimumRequiredLevel(minimumRequiredLevel);
    }

    public static void alterOutputStream(OutputStream newOutputStream) {
        if (INSTANCE != null)
            INSTANCE.setOutputStream(newOutputStream);
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }
}