package de.hpi.bpt.scylla.plugin.batch;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.simulation.ProcessSimulationComponents;
import de.hpi.bpt.scylla.simulation.QueueManager;
import de.hpi.bpt.scylla.simulation.ResourceObjectTuple;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import de.hpi.bpt.scylla.simulation.event.BPMNEndEvent;
import de.hpi.bpt.scylla.simulation.event.BPMNStartEvent;
import de.hpi.bpt.scylla.simulation.event.GatewayEvent;
import de.hpi.bpt.scylla.simulation.event.ScyllaEvent;
import de.hpi.bpt.scylla.simulation.event.TaskBeginEvent;
import de.hpi.bpt.scylla.simulation.event.TaskEnableEvent;
import de.hpi.bpt.scylla.simulation.event.TaskTerminateEvent;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

public class TaskbasedBatchCluster extends BatchCluster implements BatchCluster.StashingCluster{
	
	//Maps from task nodeIds to stash events for that tasks
    private Map<Integer, BatchStashResourceEvent> stashEvents = new HashMap<>();

	TaskbasedBatchCluster(Model owner, TimeInstant creationTime, ProcessSimulationComponents pSimComponents,
			BatchActivity batchActivity, boolean showInTrace) {
		super(owner, creationTime, pSimComponents, batchActivity, showInTrace);
	}
	
	@Override
	public void startEvent(BPMNStartEvent event) {
		super.startEvent(event);
        scheduleNextEventInBatchProcess(event);
	}
	
	@Override
	public void endEvent(BPMNEndEvent event) {
		super.endEvent(event);
        if (!isFinished()) {
        	//Schedule other end events
        	ScyllaEvent eventToSchedule = pollNextQueuedEvent(event.getNodeId());
        	if(eventToSchedule != null)eventToSchedule.schedule();
        }
	}
	
	@Override
	public void gatewayEvent(GatewayEvent event) {
		super.gatewayEvent(event);
		scheduleNextEventInBatchProcess(event);
	}
	
	@Override
	public void taskEnableEvent(TaskEnableEvent event) throws ScyllaRuntimeException {
		super.taskEnableEvent(event);
        if (hasStashedResourcesFor(event)) {
            assignStashedResources(event);
        }
	}
	
	@Override
	public void taskBeginEvent(TaskBeginEvent event) throws ScyllaRuntimeException {
		super.taskBeginEvent(event);
    	ResourceObjectTuple assignedResources = event.getProcessInstance().getAssignedResources().get(event.getSource());
    	if(assignedResources != null && !assignedResources.getResourceObjects().isEmpty()) {
    		scheduleStashEvent(event, assignedResources);
    	}
	}
	
	@Override
	public void taskTerminateEvent(TaskTerminateEvent event) throws ScyllaRuntimeException {
		super.taskTerminateEvent(event);
		if(isBatchTask() && isFinished()) {
			Integer nextNodeId = event.getNextEventMap().values().iterator().next().getNodeId();
			ScyllaEvent nextEvent;
			while(Objects.nonNull(nextEvent = pollNextQueuedEvent(nextNodeId))) {
				nextEvent.schedule();
			}
		}
		scheduleNextEventInBatchProcess(event);
	}
	
    /**
     * For execution type sequential-taskbased:
     * Scheduling the next event for the current node,
     * or the first event for the next node
     * @param event : Event of the current node
     */
    private void scheduleNextEventInBatchProcess(ScyllaEvent event) {
        // The event that would come next in default flow
        List<ScyllaEvent> nextEvents = event.getNextEventMap().values().stream().collect(Collectors.toList());
       //Integer nodeIdOfNextElement = nextEvent != null ? nextEvent.getNodeId() : null;
        
        // Get next queued event for same node or for next node instead
        List<ScyllaEvent> eventsToSchedule = new LinkedList<>();
        ScyllaEvent nextEventOnCurrentNode = pollNextQueuedEvent(event.getNodeId());
        // If existing take next event for current node, otherwise take first queued event for all next nodes
        if (nextEventOnCurrentNode != null) {
        	eventsToSchedule.add(nextEventOnCurrentNode);
        } else {
        	for(ScyllaEvent nextNodeEvent : nextEvents) {
        		ScyllaEvent nextQueuedEvent = pollNextQueuedEvent(nextNodeEvent.getNodeId());
        		if(nextQueuedEvent != null)eventsToSchedule.add(nextQueuedEvent);
        	}
        	//If events for next node are scheduled, this is the last event for this node, so stashed resources must be discarded
        	if(hasStashedResourcesFor(event))discardResources(event);
        }
        
        if (!eventsToSchedule.isEmpty()) {
        	// Schedule selected events
        	for(ScyllaEvent eventToSchedule : eventsToSchedule)eventToSchedule.schedule();
       
        	// "Unschedule" normal flow next event, but queue it instead
            //assert event.getNextEventMap().size() == 1;
            event.getNextEventMap().clear();
            for(ScyllaEvent nextEvent : nextEvents) {
                Integer nodeIdOfNextElement = nextEvent.getNodeId();
                queueEvent(nodeIdOfNextElement, nextEvent);
            }
        }
    }
    
    @Override
    public ScyllaEvent handleStashEvent(BatchStashResourceEvent event) {
		event.makeResourcesUnavailable();
		return super.handleStashEvent(event);
    }
	
    public BatchStashResourceEvent createStashEventFor(TaskBeginEvent beginEvent, ResourceObjectTuple assignedResources) {
    	Integer nodeId = beginEvent.getNodeId();
		assert !stashEvents.containsKey(nodeId);
		BatchStashResourceEvent stashEvent = super.createStashEventFor(beginEvent, assignedResources);
		stashEvents.put(nodeId, stashEvent);
		return stashEvent;
	}
	
	public boolean hasStashedResourcesFor(ScyllaEvent event) {
		return stashEvents.containsKey(event.getNodeId());
	}
	
	public BatchStashResourceEvent getStashEventFor(ScyllaEvent event) {
		return stashEvents.get(event.getNodeId());
	}
	
	public void discardResources(ScyllaEvent event) {
		BatchStashResourceEvent stashEvent = stashEvents.remove(event.getNodeId());
		try {
			QueueManager.releaseResourcesAndScheduleQueuedEvents((SimulationModel) event.getModel(), stashEvent);
		} catch (ScyllaRuntimeException e) {
			e.printStackTrace();
		}
	}

}
