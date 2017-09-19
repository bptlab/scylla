package de.hpi.bpt.scylla.plugin.gateway_exclusive;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.exception.ScyllaValidationException;
import de.hpi.bpt.scylla.logger.ProcessNodeInfo;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.model.process.graph.exception.NodeNotFoundException;
import de.hpi.bpt.scylla.model.process.node.GatewayType;
import de.hpi.bpt.scylla.plugin_type.simulation.event.GatewayEventPluggable;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.ProcessSimulationComponents;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import de.hpi.bpt.scylla.simulation.event.GatewayEvent;
import de.hpi.bpt.scylla.simulation.event.ScyllaEvent;
import de.hpi.bpt.scylla.simulation.utils.SimulationUtils;
import desmoj.core.dist.DiscreteDistEmpirical;
import desmoj.core.simulator.TimeSpan;

public class ExclusiveGatewayEventPlugin extends GatewayEventPluggable {

    @Override
    public String getName() {
        return ExclusiveGatewayPluginUtils.PLUGIN_NAME;
    }
    
    private void scheduleNextEvent(GatewayEvent desmojEvent, ProcessInstance processInstance, ProcessModel processModel, Integer nextFlowId) {
    	Set<Integer> nodeIds = null;
		try {
			nodeIds = processModel.getTargetObjectIds(nextFlowId);
		} catch (NodeNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        if (nodeIds.size() != 1) {
            try {
				throw new ScyllaValidationException(
				        "Flow " + nextFlowId + " does not connect to 1 node, but" + nodeIds.size() + " .");
			} catch (ScyllaValidationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        int nextNodeId = nodeIds.iterator().next();

        Map<Integer, ScyllaEvent> nextEventMap = desmojEvent.getNextEventMap();
        List<Integer> indicesOfEventsToKeep = new ArrayList<Integer>();
        for (int index : nextEventMap.keySet()) {
            ScyllaEvent eventCandidate = nextEventMap.get(index);
            int nodeIdOfCandidate = eventCandidate.getNodeId();
            if (nodeIdOfCandidate == nextNodeId) {
                indicesOfEventsToKeep.add(index);
                break;
            }
        }
        Map<Integer, TimeSpan> timeSpanToNextEventMap = desmojEvent.getTimeSpanToNextEventMap();
        nextEventMap.keySet().retainAll(indicesOfEventsToKeep);
        timeSpanToNextEventMap.keySet().retainAll(indicesOfEventsToKeep);
    }
    @SuppressWarnings("unchecked")
    @Override
    public void eventRoutine(GatewayEvent desmojEvent, ProcessInstance processInstance) throws ScyllaRuntimeException {
        SimulationModel model = (SimulationModel) desmojEvent.getModel();
        ProcessModel processModel = processInstance.getProcessModel();
        int nodeId = desmojEvent.getNodeId();

        boolean showInTrace = desmojEvent.traceIsOn();

        GatewayType type = processModel.getGateways().get(nodeId);
        ProcessSimulationComponents desmojObjects = desmojEvent.getDesmojObjects();

        try {
            Set<Integer> idsOfNextNodes = processModel.getIdsOfNextNodes(nodeId);

            if (idsOfNextNodes.size() > 1) { // split
                if (type == GatewayType.DEFAULT || type == GatewayType.EXCLUSIVE || type == GatewayType.EVENT_BASED) {
                    Map<Integer, Object> branchingDistributions = desmojObjects.getExtensionDistributions()
                            .get(getName());
                    DiscreteDistEmpirical<Integer> distribution = (DiscreteDistEmpirical<Integer>) branchingDistributions
                            .get(nodeId);
                    // decide on next node
                    if (distribution != null) {
	                    Integer nextFlowId = distribution.sample().intValue();
	                    if (!processModel.getIdentifiers().keySet().contains(nextFlowId)) {
	                        throw new ScyllaValidationException("Flow with id " + nextFlowId + " does not exist.");
	                    }
	                    scheduleNextEvent(desmojEvent, processInstance, processModel, nextFlowId);
	
                    } else { //does not really work out atm because if now display name is given the id is taken as the display name --> its never null
                    	Object[] outgoingRefs = processModel.getGraph().getTargetObjects(nodeId).toArray();
                    	Integer DefaultPath = null;
                    	Boolean foundAWay = false;
                    	for (Object or : outgoingRefs) {
                    		if (or.equals(getKeyByValue(processModel.getIdentifiers(),processModel.getNodeAttributes().get(desmojEvent.getNodeId()).get("default"))) == true) {
                    			DefaultPath = (Integer) or;
                    			continue;
                    		}
                    		Object[] conditions = processModel.getDisplayNames().get(or).split("&&");
                    		Integer nextFlowId = (Integer) or;
                    		List<Boolean> test = new ArrayList<>();
                    		for (Object condition : conditions) {
                    			condition = ((String) condition).trim();
                    			String field = null;
	                    		String value = null;
	                    		String comparison = null;
	                    		
	                    		if (((String) condition).contains("==")) {
	                    			field = ((String) condition).split("==")[0];
	                    			value = ((String) condition).split("==")[1];
	                    			//value = processModel.getDisplayNames().get(or).substring(2, processModel.getDisplayNames().get(or).length());
	                    			comparison = "equal";
	                    		}
	                    		else if (((String) condition).contains(">=")) {
	                    			field = ((String) condition).split(">=")[0];
	                    			value = ((String) condition).split(">=")[1];
	                    			comparison = "greaterOrEqual";
	                    		}
	                    		else if (((String) condition).contains("<=")) {
	                    			field = ((String) condition).split("<=")[0];
	                    			value = ((String) condition).split("<=")[1];
	                    			comparison = "lessOrEqual";
	                    		}
	                    		else if (((String) condition).contains("!=")) {
	                    			field = ((String) condition).split("!=")[0];
	                    			value = ((String) condition).split("!=")[1];
	                    			comparison = "notEqual";
	                    		}
	                    		else if (((String) condition).contains("=")) {
	                    			field = ((String) condition).split("=")[0];
	                    			value = ((String) condition).split("=")[1];
	                    			comparison = "equal";
	                    		}
	                    		else if (((String) condition).contains("<")) {
	                    			field = ((String) condition).split("<")[0];
	                    			value = ((String) condition).split("<")[1];
	                    			comparison = "less";
	                    		}
	                    		else if (((String) condition).contains(">")) {
	                    			field = ((String) condition).split(">")[0];
	                    			value = ((String) condition).split(">")[1];
	                    			comparison = "greater";
	                    		}
	                    		else {
	                    			throw new ScyllaValidationException(
	                                        "Condition " + condition + " does not have a comparison-operator");
	                            }
	                    		value = value.trim();
	                    		field = field.trim();
	                    		
	                    		Collection<Map<Integer, java.util.List<ProcessNodeInfo>>> allProcesses = model.getProcessNodeInfos().values();
								for (Map<Integer, java.util.List<ProcessNodeInfo>> process : allProcesses) {
									List<ProcessNodeInfo> currentProcess = process.get(processInstance.getId());
									for (ProcessNodeInfo task : currentProcess) {
										Map<String, Object> dataObjectField = task.getDataObjectField();
										for (Map.Entry<String, Object> dO : dataObjectField.entrySet()){
										    if (dO.getKey().equals(field)) {
										    	if (!isParsableAsLong(value) || !isParsableAsLong((String.valueOf(dO.getValue())))) {
										    		Integer comparisonResult = (String.valueOf(dO.getValue())).trim().compareTo(String.valueOf(value));
										    		if (comparison.equals("equal") && comparisonResult == 0) {
										    			break;
											    	} else if (comparison.equals("notEqual") && comparisonResult != 0) {
											    		break;
											    	} else {
											    		test.add(false);
											    	}
										    		
										    	} else if (isParsableAsLong(value)) {
										    		Long LongValue = Long.valueOf(value);
										    		Long dOValue = Long.valueOf((String.valueOf(dO.getValue())));
										    		Integer comparisonResult = (dOValue.compareTo(LongValue));
											    	
										    		if (comparison.equals("equal") && comparisonResult == 0) {	
										    			break;
										    		}
											    	else if (comparison.equals("less") &&  comparisonResult < 0) {
											    		break;
											    	}
											    	else if (comparison.equals("greater") &&  comparisonResult > 0) {
											    		break;
											    	}
											    	else if (comparison.equals("greaterOrEqual") && comparisonResult >= 0) {
											    		break;
											    	}
											    	else if (comparison.equals("lessOrEqual") && comparisonResult <= 0) {
											    		break;
											    	}
											    	else {
											    		test.add(false);
											    	}
										    	}
										    }
										}
									}
								}
	                    	}
                    		if (test.size() == 0) {
					    		scheduleNextEvent(desmojEvent, processInstance, processModel, nextFlowId);
					    		foundAWay = true;
                    		}
                		}
                    	if (foundAWay == false) {
                    		scheduleNextEvent(desmojEvent, processInstance, processModel, DefaultPath);
                    	}
                    } 
                }
            }
        }
        catch (NodeNotFoundException | ScyllaValidationException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            SimulationUtils.abort(model, processInstance, nodeId, showInTrace);
            return;
        }
    }

	private static boolean isParsableAsLong(final String s) {
        try {
            Long.valueOf(s);
            return true;
        } catch (NumberFormatException numberFormatException) {
            return false;
        }
    }
	public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
	    for (Entry<T, E> entry : map.entrySet()) {
	        if (Objects.equals(value, entry.getValue())) {
	            return entry.getKey();
	        }
	    }
	    return null;
	}
}
