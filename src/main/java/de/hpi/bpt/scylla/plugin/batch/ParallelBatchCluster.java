package de.hpi.bpt.scylla.plugin.batch;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.logger.ProcessNodeInfo;
import de.hpi.bpt.scylla.logger.ProcessNodeTransitionType;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.ProcessSimulationComponents;
import de.hpi.bpt.scylla.simulation.ResourceObjectTuple;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import de.hpi.bpt.scylla.simulation.event.BPMNEndEvent;
import de.hpi.bpt.scylla.simulation.event.BPMNEvent;
import de.hpi.bpt.scylla.simulation.event.BPMNIntermediateEvent;
import de.hpi.bpt.scylla.simulation.event.BPMNStartEvent;
import de.hpi.bpt.scylla.simulation.event.ScyllaEvent;
import de.hpi.bpt.scylla.simulation.event.TaskBeginEvent;
import de.hpi.bpt.scylla.simulation.event.TaskCancelEvent;
import de.hpi.bpt.scylla.simulation.event.TaskEnableEvent;
import de.hpi.bpt.scylla.simulation.event.TaskEvent;
import de.hpi.bpt.scylla.simulation.event.TaskTerminateEvent;
import de.hpi.bpt.scylla.simulation.utils.DateTimeUtils;
import de.hpi.bpt.scylla.simulation.utils.SimulationUtils;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

public class ParallelBatchCluster extends BatchCluster {

	ParallelBatchCluster(Model owner, TimeInstant creationTime, ProcessSimulationComponents pSimComponents,
			BatchActivity batchActivity, int nodeId, String dataView, boolean showInTrace) {
		super(owner, creationTime, pSimComponents, batchActivity, nodeId, dataView, showInTrace);
	}
	
	/**
	 * For parallel clusters only one process instance finishes
	 */
	@Override
	public boolean isFinished() {
		return super.isFinished() || finishedProcessInstances > 0;
	}
	
	@Override
	public void startEvent(BPMNStartEvent event) {
		logBPMNEventForNonResponsiblePI(event);
		super.startEvent(event);
	}
	
	@Override
	public void intermediateEvent(BPMNIntermediateEvent event) {
		logBPMNEventForNonResponsiblePI(event);
		super.intermediateEvent(event);
	}
	
	@Override
	public void endEvent(BPMNEndEvent event) {
		logBPMNEventForNonResponsiblePI(event);
		super.endEvent(event);
	}
	
	
	@Override
	public void taskEnableEvent(TaskEnableEvent event)  throws ScyllaRuntimeException{
		logTaskEventForNonResponsiblePI(event);
		super.taskEnableEvent(event);
	}
	
	@Override
	public void taskBeginEvent(TaskBeginEvent event) throws ScyllaRuntimeException {
		logTaskEventForNonResponsiblePI(event);
		super.taskBeginEvent(event);
	}
	
	@Override
	public void taskTerminateEvent(TaskTerminateEvent event) throws ScyllaRuntimeException {
		if(!isBatchTask()) {
			logTaskEventForNonResponsiblePI(event);
		} else {
			List<ScyllaEvent> eventsToRemove = getParentalEndEvents().stream().filter(each -> each.getProcessInstance().equals(event.getProcessInstance())).collect(Collectors.toList());
			assert eventsToRemove.size() == 1;
			getParentalEndEvents().removeAll(eventsToRemove);
			getParentalEndEvents().stream().filter(each -> !each.getProcessInstance().equals(event.getProcessInstance()))
				.findAny().ifPresent(ScyllaEvent::schedule);
		}
		super.taskTerminateEvent(event);
	}
	
	@Override
	public void taskCancelEvent(TaskCancelEvent event) throws ScyllaRuntimeException {
		logTaskEventForNonResponsiblePI(event);
		super.taskCancelEvent(event);
	}
	
    private void logBPMNEventForNonResponsiblePI(BPMNEvent event) {

    	ProcessInstance processInstance = event.getProcessInstance();
        ProcessModel processModel = processInstance.getProcessModel();

        SimulationModel model = (SimulationModel) event.getModel();

        long timestamp = model.presentTime().getTimeRounded(DateTimeUtils.getReferenceTimeUnit());
        Set<String> resources = new HashSet<String>();

        String taskName = event.getDisplayName();
        int nodeId = event.getNodeId();
        String processScopeNodeId = SimulationUtils.getProcessScopeNodeId(processModel, nodeId);
        String source = event.getSource();

        int sourceSuffix = 0;
        List<ProcessInstance> processInstances = getProcessInstances();
        for (ProcessInstance pi : processInstances) {

            if (!getResponsibleProcessInstance().equals(pi)) {

                // the source attribute comes from an event, but we did not really simulate the events for the
                // non-responsible process instances, so we mock a source attribute value
                String mockSource = source + "##" + ++sourceSuffix;

                ProcessNodeInfo info;
                info = new ProcessNodeInfo(nodeId, processScopeNodeId, mockSource, timestamp, taskName, resources,
                        ProcessNodeTransitionType.EVENT_BEGIN);
                model.addNodeInfo(processModel, pi, info);

                info = new ProcessNodeInfo(nodeId, processScopeNodeId, mockSource, timestamp, taskName, resources,
                        ProcessNodeTransitionType.EVENT_TERMINATE);
                model.addNodeInfo(processModel, pi, info);
            }
        }
    }
    
    private void logTaskEventForNonResponsiblePI(TaskEvent event) throws ScyllaRuntimeException {
    	
    	ProcessInstance processInstance = event.getProcessInstance();
        ProcessModel processModel = processInstance.getProcessModel();

        SimulationModel model = (SimulationModel) event.getModel();

        long timestamp = Math.round(model.presentTime().getTimeRounded(DateTimeUtils.getReferenceTimeUnit()));

        String taskName = event.getDisplayName();
        int nodeId = event.getNodeId();
        String processScopeNodeId = SimulationUtils.getProcessScopeNodeId(processModel, nodeId);
        String source = event.getSource();

        ProcessNodeTransitionType transition;
        if (event instanceof TaskEnableEvent) {
            transition = ProcessNodeTransitionType.ENABLE;
        } else if (event instanceof TaskBeginEvent) {
            transition = ProcessNodeTransitionType.BEGIN;
			ResourceObjectTuple commonResources = event.getProcessInstance().getAssignedResources().get(source);
			getParentalStartEvents().stream().forEach(each -> each.getProcessInstance().getAssignedResources().put(each.getSource(), commonResources));
        } else if (event instanceof TaskCancelEvent) {
            transition = ProcessNodeTransitionType.CANCEL;
        } else if (event instanceof TaskTerminateEvent) {
            transition = ProcessNodeTransitionType.TERMINATE;
        } else {
            throw new ScyllaRuntimeException("Task event type not supported.");
        }

        for(TaskBeginEvent beginEvent : getParentalStartEvents()) {
            if (!getResponsibleProcessInstance().equals(beginEvent.getProcessInstance())) {
                ResourceObjectTuple commonResources = beginEvent.getProcessInstance().getAssignedResources().get(beginEvent.getSource());
                Set<String> resourceStrings = commonResources.getResourceObjects().stream()
                		.map(res -> res.getResourceType() + "_" + res.getId())
                		.collect(Collectors.toSet());
                ProcessNodeInfo info;
                info = new ProcessNodeInfo(nodeId, processScopeNodeId, beginEvent.getSource(), timestamp, taskName, resourceStrings,
                        transition);
                model.addNodeInfo(processModel, beginEvent.getProcessInstance(), info);
            }
        }
    }
 
    
    
	

}
