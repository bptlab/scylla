package de.hpi.bpt.scylla.model.process.node;

/**
 * BPMN message flows (betweeen tasks and data objects).
 * 
 * @author Tsun Yin Wong
 *
 */
public class MessageFlow {

    private String id;
    private String sourceRef;
    private String targetRef;

    /**
     * Constructor
     * 
     * @param id
     *            identifier of message flow
     * @param sourceRef
     *            identifier of source of message flow
     * @param targetRef
     *            identifier of target of message flow
     */
    public MessageFlow(String id, String sourceRef, String targetRef) {
        this.id = id;
        this.sourceRef = sourceRef;
        this.targetRef = targetRef;
    }

    public String getId() {
        return id;
    }

    public String getSourceRef() {
        return sourceRef;
    }

    public String getTargetRef() {
        return targetRef;
    }

}
