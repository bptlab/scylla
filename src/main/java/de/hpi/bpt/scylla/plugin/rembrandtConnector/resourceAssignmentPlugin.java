package de.hpi.bpt.scylla.plugin.rembrandtConnector;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import de.hpi.bpt.scylla.plugin_type.simulation.resource.ResourceAssignmentPluggable;
import de.hpi.bpt.scylla.simulation.ResourceObjectTuple;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import de.hpi.bpt.scylla.simulation.event.ScyllaEvent;

public class resourceAssignmentPlugin extends ResourceAssignmentPluggable {
    // Ask rembrandt to assign a ResourceInstance and convert it to scylla readable assignment --> see function in QueueManager
    // hold static map which source+nodeID has which asssignment, to free them in Endevent and to delay tasktime

    public static Map<String, String> resourceTaskMap = new HashMap<>();

    @Override
    public String getName() {
        return rembrandtConnectorUtils.PLUGIN_NAME;
    }

    @Override
    public Optional<ResourceObjectTuple> getResourcesForEvent(SimulationModel model, ScyllaEvent event)

    {
        resourceTaskMap.put("1", "5e7b762d7c6d5f10fce4b6a3");
        // how to specify which is the correct recipe?

        //in the meantime: use task name
        //get all recipes
        // find ID of correct recipe by name
        // Start resource Assignment
        return Optional.empty();
    }

}

