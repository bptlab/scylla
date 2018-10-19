package de.hpi.bpt.scylla.exception;

/**
 * Thrown if there is an exception in the validation phase.
 */
public class ScyllaValidationException extends Exception {

    private static final long serialVersionUID = 1731327942249058670L;

    public ScyllaValidationException(String message) {
        super(message);
    }
    
    public ScyllaValidationException(String message, Throwable cause) {
    	super(message, cause);
    }
}
