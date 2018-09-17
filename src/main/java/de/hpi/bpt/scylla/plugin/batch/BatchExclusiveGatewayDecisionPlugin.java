package de.hpi.bpt.scylla.plugin.batch;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.exception.ScyllaValidationException;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.model.process.graph.exception.NodeNotFoundException;
import de.hpi.bpt.scylla.model.process.node.GatewayType;
import de.hpi.bpt.scylla.plugin.gateway_exclusive.ExclusiveGatewayDecisionPluggable;
import de.hpi.bpt.scylla.plugin.gateway_exclusive.ExclusiveGatewayEventPlugin;
import de.hpi.bpt.scylla.plugin_loader.Requires;
import de.hpi.bpt.scylla.plugin_loader.TemporalDependent;
import de.hpi.bpt.scylla.plugin_loader.TemporalDependent.Execute;
import de.hpi.bpt.scylla.plugin_type.simulation.event.GatewayEventPluggable;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.event.GatewayEvent;
import de.hpi.bpt.scylla.simulation.event.ScyllaEvent;

@Requires(ExclusiveGatewayEventPlugin.class)
public class BatchExclusiveGatewayDecisionPlugin extends ExclusiveGatewayDecisionPluggable{
	
	private WeakHashMap<BatchCluster, Map<Integer, ScyllaEvent>> chosenPaths = new WeakHashMap<>();

	@Override
	public String getName() {
		return BatchPluginUtils.PLUGIN_NAME + "_exclusiveGateway";
	}
	
	@Override
	public Integer decideGateway(GatewayEvent event, ProcessInstance processInstance, Integer currentlyChosen) throws ScyllaRuntimeException {
		BatchPluginUtils pluginInstance = BatchPluginUtils.getInstance();
        
        BatchCluster cluster = pluginInstance.getCluster(processInstance);
        if (cluster == null) return null;
    	if(!chosenPaths.containsKey(cluster))return null;
    	
    	Integer nodeId = event.getNodeId();
    	Integer lastChosenNode = chosenPaths.get(cluster).get(nodeId).getNodeId();
        ProcessModel processModel = processInstance.getProcessModel();
    	try {
			Set<Integer> flowIds = processModel.getTargetObjectIds(nodeId);
	        for (Integer flowId : flowIds) {
	        	Integer targetNodeId = processModel.getTargetObjectIds(flowId).iterator().next();
	        	if(targetNodeId.equals(lastChosenNode))return flowId;
	        }
		} catch (NodeNotFoundException e) {
			e.printStackTrace();
		}
    	return null;
	}
	
	@Requires(BatchExclusiveGatewayDecisionPlugin.class)
	@TemporalDependent(ExclusiveGatewayEventPlugin.class)
	@TemporalDependent(value = BatchGatewayPlugin.class, execute=Execute.BEFORE)
	public class DecisionCapturePlugin extends GatewayEventPluggable{
		

		@Override
		public String getName() {
			return BatchExclusiveGatewayDecisionPlugin.this.getName();
		}

		@Override
		public void eventRoutine(GatewayEvent event, ProcessInstance processInstance) throws ScyllaRuntimeException {
			BatchPluginUtils pluginInstance = BatchPluginUtils.getInstance();
	        
	        BatchCluster cluster = pluginInstance.getCluster(processInstance);
	        if (cluster != null) {
	        	Integer nodeId = event.getNodeId();
	            ProcessModel processModel = processInstance.getProcessModel();
	            GatewayType type = processModel.getGateways().get(nodeId);
	            
	            if(type == GatewayType.EXCLUSIVE) {
	                try {
	                	Set<Integer> idsOfNextNodes = processModel.getIdsOfNextNodes(nodeId);
	                    if (idsOfNextNodes.size() > 1) {
	                    	Map<Integer, ScyllaEvent> nextEventMap = event.getNextEventMap();
	                    	assert nextEventMap.size() == 1;
	                    	ScyllaEvent nextEvent = nextEventMap.values().iterator().next();
	                    	if(!chosenPaths.containsKey(cluster)) {chosenPaths.put(cluster, new HashMap<Integer, ScyllaEvent>());}
	                    	if(!chosenPaths.get(cluster).containsKey(nodeId)) {
	                    		chosenPaths.get(cluster).put(nodeId, nextEvent);
	                    	}
	                    }
					} catch (AssertionError | NodeNotFoundException | ScyllaValidationException e) {
						e.printStackTrace();
					}
	            }
	        }
		}
		
	}

}
