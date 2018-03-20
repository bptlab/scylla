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
import desmoj.core.simulator.Event;
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
    private Map<String, Object> dataView;
    private BatchClusterState state;
    private Duration currentTimeOut;

    private ProcessInstance responsibleProcessInstance;
    private List<TaskTerminateEvent> parentalEndEvents;

    private List<TimeInstant> processInstanceEntranceTimes;
    private TimeInstant startTime;
    private Map<Integer,List<Pair<ScyllaEvent, ProcessInstance>>> notPIEvents;
    private Integer finishedProcessInstances;
    public void setProcessInstanceToFinished(){
        finishedProcessInstances++;
    }

    public boolean areAllProcessInstancesFinished(){
        return finishedProcessInstances.equals(processInstances.size());
    }

    public void addPIEvent(Integer startNodeId, ScyllaEvent notPIEvent, ProcessInstance subprocessInstance) {
        if (this.notPIEvents.get(startNodeId) == null){
            List<Pair<ScyllaEvent, ProcessInstance>> notPIEvents = new ArrayList<Pair<ScyllaEvent, ProcessInstance>>();
            notPIEvents.add(new Pair(notPIEvent, subprocessInstance));
            this.notPIEvents.put(startNodeId, notPIEvents);
        } else {
            this.notPIEvents.get(startNodeId).add(new Pair(notPIEvent, subprocessInstance));
        }
    }

    public Pair<ScyllaEvent, ProcessInstance> getNotPIEvents(Integer startNodeId) {
        if (notPIEvents.containsKey(startNodeId) && !notPIEvents.get(startNodeId).isEmpty()) {
            return notPIEvents.get(startNodeId).remove(0);
        } else
            return null;
    }

    BatchCluster(Model owner, TimeInstant creationTime, ProcessSimulationComponents pSimComponents,
            BatchActivity batchActivity, int nodeId, Map<String, Object> dataView, boolean showInTrace) {
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
        this.notPIEvents = new HashMap<Integer,List<Pair<ScyllaEvent, ProcessInstance>>>();

        this.finishedProcessInstances = 0;
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

    Map<String, Object> getDataView() {
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
}
