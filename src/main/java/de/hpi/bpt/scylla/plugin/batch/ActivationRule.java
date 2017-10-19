package de.hpi.bpt.scylla.plugin.batch;

import java.time.Duration;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.event.TaskBeginEvent;

public interface ActivationRule {
	
	public Duration getTimeOut(TaskBeginEvent desmojEvent, ProcessInstance processInstance);
	
	public int getThreshold(TaskBeginEvent desmojEvent, ProcessInstance processInstance);

}
