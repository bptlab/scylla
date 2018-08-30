package de.hpi.bpt.scylla.plugin.batch;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.exception.ScyllaValidationException;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.model.process.graph.exception.NodeNotFoundException;
import de.hpi.bpt.scylla.model.process.node.GatewayType;
import de.hpi.bpt.scylla.plugin.gateway_exclusive.ExclusiveGatewayEventPlugin;
import de.hpi.bpt.scylla.plugin_loader.Requires;
import de.hpi.bpt.scylla.plugin_loader.TemporalDependent;
import de.hpi.bpt.scylla.plugin_type.simulation.event.GatewayEventPluggable;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.ProcessSimulationComponents;
import de.hpi.bpt.scylla.simulation.event.GatewayEvent;
import de.hpi.bpt.scylla.simulation.event.ScyllaEvent;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

@Requires(ExclusiveGatewayEventPlugin.class)
@TemporalDependent(ExclusiveGatewayEventPlugin.class)
public class BatchExclusiveGatewayPlugin extends GatewayEventPluggable{
	
	private WeakHashMap<BatchCluster, Map<Integer, ScyllaEvent>> chosenPaths = new WeakHashMap<>();

	@Override
	public String getName() {
		return BatchPluginUtils.PLUGIN_NAME + "_exclusiveGateway";
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
                    	Integer nextEventKey = nextEventMap.keySet().iterator().next();
                    	ScyllaEvent nextEvent = nextEventMap.values().iterator().next();
                    	if(!chosenPaths.containsKey(cluster)) {chosenPaths.put(cluster, new HashMap<Integer, ScyllaEvent>());}
                    	if(!chosenPaths.get(cluster).containsKey(nodeId)) {
                    		chosenPaths.get(cluster).put(nodeId, nextEvent);
                    	}else {
                    		ScyllaEvent chosenPath = chosenPaths.get(cluster).get(nodeId);
                        	if(nextEvent.getNodeId() != chosenPath.getNodeId()) {
                        		ScyllaEvent newEvent = null;
                        		try {
                        			//TODO only works most of the time, wait for plugin system to improve
                        			Constructor<? extends ScyllaEvent> constructor = chosenPath.getClass().getConstructor(Model.class, String.class, TimeInstant.class, ProcessSimulationComponents.class, ProcessInstance.class, int.class);
                        			newEvent = constructor.newInstance(chosenPath.getModel(), chosenPath.getSource(), chosenPath.getSimulationTimeOfSource(), chosenPath.getSimulationComponents(), event.getProcessInstance(), chosenPath.getNodeId());
                        		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
									e.printStackTrace();
								}
                        		nextEventMap.put(nextEventKey, newEvent);
                        	}
                    	}
                    }
				} catch (AssertionError | NodeNotFoundException | ScyllaValidationException e) {
					e.printStackTrace();
				}
            }
        }
	}

}
