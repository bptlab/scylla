package de.hpi.bpt.scylla.plugin.batch;

import co.paralleluniverse.fibers.SuspendExecution;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.event.ScyllaEvent;
import desmoj.core.simulator.TimeInstant;

public class BatchStashResourceEvent extends ScyllaEvent{

	public BatchStashResourceEvent(BatchCluster cluster, Integer nodeId) {
		super(cluster.getModel(), cluster.getName(), new TimeInstant(0), cluster.getProcessSimulationComponents(), cluster.getResponsibleProcessInstance(), nodeId);
	}

	@Override
	public void eventRoutine(ProcessInstance arg0) throws SuspendExecution {
		
	}

}
