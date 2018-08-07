package de.hpi.bpt.scylla.plugin.batch;

import java.time.Duration;
import java.util.*;

import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.ProcessSimulationComponents;
import de.hpi.bpt.scylla.simulation.event.ScyllaEvent;
import de.hpi.bpt.scylla.simulation.event.TaskBeginEvent;
import de.hpi.bpt.scylla.simulation.event.TaskTerminateEvent;
import desmoj.core.simulator.Entity;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import org.javatuples.Pair;

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
    private Map<Integer,Queue<Pair<ScyllaEvent, ProcessInstance>>> queuedEvents;
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
        this.queuedEvents = new HashMap<Integer,Queue<Pair<ScyllaEvent, ProcessInstance>>>();
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
     * @param startNodeId : Id of the node of the start event
     * @param event : Event to be queued
     * @param subprocessInstance : Process instance
     */
    public void queueEvent(Integer startNodeId, ScyllaEvent event, ProcessInstance subprocessInstance) {
        if(!queuedEvents.containsKey(startNodeId)) {
            queuedEvents.put(startNodeId, new LinkedList<Pair<ScyllaEvent, ProcessInstance>>());
        }
        queuedEvents.get(startNodeId).add(new Pair<>(event, subprocessInstance));
    }

    /**
     * Pops the next event for the node with a given id
     * @param nodeId : Id of the start event node
     * @return Pair of event and the process instance it belongs to or null if no more elements are queued
     */
    public Pair<ScyllaEvent, ProcessInstance> pollNextQueuedEvent(Integer nodeId) {
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
        Pair<ScyllaEvent, ProcessInstance> eventToSchedule = pollNextQueuedEvent(getStartNodeId());
        if (eventToSchedule != null) {
            eventToSchedule.getValue0().schedule(eventToSchedule.getValue1());
            //System.out.println("Scheduled " + eventToSchedule.getValue0().getDisplayName() + " for process instance " + eventToSchedule.getValue1());
        }
    }
}
