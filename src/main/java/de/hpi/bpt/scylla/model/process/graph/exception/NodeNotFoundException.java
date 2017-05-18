package de.hpi.bpt.scylla.model.process.graph.exception;

/**
 * Thrown if a node does not exist in the graph.
 * 
 * @author Tsun Yin Wong
 */
public class NodeNotFoundException extends Exception {

    private static final long serialVersionUID = 8173404040139159609L;

    /**
     * Constructor.
     * 
     * @param message
     *            message of exception
     */
    public NodeNotFoundException(String message) {
        super(message);
    }
}