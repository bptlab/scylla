package de.hpi.bpt.scylla.plugin.rembrandtConnector;
import de.hpi.bpt.scylla.plugin_type.simulation.resource.ResourceQueueUpdatedPluggable;
import de.hpi.bpt.scylla.simulation.ScyllaEventQueue;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import de.hpi.bpt.scylla.simulation.event.ScyllaEvent;

import java.util.Set;

public class RembrandtResourceQueuesUpdatedPlugin extends ResourceQueueUpdatedPluggable {

    // this plugin manages waiting events and starts them when a resource is available.
    @Override
    public String getName() {
        return rembrandtConnectorUtils.PLUGIN_NAME;
    }

    @Override
    public ScyllaEvent eventToBeScheduled(SimulationModel model, Set<String> resourceQueuesUpdated){

        System.out.println(resourceQueuesUpdated);
        System.out.println(model.getGlobalConfiguration().getResources());
        // if resource is from Rembrandt
        // return the first event with nodeid and processid from the static map
        //ScyllaEventQueue rembrandtResourceTypeQueue = model.getEventQueues().get(//resourceID of pseudotype);




        return null;
    }

}
