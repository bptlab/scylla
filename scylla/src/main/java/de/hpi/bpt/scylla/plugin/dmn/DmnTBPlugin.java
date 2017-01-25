package de.hpi.bpt.scylla.plugin.dmn;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.hpi.bpt.drools.rules_engine.DroolsRuleEngine;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.exception.ScyllaValidationException;
import de.hpi.bpt.scylla.logger.DebugLogger;
import de.hpi.bpt.scylla.model.configuration.distribution.EmpiricalStringDistribution;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.model.process.graph.exception.NodeNotFoundException;
import de.hpi.bpt.scylla.model.process.node.TaskType;
import de.hpi.bpt.scylla.plugin.dataobject.DataDistributionWrapper;
import de.hpi.bpt.scylla.plugin.dataobject.DataObjectField;
import de.hpi.bpt.scylla.plugin.dataobject.DataObjectPluginUtils;
import de.hpi.bpt.scylla.plugin_type.simulation.event.TaskBeginEventPluggable;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.ProcessSimulationComponents;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import de.hpi.bpt.scylla.simulation.event.TaskBeginEvent;
import de.hpi.bpt.scylla.simulation.utils.SimulationUtils;
import desmoj.core.dist.NumericalDist;

public class DmnTBPlugin extends TaskBeginEventPluggable {

    @Override
    public String getName() {
        return DmnPluginUtils.PLUGIN_NAME;
    }

    @SuppressWarnings("unchecked")
	@Override
    public void eventRoutine(TaskBeginEvent desmojEvent, ProcessInstance processInstance)
            throws ScyllaRuntimeException {

    	SimulationModel model = (SimulationModel) desmojEvent.getModel();
        ProcessModel processModel = processInstance.getProcessModel();
        ProcessSimulationComponents desmojObjects = desmojEvent.getDesmojObjects();
        int nodeId = desmojEvent.getNodeId();
        boolean showInTrace = desmojEvent.traceIsOn();
        boolean showInReport = model.reportIsOn();
        String name = processModel.getModelScopeId() + "_" + nodeId;

    	// stop, if task is something other than business rule task
    	if(TaskType.BUSINESS_RULE != processModel.getTasks().get(nodeId)) {
    		return;
    	}
    	
    	try {
    		
		// ***************************
		// GET INPUT
		// ***************************	
    		
    		// fetch source data object ids
    		Set<Integer> sourceDataObjectIds = null;
			
			try {
				sourceDataObjectIds = processModel.getDataObjectsGraph().getSourceObjects(nodeId);
			} catch (NodeNotFoundException e1) {
				DebugLogger.error(e1.getMessage());
	            DebugLogger.error(nodeId + " does not have any input data objects.");
			}
    				
	    	// fetch all available data objects
	    	Map<Integer, Object> dataObjects = 
	    			(Map<Integer, Object>) desmojObjects.getExtensionDistributions()
	    			.get(DataObjectPluginUtils.PLUGIN_NAME);
	    	
	    	Map<String, Object> simulationInput = new HashMap<String, Object>();
	    	
	    	// iterate over all available data objects
			for(int dataObjectId : dataObjects.keySet()){
				
				// pick the ones that are included in the source data object ids
				if(sourceDataObjectIds.contains(dataObjectId)){
					
					// fetch all data fields
					Map<String, DataObjectField> dataObjectFields = 
							(Map<String, DataObjectField>) dataObjects.get(dataObjectId);
					String dataObjectName = processModel.getDisplayNames().get(dataObjectId);
				
					for(String fieldName : dataObjectFields.keySet()){
						
						// create the key by concatenating dataObjectName + fieldName and assure that the first letter is lower case
		            	String key = dataObjectName + "." + fieldName;
		            	
		            	// handle the distribution of every data field
		            	DataObjectField dataObjectField = dataObjectFields.get(fieldName);
		            	DataDistributionWrapper distWrapper = dataObjectField.getDataDistributionWrapper();
		            	simulationInput.put(key, distWrapper.getSample());
					}
				}
			}
			
			
			// TODO if you want to have a more sophisticated comparison at a gateway how to branch according to a dmn result,
			// you need to support more data types here besides string in the hash map value
			// right now it's just a simple string equality comparison 
			Map<String, String> simulationOutput = new HashMap<String, String>();
			
			
			// ***************************
			// RULE EVALUATION
			// ***************************

			RulesEngine re = new DroolsRuleEngine();
			re.setup();
			simulationOutput = re.evaluateRules(simulationInput);
			
			// TODO add multiple hit policy handling
			// currently only one output is supported
			if (simulationOutput.size() != 1) {
				throw new ScyllaValidationException(
						"The DMN rule evaluation of node " + nodeId + 
						" has not 1 output, but" + simulationOutput.size() + " .");
			}
			
			// ***************************
			// LOG STATS
			// ***************************			
			
			String source = desmojEvent.getSource();
			String processScopeNodeId = SimulationUtils.getProcessScopeNodeId(processModel, nodeId);
			String taskName = desmojEvent.getDisplayName();
			DmnNodeInfo info = new DmnNodeInfo(processScopeNodeId, source, simulationInput, simulationOutput, taskName);
			DmnPluginUtils dmnPluginUtils = DmnPluginUtils.getInstance();
			dmnPluginUtils.addDmnNodeInfo(processModel, processInstance, info);
			
			
			// ***************************
			// STORE OUTPUT
			// ***************************
			
			Set<Integer> outputObjectReferences = null;
			
			try {
				outputObjectReferences = processModel.getDataObjectsGraph().getTargetObjects(nodeId);
			} catch (NodeNotFoundException e1) {
				DebugLogger.error(e1.getMessage());
	            DebugLogger.error(nodeId + " does not have an output data objects.");
			}
			
			// only one output is supported
			if (outputObjectReferences.size() != 1) {
				throw new ScyllaValidationException(
						"The node " + nodeId + " has not 1 output data element, but" + outputObjectReferences.size() + " .");
			}
            
			// translate the connection of object reference and data object
            int outputObjectReferenceId = outputObjectReferences.iterator().next();
            String outputObjectIdentifier = processModel.getDataObjectReferences().get(outputObjectReferenceId);
            int outputObjectId = processModel.getIdentifiersToNodeIds().get(outputObjectIdentifier);

            Map<String, DataObjectField> dataObjectFields = 
					(Map<String, DataObjectField>) dataObjects.get(outputObjectId);
            
            // only one output is supported
            if (dataObjectFields.size() != 1) {
				throw new ScyllaValidationException(
						"The node " + nodeId + " has not 1 output field, but" + dataObjectFields.size() + " .");
			}
            
            String dataObjectFieldName = dataObjectFields.entrySet().iterator().next().getKey();
            DataObjectField dataObjectField = dataObjectFields.get(dataObjectFieldName);
		
            // only one output is supported - was checked earlier
            String value = simulationOutput.entrySet().iterator().next().getValue();
			
			// every data that is available in the system is represented by distributions
			// therefore the output will also be a distribution
			// but as there is only one output, an empirical string distribution is used
			// to store one value with 100% frequency
			EmpiricalStringDistribution dist = new EmpiricalStringDistribution();
			dist.addEntry(value, 1);
			NumericalDist<?> desmojDist = null;
        	try {
        		desmojDist = SimulationUtils.getDistribution(dist, model, name, nodeId, showInReport, showInTrace);
			} catch (InstantiationException e) {
				DebugLogger.error(e.getMessage());
	            DebugLogger.error("Instantiation of dmn model failed.");
			}
			
        	dataObjectField.getDataDistributionWrapper().setDistribution(dist);
        	dataObjectField.getDataDistributionWrapper().setDesmojDistribution(desmojDist);
			

		} catch (ScyllaValidationException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            SimulationUtils.abort(model, processInstance, nodeId, showInTrace);
            return;
        }
    }
}
