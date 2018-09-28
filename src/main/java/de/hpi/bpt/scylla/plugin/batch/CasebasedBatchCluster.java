package de.hpi.bpt.scylla.plugin.batch;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.simulation.ProcessSimulationComponents;
import de.hpi.bpt.scylla.simulation.QueueManager;
import de.hpi.bpt.scylla.simulation.ResourceObjectTuple;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import de.hpi.bpt.scylla.simulation.event.BPMNEndEvent;
import de.hpi.bpt.scylla.simulation.event.ScyllaEvent;
import de.hpi.bpt.scylla.simulation.event.TaskBeginEvent;
import de.hpi.bpt.scylla.simulation.event.TaskEnableEvent;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

public class CasebasedBatchCluster extends BatchCluster {
	
	private BatchStashResourceEvent stashEvent;

	CasebasedBatchCluster(Model owner, TimeInstant creationTime, ProcessSimulationComponents pSimComponents,
			BatchActivity batchActivity, int nodeId, String dataView, boolean showInTrace) {
		super(owner, creationTime, pSimComponents, batchActivity, nodeId, dataView, showInTrace);
	}
	

	@Override
	public void endEvent(BPMNEndEvent event) {
		super.endEvent(event);
		if(!isFinished())scheduleNextCaseInBatchProcess();
	}
	
	@Override
	public void taskEnableEvent(TaskEnableEvent event) throws ScyllaRuntimeException {
		super.taskEnableEvent(event);
		//TODO maybe some stashing here?
	}
	
	@Override
	public void taskBeginEvent(TaskBeginEvent event) throws ScyllaRuntimeException {
		super.taskBeginEvent(event);
		//TODO maybe some stashing here?
	}
	
    /**
     * For execution type sequential-casebased:
     * Poll and schedule the start event for the next case if existent
     */
    private void scheduleNextCaseInBatchProcess() {
        // Get the start event of the next process instance and schedule it
        ScyllaEvent eventToSchedule = pollNextQueuedEvent(getStartNodeId());
        if (eventToSchedule != null) {
            eventToSchedule.schedule(eventToSchedule.getProcessInstance());
            //System.out.println("Scheduled " + eventToSchedule.getValue0().getDisplayName() + " for process instance " + eventToSchedule.getValue1());
        } else {
        	if(hasStashedResources())discardResources();
        }
    }
	
	
	
    public BatchStashResourceEvent createStashEventFor(TaskBeginEvent beginEvent, ResourceObjectTuple assignedResources) {
		assert stashEvent == null;
		stashEvent = super.createStashEventFor(beginEvent, assignedResources);
		return stashEvent;
	}
	
	public boolean hasStashedResourcesFor(ScyllaEvent event) {
		return hasStashedResources();//TODO remove from interface
	}
	
	public boolean hasStashedResources() {
		return stashEvent != null;
	}
	
	public BatchStashResourceEvent getStashEventFor(ScyllaEvent event) {
		return stashEvent;
	}
	
	public void discardResources() {
		try {
			QueueManager.releaseResourcesAndScheduleQueuedEvents((SimulationModel) stashEvent.getModel(), stashEvent);
		} catch (ScyllaRuntimeException e) {
			e.printStackTrace();
		}
	}

}
