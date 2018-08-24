package de.hpi.bpt.scylla.plugin.batch;

import java.util.Collection;
import java.util.Set;

import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.ResourceObject;
import de.hpi.bpt.scylla.simulation.ResourceObjectTuple;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import de.hpi.bpt.scylla.simulation.event.ScyllaEvent;
import de.hpi.bpt.scylla.simulation.event.TaskBeginEvent;
import desmoj.core.simulator.TimeInstant;

public class BatchStashResourceEvent extends ScyllaEvent{

	private ResourceObjectTuple resources;
	private String taskSource;

	public BatchStashResourceEvent(BatchCluster cluster, TaskBeginEvent taskBeginEvent, ResourceObjectTuple resources) {
		super(cluster.getModel(), cluster.getName()+"_stashNode#"+taskBeginEvent.getNodeId(), new TimeInstant(0), cluster.getProcessSimulationComponents(), taskBeginEvent.getProcessInstance(), taskBeginEvent.getNodeId());
		this.resources = resources;
		taskSource = taskBeginEvent.getSource();
	}

	@Override
	public void eventRoutine(ProcessInstance processInstance) {
		//cluster.stashResources(this, resources);
	}
	
	public void makeResourcesUnavailable() {
		//Remove resources from queues; they are not available anymore
		SimulationModel model = (SimulationModel)getModel();
		for(ResourceObject resource : resources.getResourceObjects()) {
			Collection<ResourceObject> typeQueue = model.getResourceObjects().get(resource.getResourceType());
			typeQueue.remove(resource);
		}
	}
	
	/**
	 * Have the resources for the old source been removed?
	 * @param resourceIds : Ids of released resources
	 * @return
	 */
	public boolean interestedInResources(Set<String> resourceIds) {
		return !processInstance.getAssignedResources().containsKey(taskSource);
	}

}
