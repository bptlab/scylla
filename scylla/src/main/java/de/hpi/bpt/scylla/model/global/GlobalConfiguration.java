package de.hpi.bpt.scylla.model.global;

import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import de.hpi.bpt.scylla.model.SimulationInput;
import de.hpi.bpt.scylla.model.global.resource.Resource;
import de.hpi.bpt.scylla.plugin_type.parser.EventOrderType;

/**
 * Represents all simulation parameters which are necessary for conducting the simulation, across all involved business
 * processes.
 * 
 * @author Tsun Yin Wong
 */
public class GlobalConfiguration extends SimulationInput {

    private ZoneId zoneId;
    private Long randomSeed;
    private Map<String, Resource> resources;
    private List<EventOrderType> resourceAssignmentOrder;
    private String fileNameWithoutExtension;

    /**
     * Constructor.
     * 
     * @param id
     *            identifier of global configuration
     * @param zoneId
     *            time zone identifier (for BPS logging)
     * @param randomSeed
     *            random seed of simulation
     * @param resources
     *            map of resource type name to {@link de.hpi.bpt.scylla.model.global.resource.Resource} container with
     *            further information
     * @param resourceAssignmentOrder
     *            definition how tasks waiting for free resources are ordered in queues
     */
    public GlobalConfiguration(String id, ZoneId zoneId, Long randomSeed, Map<String, Resource> resources,
            List<EventOrderType> resourceAssignmentOrder) {
        super(id);
        this.zoneId = zoneId;
        this.randomSeed = randomSeed;
        this.resources = resources;
        this.resourceAssignmentOrder = resourceAssignmentOrder;
    }

    public ZoneId getZoneId() {
        return zoneId;
    }

    public Long getRandomSeed() {
        return randomSeed;
    }

    public Map<String, Resource> getResources() {
        return resources;
    }

    public List<EventOrderType> getResourceAssignmentOrder() {
        return resourceAssignmentOrder;
    }

    public String getFileNameWithoutExtension() {
        return fileNameWithoutExtension;
    }

    public void setFileNameWithoutExtension(String bpmnFileNameWithoutExtension) {
        this.fileNameWithoutExtension = bpmnFileNameWithoutExtension;
    }

}
