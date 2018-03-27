package de.hpi.bpt.scylla.plugin.batch;

/**
 * @project scylla
 * @author bjoern on 26.03.18
 */

// This class holds the execution types a batch cluster can have
public enum BatchClusterExecutionType {
    PARALLEL, SEQUENTIAL_TASKBASED, SEQUENTIAL_CASEBASED
}
