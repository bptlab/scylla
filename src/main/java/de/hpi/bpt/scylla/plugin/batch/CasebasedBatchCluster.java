package de.hpi.bpt.scylla.plugin.batch;

import java.util.LinkedList;
import java.util.Queue;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.simulation.ProcessSimulationComponents;
import de.hpi.bpt.scylla.simulation.QueueManager;
import de.hpi.bpt.scylla.simulation.ResourceObjectTuple;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import de.hpi.bpt.scylla.simulation.event.BPMNEndEvent;
import de.hpi.bpt.scylla.simulation.event.BPMNStartEvent;
import de.hpi.bpt.scylla.simulation.event.ScyllaEvent;
import de.hpi.bpt.scylla.simulation.event.TaskBeginEvent;
import de.hpi.bpt.scylla.simulation.event.TaskEnableEvent;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

public class CasebasedBatchCluster extends BatchCluster {
	
	private BatchStashResourceEvent stashEvent;
	
	private Queue<TaskBeginEvent> waitingTaskBegins = new LinkedList<>();

	CasebasedBatchCluster(Model owner, TimeInstant creationTime, ProcessSimulationComponents pSimComponents,
			BatchActivity batchActivity, int nodeId, String dataView, boolean showInTrace) {
		super(owner, creationTime, pSimComponents, batchActivity, nodeId, dataView, showInTrace);
	}
	
	@Override
	public void startEvent(BPMNStartEvent event) {
		super.startEvent(event);
	}
	

	@Override
	public void endEvent(BPMNEndEvent event) {
		super.endEvent(event);
		if(!isFinished())scheduleNextCaseInBatchProcess();
		else if(hasStashedResources())discardResources();
	}
	
	@Override
	public void taskEnableEvent(TaskEnableEvent event) throws ScyllaRuntimeException {
		super.taskEnableEvent(event);
		TaskBeginEvent beginEvent = event.getBeginEvent();
		if(hasStashedResources()) {
			if(resourcesAreInStash()) {//Resources are in stash => resources are available
				//Force assign and assure execution
				assignStashedResources(event);
			} else { // Resources are not in stash => not available
				//If begin event was already scheduled for execution, remove it and release (wrong) resources
				if(event.getNextEventMap().size() > 0) {
					ScyllaEvent removedEvent = event.getNextEventMap().remove(0);
					assert removedEvent == beginEvent;
					event.getTimeSpanToNextEventMap().remove(0);
					QueueManager.releaseResourcesAndScheduleQueuedEvents((SimulationModel) beginEvent.getModel(), beginEvent);
				}
				//Wait for resources in extra queue
				waitingTaskBegins.add(beginEvent);
			}
		} else {
	    	ResourceObjectTuple assignedResources = event.getProcessInstance().getAssignedResources().get(beginEvent.getSource());
	    	//If an assignment exist, use it for all further assignments
	    	if(assignedResources != null && !assignedResources.getResourceObjects().isEmpty()) {
	    		createStashEventFor(beginEvent, assignedResources);//so all other events know these are the resources to be used for stashing
	    	}//else wait until natural assignment is done
		}
	}
	
	private boolean resourcesAreInStash() {
		return stashEvent.getProcessInstance().getAssignedResources().get(stashEvent.getSource()) != null;
	}


	@Override
	public void taskBeginEvent(TaskBeginEvent event) throws ScyllaRuntimeException {
		super.taskBeginEvent(event);
    	ResourceObjectTuple assignedResources = event.getProcessInstance().getAssignedResources().get(event.getSource());
    	if(assignedResources != null && !assignedResources.getResourceObjects().isEmpty()) {
    		scheduleStashEvent(event, assignedResources);
    	}
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
        }
    }
	
	@Override
	public ScyllaEvent handleStashEvent(BatchStashResourceEvent event) {
		stashEvent.makeResourcesUnavailable();
		if(!waitingTaskBegins.isEmpty()) {
			TaskBeginEvent nextEvent = waitingTaskBegins.poll();
			QueueManager.assignResourcesToEvent((SimulationModel) nextEvent.getModel(), nextEvent, stashEvent.getResources());
			QueueManager.removeFromEventQueues((SimulationModel) nextEvent.getModel(), nextEvent);
			return nextEvent;
		}
		return stashEvent;
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
