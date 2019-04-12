package de.hpi.bpt.scylla.plugin.batch;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.ProcessSimulationComponents;
import de.hpi.bpt.scylla.simulation.QueueManager;
import de.hpi.bpt.scylla.simulation.ResourceObjectTuple;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import de.hpi.bpt.scylla.simulation.event.BPMNEndEvent;
import de.hpi.bpt.scylla.simulation.event.BPMNIntermediateEvent;
import de.hpi.bpt.scylla.simulation.event.BPMNStartEvent;
import de.hpi.bpt.scylla.simulation.event.GatewayEvent;
import de.hpi.bpt.scylla.simulation.event.ScyllaEvent;
import de.hpi.bpt.scylla.simulation.event.TaskBeginEvent;
import de.hpi.bpt.scylla.simulation.event.TaskCancelEvent;
import de.hpi.bpt.scylla.simulation.event.TaskEnableEvent;
import de.hpi.bpt.scylla.simulation.event.TaskTerminateEvent;
import desmoj.core.simulator.Entity;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;

abstract class BatchCluster extends Entity {

	private BatchClusterEnableEvent enableEvent;
	
    private TimeInstant creationTime;
    private ProcessSimulationComponents pSimComponents;
    private BatchActivity batchActivity;

    private List<ProcessInstance> processInstances;
    private List<TaskBeginEvent> parentalStartEvents;
    private BatchClusterState state;
    private Duration currentTimeOut;

    private ProcessInstance responsibleProcessInstance;
    private List<TaskTerminateEvent> parentalEndEvents;
    private Integer startNodeId;

    private List<TimeInstant> processInstanceEntranceTimes;
    private TimeInstant startTime;
    /**Maps from event node ids to a queue of connected events and their process instance<br>
     * At the beginning (BatchClusterStartEvent), there is exactly one queue, that contains for the start event node the start events of all not responsible process instances
     * This stores queues that are used for sequential case- and taskbased:<br>
     * For sequential casebased: The start events of the single instances are stored and scheduled after one instance reaches the end event<br>
     * For sequential taskbased: All start events, task enable events and end events are stored there, and are scheduled sequentially. More events might follow in the future*/
    private Map<Integer,Queue<ScyllaEvent>> queuedEvents;
    // this is needed for all types of sequential execution to determine when to schedule the completion of the batch actvity
    protected Integer finishedProcessInstances = 0;
    
    public static BatchCluster create(Model owner, TimeInstant creationTime, ProcessSimulationComponents pSimComponents, BatchActivity batchActivity, boolean showInTrace) {
    	switch(batchActivity.getExecutionType()) {
    	case PARALLEL : return new ParallelBatchCluster(owner, creationTime, pSimComponents, batchActivity, showInTrace);
    	case SEQUENTIAL_CASEBASED : return new CasebasedBatchCluster(owner, creationTime, pSimComponents, batchActivity, showInTrace);
    	case SEQUENTIAL_TASKBASED : return new TaskbasedBatchCluster(owner, creationTime, pSimComponents, batchActivity, showInTrace);
    	default : return null;
    	}
    }

    protected BatchCluster(Model owner, TimeInstant creationTime, ProcessSimulationComponents pSimComponents,
                 BatchActivity batchActivity, boolean showInTrace) {
        super(owner, buildBatchClusterName(pSimComponents, batchActivity.getNodeId()), showInTrace);
        this.creationTime = creationTime;
        this.pSimComponents = pSimComponents;
        this.batchActivity = batchActivity;

        this.processInstances = new ArrayList<ProcessInstance>();
        this.parentalStartEvents = new ArrayList<TaskBeginEvent>();
        this.state = BatchClusterState.INIT;

        this.responsibleProcessInstance = null;
        this.parentalEndEvents = new ArrayList<TaskTerminateEvent>();

        this.processInstanceEntranceTimes = new ArrayList<TimeInstant>();
        this.startTime = null;
        this.queuedEvents = new HashMap<Integer,Queue<ScyllaEvent>>();
    }

    private static String buildBatchClusterName(ProcessSimulationComponents pSimComponents, int nodeId) {
        ProcessModel processModel = pSimComponents.getProcessModel();
        return prependProcessModelIds(processModel) + nodeId;
    }

    private static String prependProcessModelIds(ProcessModel processModel) {
        if (processModel.getParent() == null) {
            return "BatchCluster_" + processModel.getId() + "_";
        }
        return prependProcessModelIds(processModel.getParent()) + processModel.getId() + "_";
    }
    
    public BatchClusterExecutionType getExecutionType() {
    	return getBatchActivity().getExecutionType();
    }

    public Integer getStartNodeId() {
        return startNodeId;
    }

    public void setStartNodeId(Integer startNodeId) {
        this.startNodeId = startNodeId;
    }
    
    public boolean isProcessInstanceMatchingGroupingCharacteristic(ProcessInstance processInstance) {
    	return getProcessInstances().isEmpty() || 
	    	getBatchActivity().getGroupingCharacteristic().stream()
	    		.allMatch(each -> each.isFulfilledBetween(getProcessInstances().get(0), processInstance));
    }

    public void setProcessInstanceToFinished(){
        finishedProcessInstances++;
    }

    public boolean areAllProcessInstancesFinished(){
        return finishedProcessInstances.equals(processInstances.size());
    }

    /**
     * Queue an event for later use @see {@link BatchCluster#queuedEvents}
     * @param nodeId : Id of the node the event is stored for
     * @param event : Event to be queued
     */
    public void queueEvent(Integer nodeId, ScyllaEvent event) {
        if(!queuedEvents.containsKey(nodeId)) {
            queuedEvents.put(nodeId, new LinkedList<ScyllaEvent>());
        }
        queuedEvents.get(nodeId).add(event);
    }

    /**
     * Pops the next event for the node with a given id
     * @param nodeId : Id of the event node
     * @return Event  or null if no more elements are queued
     */
    public ScyllaEvent pollNextQueuedEvent(Integer nodeId) {
        if (!queuedEvents.containsKey(nodeId))return null;
        return queuedEvents.get(nodeId).poll();
    }


    TimeInstant getCreationTime() {
        return creationTime;
    }

    ProcessSimulationComponents getProcessSimulationComponents() {
        return pSimComponents;
    }

    BatchActivity getBatchActivity() {
        return batchActivity;
    }
    
    List<ProcessInstance> getProcessInstances() {
        return processInstances;
    }

    List<TaskBeginEvent> getParentalStartEvents() {
        return parentalStartEvents;
    }

    void addProcessInstance(ProcessInstance processInstance, TaskBeginEvent parentalStartEvent) {
        this.processInstances.add(processInstance);
        this.processInstanceEntranceTimes.add(processInstance.presentTime());
        int numberOfProcessInstances = processInstances.size();
        // in case that the threshold is not defined, it never gets activated here
        if (numberOfProcessInstances >= batchActivity.getActivationRule().getThreshold(parentalStartEvent, processInstance)) {
            this.state = BatchClusterState.READY;
        }
        if (numberOfProcessInstances >= batchActivity.getMaxBatchSize()) {
            this.state = BatchClusterState.MAXLOADED;
        }

        getParentalStartEvents().add(parentalStartEvent);
    }

    protected BatchClusterEnableEvent getEnableEvent() {
		return enableEvent;
	}

    protected void setEnableEvent(BatchClusterEnableEvent enableEvent) {
		this.enableEvent = enableEvent;
	}

	BatchClusterState getState() {
        return state;
    }

    void setState(BatchClusterState state) {
        this.state = state;
    }

    ProcessInstance getResponsibleProcessInstance() {
        return responsibleProcessInstance;
    }

    void setResponsibleProcessInstance(ProcessInstance responsibleProcessInstance) {
        this.responsibleProcessInstance = responsibleProcessInstance;
    }

    List<TaskTerminateEvent> getParentalEndEvents() {
        return parentalEndEvents;
    }

    public TimeInstant getStartTime() {
        return startTime;
    }

    public void setStartTime(TimeInstant startTime) {
        this.startTime = startTime;
    }

    public List<TimeInstant> getProcessInstanceEntranceTimes() {
        return processInstanceEntranceTimes;
    }

	public Duration getCurrentTimeOut() {
		return currentTimeOut;
	}

	public void setCurrentTimeOut(Duration currentTimeOut) {
		this.currentTimeOut = currentTimeOut;
	}
	
    public boolean hasNotStarted() {
        return getState() == BatchClusterState.INIT || getState() == BatchClusterState.READY;
    }
	
    /**
     * This cluster is a batch task, if there is no subprocess for its node id.
     */
    public boolean isBatchTask() {
    	return !getProcessSimulationComponents().getProcessModel().getSubProcesses().containsKey(getBatchActivity().getNodeId());
    }
	
	
	public void startEvent(BPMNStartEvent event) {}
	
	public void intermediateEvent(BPMNIntermediateEvent event) {}
	
	public void endEvent(BPMNEndEvent event) {
		ProcessInstance processInstance = event.getProcessInstance();
        BatchPluginUtils pluginInstance = BatchPluginUtils.getInstance();
        setProcessInstanceToFinished();
        // Schedule them only if either all process instances has passed the last event of the batch activity or the execution type is parallel
        if (isFinished()) {

            if (pluginInstance.isProcessInstanceCompleted(processInstance)) {
                List<TaskTerminateEvent> parentalEndEvents = getParentalEndEvents();
                for (TaskTerminateEvent pee : parentalEndEvents) {
                    pee.schedule();
                }

                parentalEndEvents.clear();

                pluginInstance.setClusterToTerminated(getResponsibleProcessInstance(), getBatchActivity().getNodeId());
            }

            // Prevent parental task terminate event from scheduling, if there is any (from subprocess plugin)

            Map<Integer, ScyllaEvent> nextEventMap = event.getNextEventMap();
            if (!nextEventMap.isEmpty()) {
            	//TODO this is currently not covered, when is it called?
                Map<Integer, TimeSpan> timeSpanToNextEventMap = event.getTimeSpanToNextEventMap();
                int indexOfParentalTaskTerminateEvent = 0;

                nextEventMap.remove(indexOfParentalTaskTerminateEvent);
                timeSpanToNextEventMap.remove(indexOfParentalTaskTerminateEvent);
            }
        }
	}
	
	public void gatewayEvent(GatewayEvent event) {}
	
	public void taskEnableEvent(TaskEnableEvent event) throws ScyllaRuntimeException {}
	public void taskBeginEvent(TaskBeginEvent event) throws ScyllaRuntimeException {}
	public void taskTerminateEvent(TaskTerminateEvent event) throws ScyllaRuntimeException {
		if(isBatchTask()) {
			setProcessInstanceToFinished();
		}
	}
	public void taskCancelEvent(TaskCancelEvent event) throws ScyllaRuntimeException {
        for (TaskTerminateEvent pee : parentalEndEvents) {
            TaskCancelEvent cancelEvent = new TaskCancelEvent(pee.getModel(), pee.getSource(),
                    pee.getSimulationTimeOfSource(), pee.getSimulationComponents(), pee.getProcessInstance(),
                    pee.getNodeId());
            cancelEvent.schedule(pee.getProcessInstance());
        }

        parentalEndEvents.clear();

        BatchPluginUtils.getInstance().setClusterToTerminated(getResponsibleProcessInstance(), getBatchActivity().getNodeId());
	}
	
	public boolean isFinished () {
		return areAllProcessInstancesFinished();
	}
	
    
	public ScyllaEvent handleStashEvent(BatchStashResourceEvent event) {
		return event;
	}
	
	protected BatchStashResourceEvent createStashEventFor(TaskBeginEvent beginEvent, ResourceObjectTuple assignedResources) {
		return new BatchStashResourceEvent(BatchCluster.this, beginEvent, assignedResources);
	}
	
	protected interface StashingCluster {
		
	    /**
	     * Schedule a stash event for the given resources so they are not released when the task ends
	     * @param event
	     * @param assignedResources
	     */
		default void scheduleStashEvent(TaskBeginEvent event, ResourceObjectTuple assignedResources) {
			BatchStashResourceEvent stashEvent = getOrCreateStashEvent(event, assignedResources);
			BatchPluginUtils.getInstance().scheduleStashEvent(stashEvent);
		}
	    
		BatchStashResourceEvent createStashEventFor(TaskBeginEvent beginEvent, ResourceObjectTuple assignedResources);
		
		default BatchStashResourceEvent getOrCreateStashEvent(TaskBeginEvent beginEvent, ResourceObjectTuple assignedResources) {
			if(hasStashedResourcesFor(beginEvent)) {
				return getStashEventFor(beginEvent);
			} else {
				return createStashEventFor(beginEvent, assignedResources);
			}
		}
		
		/**
		 * Ensure that if there are stashed resources for a task,
		 * force-assign them to the task begin event and assure execution of event.
		 * @param event : Enable event of a task inside a sequential task-based batch region
		 */
		default void assignStashedResources(TaskEnableEvent event) {
			assert hasStashedResourcesFor(event);
			BatchStashResourceEvent stashEvent = getStashEventFor(event);
			ResourceObjectTuple resources = stashEvent.getResources();
			TaskBeginEvent beginEvent = event.getBeginEvent();
			if(!event.getNextEventMap().isEmpty()) {//Event has assigned resources - but not the ones that are stashed for it
				assert event.getNextEventMap().size() == 1;
				assert event.getNextEventMap().get(0) == beginEvent;
				try {
					QueueManager.releaseResourcesAndScheduleQueuedEvents((SimulationModel) beginEvent.getModel(), beginEvent);
				} catch (ScyllaRuntimeException e) { e.printStackTrace(); }
			} else {//Event waits for resources
		        QueueManager.removeFromEventQueues((SimulationModel) beginEvent.getModel(), beginEvent);
	            event.getNextEventMap().put(0, beginEvent);
	            event.getTimeSpanToNextEventMap().put(0, new TimeSpan(0));
			}
			QueueManager.assignResourcesToEvent((SimulationModel) beginEvent.getModel(), beginEvent, resources);
			stashEvent.setResourcesInStash(false);
		}
		
		BatchStashResourceEvent getStashEventFor(ScyllaEvent event);
		boolean hasStashedResourcesFor(ScyllaEvent event);
	}
	



}
