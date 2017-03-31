package de.hpi.bpt.scylla.exception;

/**
 * Thrown if there is an exception during runtime.
 */
public class ScyllaRuntimeException extends Exception {

    private static final long serialVersionUID = 1731327942249058670L;

    public ScyllaRuntimeException(String message) {
        super(message); // TODO consider making ScyllaRuntimeException extend RuntimeException instead of Exception
    }
}
