package de.hpi.bpt.scylla.plugin.batch;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.simulation.ProcessSimulationComponents;
import de.hpi.bpt.scylla.simulation.event.BPMNEndEvent;
import de.hpi.bpt.scylla.simulation.event.BPMNIntermediateEvent;
import de.hpi.bpt.scylla.simulation.event.BPMNStartEvent;
import de.hpi.bpt.scylla.simulation.event.TaskBeginEvent;
import de.hpi.bpt.scylla.simulation.event.TaskCancelEvent;
import de.hpi.bpt.scylla.simulation.event.TaskEnableEvent;
import de.hpi.bpt.scylla.simulation.event.TaskTerminateEvent;
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
		BatchPluginUtils.getInstance().logBPMNEventForNonResponsiblePI(event, event.getProcessInstance());
		super.startEvent(event);
	}
	
	@Override
	public void intermediateEvent(BPMNIntermediateEvent event) {
		BatchPluginUtils.getInstance().logBPMNEventForNonResponsiblePI(event, event.getProcessInstance());
		super.intermediateEvent(event);
	}
	
	@Override
	public void endEvent(BPMNEndEvent event) {
		BatchPluginUtils.getInstance().logBPMNEventForNonResponsiblePI(event, event.getProcessInstance());
		super.endEvent(event);
	}
	
	
	@Override
	public void taskEnableEvent(TaskEnableEvent event)  throws ScyllaRuntimeException{
		BatchPluginUtils.getInstance().logTaskEventForNonResponsiblePI(event, event.getProcessInstance());
		super.taskEnableEvent(event);
	}
	
	@Override
	public void taskBeginEvent(TaskBeginEvent event) throws ScyllaRuntimeException {
		BatchPluginUtils.getInstance().logTaskEventForNonResponsiblePI(event, event.getProcessInstance());
		super.taskBeginEvent(event);
	}
	
	@Override
	public void taskTerminateEvent(TaskTerminateEvent event) throws ScyllaRuntimeException {
		BatchPluginUtils.getInstance().logTaskEventForNonResponsiblePI(event, event.getProcessInstance());
		super.taskTerminateEvent(event);
	}
	
	@Override
	public void taskCancelEvent(TaskCancelEvent event) throws ScyllaRuntimeException {
		BatchPluginUtils.getInstance().logTaskEventForNonResponsiblePI(event, event.getProcessInstance());
		super.taskCancelEvent(event);
	}
	

}
