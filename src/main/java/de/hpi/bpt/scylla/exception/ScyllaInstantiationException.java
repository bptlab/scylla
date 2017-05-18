package de.hpi.bpt.scylla.exception;

/**
 * Thrown if there is an exception in the instantiation phase.
 */
public class ScyllaInstantiationException extends Exception {

    private static final long serialVersionUID = 1731327942249058670L;

    public ScyllaInstantiationException(String message) {
        super(message);
    }
}
