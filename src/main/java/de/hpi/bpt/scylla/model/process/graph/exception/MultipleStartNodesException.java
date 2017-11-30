package de.hpi.bpt.scylla.model.process.graph.exception;

/**
 * Thrown if multiple start nodes exist in the graph.
 * 
 * @author Tsun Yin Wong
 */
public class MultipleStartNodesException extends Exception {

    private static final long serialVersionUID = 459444349305003377L;

    /**
     * Constructor.
     * 
     * @param message
     *            message of exception
     */
    public MultipleStartNodesException(String message) {
        super(message);
    }
}
