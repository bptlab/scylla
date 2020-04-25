package de.hpi.bpt.scylla.plugin.rembrandtConnector;
import de.hpi.bpt.scylla.plugin_type.simulation.resource.ResourceQueueUpdatedPluggable;
import de.hpi.bpt.scylla.simulation.event.ScyllaEvent;

import java.util.Set;

public class ResourceQueuesUpdatedPlugin extends ResourceQueueUpdatedPluggable {

    // this plugin manages waiting events and starts them when a resource is available.
    @Override
    public String getName() {
        return rembrandtConnectorUtils.PLUGIN_NAME;
    }

    @Override
    public ScyllaEvent eventToBeScheduled(Set<String> resourceQueuesUpdated){

        System.out.println(resourceQueuesUpdated);
        // if resource is from Rembrandt
        // return the first event with nodeid and processid from the static map



        return null;
    }

}
