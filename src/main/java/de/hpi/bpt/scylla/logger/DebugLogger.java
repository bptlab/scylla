package de.hpi.bpt.scylla.logger;

/**
 * General class for logging occurences during simulation.
 * 
 * @author Tsun Yin Wong
 *
 */
public class DebugLogger {
	
	public static boolean allowDebugLogging = true;

    /**
     * Logs general messages.
     * 
     * @param message
     *            the message
     */
    public static void log(String message) {
       if(allowDebugLogging)System.out.println("[DEBUG] "+message);
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
