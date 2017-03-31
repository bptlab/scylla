package de.hpi.bpt.scylla.model.configuration;

import java.util.Map;

/**
 * Container class which describes a resource type.
 * 
 * @author Tsun Yin Wong
 *
 */
public class ResourceReference {

    private String resourceId;
    private int amount;
    private Map<String, String> assignmentDefinition;

    /**
     * Constructor.
     * 
     * @param resourceId
     *            resource type
     * @param amount
     *            number of resource instances
     * @param assignmentDefinition
     *            map of resource attributes (e.g. priority definition)
     */
    public ResourceReference(String resourceId, int amount, Map<String, String> assignmentDefinition) {
        this.resourceId = resourceId;
        this.amount = amount;
        this.assignmentDefinition = assignmentDefinition;
    }

    public String getResourceId() {
        return resourceId;
    }

    public int getAmount() {
        return amount;
    }

    public Map<String, String> getAssignmentDefinition() {
        return assignmentDefinition;
    }
}
