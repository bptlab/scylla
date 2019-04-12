package de.hpi.bpt.scylla.plugin.batch;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.model.process.graph.exception.NodeNotFoundException;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.SimulationModel;
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
    private static List<ProcessInstance> getRunningInstances(ProcessInstance processInstance) {
    	return ((SimulationModel)processInstance.getModel()).getEntities(true).stream()
    			.filter(ProcessInstance.class::isInstance)
    			.map(each -> (ProcessInstance) each)
    			.filter(each -> each.getProcessModel().equals(processInstance.getProcessModel()))
    			.collect(Collectors.toList());
    }
    
    private static boolean areSimilarInstancesAvailable(Integer forNodeId, ProcessInstance inProcessInstance) {
        for (ProcessInstance runningInstance : getRunningInstances(inProcessInstance)) {
        	Set<Integer> activeNodes = runningInstance.getScheduledEvents().stream()
        			.filter(ScyllaEvent.class::isInstance)//ProcessInstanceGenerationEvents and similar must be filtered out
        			.map(each -> (ScyllaEvent)each)
        			.map(ScyllaEvent::getNodeId)
        			.collect(Collectors.toSet());
            if (inProcessInstance.getId() != runningInstance.getId() && willReach(runningInstance.getProcessModel(), activeNodes, forNodeId)) {
                BatchActivity batchActivity = BatchPluginUtils.getBatchActivities(inProcessInstance.getProcessModel()).get(forNodeId);
                if(batchActivity == null)throw new ScyllaRuntimeException("No batch activity found for node id "+forNodeId);
                if(batchActivity.getGroupingCharacteristic().stream()
                	.allMatch(each -> each.isFulfilledBetween(inProcessInstance, runningInstance))) {
                	return true;
                }
            }
        }
        return false;
    }
    
    public static boolean willReach(ProcessModel processModel, Set<Integer> activeNodeIds, Integer targetNodeId) {
    	Set<Integer> active = activeNodeIds.stream().collect(Collectors.toSet());
    	active.remove(targetNodeId);//Execution paths that have already reached the target node (e.g. batch region) should be excluded
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
		return false;
    }
    

}
