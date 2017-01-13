package de.hpi.bpt.scylla.plugin.eventorder_basic;

import java.util.Set;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.model.configuration.ResourceReference;
import de.hpi.bpt.scylla.plugin_type.parser.EventOrderType;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import de.hpi.bpt.scylla.simulation.event.ScyllaEvent;

/**
 * 
 * if no priority provided -> set to lowest priority
 * 
 * if both events do not have priority -> skip
 */
public class EventOrderPriority extends EventOrderType {

    private EventOrderPriority instance;

    public EventOrderPriority() {
        if (instance == null) {
            instance = this;
        }
    }

    @Override
    public String getName() {
        return "priority";
    }

    @Override
    public int compare(String resourceId, ScyllaEvent e1, ScyllaEvent e2) throws ScyllaRuntimeException {
        SimulationModel model = (SimulationModel) e1.getModel();
        if (!model.equals((SimulationModel) e2.getModel())) {
            throw new ScyllaRuntimeException("New event for queue is attached to another simulation model.");
        }

        Integer priorityOfFirstEvent = null;
        Set<ResourceReference> resourceRefsOfFirstEvent = e1.getDesmojObjects().getSimulationConfiguration()
                .getResourceReferenceSet(e1.getNodeId());
        for (ResourceReference resourceRef : resourceRefsOfFirstEvent) {
            if (resourceId.equals(resourceRef.getResourceId())) {
                String priorityString = resourceRef.getAssignmentDefinition().get("priority");
                if (priorityString == null) {
                    priorityOfFirstEvent = -1;
                }
                else {
                    priorityOfFirstEvent = Integer.parseInt(resourceRef.getAssignmentDefinition().get("priority"));
                }
            }
        }
        Integer priorityOfSecondEvent = null;
        Set<ResourceReference> resourceRefsOfSecondEvent = e2.getDesmojObjects().getSimulationConfiguration()
                .getResourceReferenceSet(e2.getNodeId());
        for (ResourceReference resourceRef : resourceRefsOfSecondEvent) {
            if (resourceId.equals(resourceRef.getResourceId())) {
                String priorityString = resourceRef.getAssignmentDefinition().get("priority");
                if (priorityString == null) {
                    priorityOfSecondEvent = -1;
                }
                else {
                    priorityOfSecondEvent = Integer.parseInt(resourceRef.getAssignmentDefinition().get("priority"));
                }
            }
        }
        // if (priorityOfFirstEvent == null) {
        // DebugLogger.error(e1.getName() + " does not define priority value for resource assignment.");
        // return 0;
        // }
        // if (priorityOfSecondEvent == null) {
        // DebugLogger.error(e2.getName() + " does not define priority value for resource assignment.");
        // return 0;
        // }
        return priorityOfSecondEvent - priorityOfFirstEvent; // higher value is sorted first
    }

}
