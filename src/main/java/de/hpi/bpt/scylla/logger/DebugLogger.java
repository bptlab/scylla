package de.hpi.bpt.scylla.logger;

/**
 * General class for logging occurences during simulation.
 * 
 * @author Tsun Yin Wong
 *
 */
public class DebugLogger {

    /**
     * Logs general messages.
     * 
     * @param message
     *            the message
     */
    public static void log(String message) {
        System.out.println(message);
    }

    /**
     * Logs exceptional messages.
     * 
     * @param message
     *            the message
     */
    public static void error(String message) {
        System.err.println(message);
    }

}
