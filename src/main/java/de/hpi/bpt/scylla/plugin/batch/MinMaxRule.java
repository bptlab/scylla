package de.hpi.bpt.scylla.plugin.batch;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.model.process.graph.exception.NodeNotFoundException;
import de.hpi.bpt.scylla.plugin_type.simulation.event.BPMNEndEventPluggable;
import de.hpi.bpt.scylla.plugin_type.simulation.event.BPMNStartEventPluggable;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.event.BPMNEndEvent;
import de.hpi.bpt.scylla.simulation.event.BPMNStartEvent;
import de.hpi.bpt.scylla.simulation.event.ScyllaEvent;
import de.hpi.bpt.scylla.simulation.event.TaskBeginEvent;

/**
 * A MinMaxRule wants to group process instances with similar grouping characteristics.
 * We use data attributes as grouping characteristics.<br>
 * The rule defines two sets of timeout and threshold:<br>
 * When at least one other instance with the same grouping characteristic is running,<br>
 * &nbsp;&nbsp;&nbsp;&nbsp; the set with higher threshold and timeout is used, <br>
 * otherwise <br>
 * &nbsp;&nbsp;&nbsp;&nbsp; the lower threshold and timeout are used.
 * @author Leon Bein
 */
public class MinMaxRule implements ActivationRule {

    private int minInstances;
    private Duration minTimeout;
    private int maxInstances;
    private Duration maxTimeout;

    public MinMaxRule(int minInstances, Duration minTimeout, int maxInstances, Duration maxTimeout) {
        this.minInstances = minInstances;
        this.minTimeout = minTimeout;
        this.maxInstances = maxInstances;
        this.maxTimeout = maxTimeout;
    }


    public int getThreshold(TaskBeginEvent desmojEvent, ProcessInstance processInstance) {
    	if(areSimilarInstancesAvailable(desmojEvent.getNodeId(), processInstance))return maxInstances;
    	else return minInstances;
    }

    public Duration getTimeOut(TaskBeginEvent desmojEvent, ProcessInstance processInstance) {
    	if(areSimilarInstancesAvailable(desmojEvent.getNodeId(), processInstance))return maxTimeout;
    	else return minTimeout;
    }
    
    
    /**
     * @return the information to all currently running process instances, i.o. to be able to identify similar instances that are to reach the batch region
     */
    private static List<ProcessInstance> getRunningInstances() {
    	return ActiveInstanceLogger.instance.activeInstances;
    }
    
    private static boolean areSimilarInstancesAvailable(Integer forNodeId, ProcessInstance inProcessInstance) {
        for (ProcessInstance runningInstance : getRunningInstances()) {
        	Set<Integer> activeNodes = runningInstance.getScheduledEvents().stream()
        			.map(each -> (ScyllaEvent)each)
        			.map(ScyllaEvent::getNodeId)
        			.collect(Collectors.toSet());
            if (inProcessInstance.getId() != runningInstance.getId() && willReach(runningInstance.getProcessModel(), activeNodes, forNodeId)) {
                BatchActivity batchActivity = BatchPluginUtils.getBatchActivities(inProcessInstance.getProcessModel()).get(forNodeId);
                if(batchActivity == null)throw new ScyllaRuntimeException("No batch activity found for node id "+forNodeId);
                if(batchActivity.getGroupingCharacteristic().stream()
                	.allMatch(each -> each.isFulfilledBetween(inProcessInstance, runningInstance))) return true;
            }
        }
        return false;
    }
    
    private static boolean willReach(ProcessModel processModel, Set<Integer> activeNodeIds, Integer targetNodeId) {
    	Set<Integer> active = activeNodeIds.stream().collect(Collectors.toSet());
    	Set<Integer> visited = new HashSet<>();
    	Integer current;
    	while(!active.isEmpty()) {
    		current = active.stream().findAny().get();
    		if(current == targetNodeId)return true;
    		active.remove(current);
    		visited.add(current);
    		try {
				processModel.getGraph().getTargetObjects(current).stream()
					.filter(each -> !visited.contains(each))
					.forEach(active::add);
			} catch (NodeNotFoundException e) {
				throw new ScyllaRuntimeException("There has been an error at process model graph traversal", e);
			}
    	}
		return true;
    }
    
    /**
     * TODO: This is quite hacky
     * => Refactor it!
     * @author Leon Bein
     *
     */
    public static class ActiveInstanceLogger extends BPMNStartEventPluggable{
    	
    	private static ActiveInstanceLogger instance;
    	{
    		instance = this;
    	}
    	
    	private List<ProcessInstance> activeInstances = new ArrayList<>();

		@Override
		public String getName() {
			return BatchPluginUtils.PLUGIN_NAME+"_"+"MinMaxRule";
		}

		@Override
		public void eventRoutine(BPMNStartEvent startEvent, ProcessInstance processInstance) throws ScyllaRuntimeException {
			activeInstances.add(processInstance);
		}
		
		public class TerminatedTaskRemove extends BPMNEndEventPluggable {

			@Override
			public String getName() {
				return ActiveInstanceLogger.this.getName();
			}

			@Override
			public void eventRoutine(BPMNEndEvent endEvent, ProcessInstance processInstance) throws ScyllaRuntimeException {
				activeInstances.remove(processInstance);
			}
			
		}
    	
    }

}
