package de.hpi.bpt.scylla.logger;

import java.util.Set;

/**
 * 
 * Container class for logging business process-specific occurences.
 * 
 * @author Tsun Yin Wong
 *
 */
public class ProcessNodeInfo {

    private String processScopeNodeId;
    private String source;
    private long timestamp;
    private String taskName;
    private Set<String> resources;
    private ProcessNodeTransitionType transition;

    /**
     * Constructor.
     * 
     * @param processScopeNodeId
     *            process node identifier which is unique on all levels of a process
     * @param source
     *            the DesmoJ event which logs the business process-specific occurence
     * @param timestamp
     *            time relative to simulation start
     * @param nodeName
     *            display name of the node
     * @param resources
     *            names of resource instances involved
     * @param transition
     *            transition of node
     */
    public ProcessNodeInfo(String processScopeNodeId, String source, long timestamp, String nodeName,
            Set<String> resources, ProcessNodeTransitionType transition) {
        this.processScopeNodeId = processScopeNodeId;
        this.source = source;
        this.timestamp = timestamp;
        this.taskName = nodeName;
        this.resources = resources;
        this.transition = transition;
    }

    public String getProcessScopeNodeId() {
        return processScopeNodeId;
    }

    public String getSource() {
        return source;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getTaskName() {
        return taskName;
    }

    public Set<String> getResources() {
        return resources;
    }

    public ProcessNodeTransitionType getTransition() {
        return transition;
    }

}
