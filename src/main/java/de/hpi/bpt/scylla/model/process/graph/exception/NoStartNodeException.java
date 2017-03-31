package de.hpi.bpt.scylla.model.process.graph.exception;

/**
 * Thrown if no start node exists in the graph.
 * 
 * @author Tsun Yin Wong
 */
public class NoStartNodeException extends Exception {

    private static final long serialVersionUID = 6136360442962188682L;

    /**
     * Constructor.
     * 
     * @param message
     *            message of exception
     */
    public NoStartNodeException(String message) {
        super(message);
    }
}
