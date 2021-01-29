package de.hpi.bpt.scylla.plugin.batch;

import java.util.Set;

import de.hpi.bpt.scylla.plugin_type.simulation.resource.ResourceQueueUpdatedPluggable;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import de.hpi.bpt.scylla.simulation.event.ScyllaEvent;

public class BatchResourceQueueUpdatedPlugin extends ResourceQueueUpdatedPluggable {

	@Override
	public String getName() {
		return BatchPluginUtils.PLUGIN_NAME;
	}

	@Override
	public ScyllaEvent eventToBeScheduled(SimulationModel model, String[] resourceQueuesUpdated) {
		for(BatchStashResourceEvent stashEvent : BatchPluginUtils.getInstance().getStashEvents()) {
			if(stashEvent.interestedInResources(resourceQueuesUpdated)) {
				BatchPluginUtils.getInstance().getStashEvents().remove(stashEvent);
				return stashEvent.getCluster().handleStashEvent(stashEvent);
			}
		}
		return null;
	}

}
