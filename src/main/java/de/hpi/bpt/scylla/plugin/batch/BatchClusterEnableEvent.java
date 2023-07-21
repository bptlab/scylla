package de.hpi.bpt.scylla.plugin.batch;

import co.paralleluniverse.fibers.SuspendExecution;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.event.TaskBeginEvent;
import de.hpi.bpt.scylla.simulation.event.TaskEnableEvent;

public class BatchClusterEnableEvent extends TaskEnableEvent{
	
	protected BatchCluster cluster;
	protected boolean hasAlreadyFired;


	public BatchClusterEnableEvent(ProcessInstance processInstance, BatchCluster cluster) {
		super(processInstance.getModel(), "BCEnable_" + cluster.getName(), processInstance.getModel().presentTime(), cluster.getProcessSimulationComponents(), processInstance, cluster.getBatchActivity().getNodeId());
		this.cluster = cluster;
	}
	
	@Override
	protected TaskBeginEvent createBeginEvent() {
		return new BatchClusterStartEvent(getProcessInstance(), cluster);
	}
	
	@Override
	public void eventRoutine(ProcessInstance processInstance) throws SuspendExecution {
        sendTraceNote("Enabling batch cluster "+cluster);
		super.eventRoutine(processInstance);
		hasAlreadyFired = true;
	}

	@Override
    protected void addToLog(ProcessInstance processInstance) {
    	//Shhhh TODO
    }


	public boolean hasAlreadyFired() {
		return hasAlreadyFired;
	}
}
