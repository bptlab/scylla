package de.hpi.bpt.scylla.plugin.batch;

import java.time.Duration;

import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.event.ScyllaEvent;
import de.hpi.bpt.scylla.simulation.event.TaskBeginEvent;

/**
 * The activation rule of a batch cluster decides, when to start it.
 * Normally a threshold provides a minimum number of instances to start (TODO yet nothing happens when the threshold is reached)
 * and a timeout defines a maximum date until the cluster starts.
 * Note: The starting behavior of batch clusters is also influenced by their max batch size
 * @author was not Leon Bein
 *
 */
public interface ActivationRule {

	/**
	 * Time after that the batch cluster is started, no matter how many instances it contains
	 * The begin event is scheduled for the timeout of the first instance
	 * and then rescheduled for every instance that has a lower timeout
	 * @param desmojEvent
	 * @param processInstance
	 * @return
	 */
	public Duration getTimeOut(TaskBeginEvent desmojEvent, ProcessInstance processInstance);
	

	/**
	 * Number of instances in the unstarted batch cluster, that will automatically enable it.
	 * Actually does nothing.
	 * @param desmojEvent : Begin event of the cluster
	 * @param processInstance : The process instance of the begin event TODO: could be queried at the event via {@link ScyllaEvent#getProcessInstance()}?
	 */
	public int getThreshold(TaskBeginEvent desmojEvent, ProcessInstance processInstance);

}
