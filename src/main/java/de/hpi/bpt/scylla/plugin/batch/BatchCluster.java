package de.hpi.bpt.scylla.plugin.batch;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.ProcessSimulationComponents;
import de.hpi.bpt.scylla.simulation.QueueManager;
import de.hpi.bpt.scylla.simulation.ResourceObjectTuple;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import de.hpi.bpt.scylla.simulation.event.ScyllaEvent;
import de.hpi.bpt.scylla.simulation.event.TaskBeginEvent;
import de.hpi.bpt.scylla.simulation.event.TaskEnableEvent;
import de.hpi.bpt.scylla.simulation.event.TaskTerminateEvent;
import desmoj.core.simulator.Entity;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;

class BatchCluster extends Entity {

    private TimeInstant creationTime;
    private ProcessSimulationComponents pSimComponents;
    private BatchActivity batchActivity;
    private int nodeId;

    private List<ProcessInstance> processInstances;
    private List<TaskBeginEvent> parentalStartEvents;
    private String dataView;
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
    private Integer finishedProcessInstances = 0;

    BatchCluster(Model owner, TimeInstant creationTime, ProcessSimulationComponents pSimComponents,
                 BatchActivity batchActivity, int nodeId, String dataView, boolean showInTrace) {
        super(owner, buildBatchClusterName(pSimComponents, nodeId), showInTrace);
        this.creationTime = creationTime;
        this.pSimComponents = pSimComponents;
        this.batchActivity = batchActivity;
        this.nodeId = nodeId;

        this.processInstances = new ArrayList<ProcessInstance>();
        this.parentalStartEvents = new ArrayList<TaskBeginEvent>();
        this.dataView = dataView;
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

    public boolean hasExecutionType(BatchClusterExecutionType executionType){
        return this.getBatchActivity().getExecutionType().equals(executionType);
    }
    public Integer getStartNodeId() {
        return startNodeId;
    }

    public void setStartNodeId(Integer startNodeId) {
        this.startNodeId = startNodeId;
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

    int getNodeId() {
        return nodeId;
    }

    String getDataView() {
        return dataView;
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
        if (numberOfProcessInstances == batchActivity.getActivationRule().getThreshold(parentalStartEvent, processInstance)) {
            this.state = BatchClusterState.READY;
        }
        if (numberOfProcessInstances == batchActivity.getMaxBatchSize()) {
            this.state = BatchClusterState.MAXLOADED;
        }

        this.parentalStartEvents.add(parentalStartEvent);
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
	
    /**
     * For execution type sequential-casebased:
     * Poll and schedule the start event for the next case if existent
     */
    void scheduleNextCaseInBatchProcess() {
        // Get the start event of the next process instance and schedule it
        ScyllaEvent eventToSchedule = pollNextQueuedEvent(getStartNodeId());
        if (eventToSchedule != null) {
            eventToSchedule.schedule(eventToSchedule.getProcessInstance());
            //System.out.println("Scheduled " + eventToSchedule.getValue0().getDisplayName() + " for process instance " + eventToSchedule.getValue1());
        }
    }
    
    /**
     * For execution type sequential-taskbased:
     * Scheduling the next event for the current node,
     * or the first event for the next node
     * Currently called for start and task terminate events
     * @param event : Event of the current node
     */
    void scheduleNextEventInBatchProcess(ScyllaEvent event) {
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
        	if(hasStashedResourcesFor(event.getNodeId()))discardResources(event);
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

    //TODO Stashing is specific for task-based clusters => use polymorphism and subclass BatchCluster
    private Map<Integer, BatchStashResourceEvent> stashEvents = new HashMap<>();
    

    /**
     * Schedule a stash event for the given resources so they are not released when the task ends
     * @param event
     * @param assignedResources
     */
	public void scheduleStashEvent(TaskBeginEvent event, ResourceObjectTuple assignedResources) {
		BatchStashResourceEvent stashEvent = getStashResourceEvent(event, assignedResources);
		BatchPluginUtils.getInstance().scheduleStashEvent(stashEvent);
	}
    
	public BatchStashResourceEvent getStashResourceEvent(TaskBeginEvent beginEvent, ResourceObjectTuple assignedResources) {
		Integer nodeId = beginEvent.getNodeId();
		if(hasStashedResourcesFor(nodeId)) {
			return stashEvents.get(nodeId);
		} else {
			assert !stashEvents.containsKey(nodeId);
			BatchStashResourceEvent stashEvent = new BatchStashResourceEvent(this, beginEvent, assignedResources);
			stashEvents.put(nodeId, stashEvent);
			return stashEvent;
		}
	}
	
	public void discardResources(ScyllaEvent event) {
		BatchStashResourceEvent stashEvent = stashEvents.remove(event.getNodeId());
		try {
			QueueManager.releaseResourcesAndScheduleQueuedEvents((SimulationModel) event.getModel(), stashEvent);
		} catch (ScyllaRuntimeException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Ensure that if there are stashed resources for a task,
	 * force-assign them to the task begin event and assure execution of event.
	 * @param event : Enable event of a task inside a sequential task-based batch region
	 */
	public void assignStashedResources(TaskEnableEvent event) {
		if(hasStashedResourcesFor(event.getNodeId())) {
			ResourceObjectTuple resources = stashEvents.get(event.getNodeId()).getResources();
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
		}
	}
	
	public boolean hasStashedResourcesFor(Integer nodeId) {
		return stashEvents.containsKey(nodeId);
	}


}
