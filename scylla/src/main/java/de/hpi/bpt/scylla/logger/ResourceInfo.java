package de.hpi.bpt.scylla.logger;

import de.hpi.bpt.scylla.simulation.ProcessInstance;

/**
 * Container class for description of state changes of resource instances.
 * 
 * @author Tsun Yin Wong
 *
 */
public class ResourceInfo {

    private long timestamp; // in seconds, relative to start of simulation
    private ResourceStatus transition;
    private ProcessInstance processInstance;
    private int nodeId;

    /**
     * Constructor.
     * 
     * @param timestamp
     *            time relative to simulation start
     * @param transition
     *            transition of resource instance
     * @param processInstance
     *            process instance in which the resource instance is involved
     * @param nodeId
     *            identifier of the node at which the state of the resource instance changes
     */
    public ResourceInfo(long timestamp, ResourceStatus transition, ProcessInstance processInstance, int nodeId) {
        this.timestamp = timestamp;
        this.transition = transition;
        this.processInstance = processInstance;
        this.nodeId = nodeId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public ResourceStatus getTransition() {
        return transition;
    }

    public ProcessInstance getProcessInstance() {
        return processInstance;
    }

    public int getNodeId() {
        return nodeId;
    }
}
