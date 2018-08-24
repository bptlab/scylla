package de.hpi.bpt.scylla.plugin.batch;

import java.util.Set;

import co.paralleluniverse.fibers.SuspendExecution;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.ResourceObjectTuple;
import de.hpi.bpt.scylla.simulation.event.ScyllaEvent;
import de.hpi.bpt.scylla.simulation.event.TaskBeginEvent;
import desmoj.core.simulator.TimeInstant;

public class BatchStashResourceEvent extends ScyllaEvent{

	private ResourceObjectTuple resources;
	private BatchCluster cluster;
	private String taskSource;

	public BatchStashResourceEvent(BatchCluster cluster, TaskBeginEvent taskBeginEvent, ResourceObjectTuple resources) {
		super(cluster.getModel(), cluster.getName()+"_stashNode#"+taskBeginEvent.getNodeId(), new TimeInstant(0), cluster.getProcessSimulationComponents(), taskBeginEvent.getProcessInstance(), taskBeginEvent.getNodeId());
		this.resources = resources;
		this.cluster = cluster;
		taskSource = taskBeginEvent.getSource();
	}

	@Override
	public void eventRoutine(ProcessInstance processInstance) throws SuspendExecution {
		//cluster.stashResources(this, resources);
	}
	
	public void stashResources() {
		cluster.stashResources(this, resources);
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
