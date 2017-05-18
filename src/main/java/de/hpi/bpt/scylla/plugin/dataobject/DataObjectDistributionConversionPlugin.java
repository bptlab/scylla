package de.hpi.bpt.scylla.plugin.dataobject;

import java.util.Map;

import de.hpi.bpt.scylla.logger.DebugLogger;
import de.hpi.bpt.scylla.model.configuration.SimulationConfiguration;
import de.hpi.bpt.scylla.model.configuration.distribution.Distribution;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.plugin_type.simulation.DistributionConversionPluggable;
import de.hpi.bpt.scylla.simulation.ProcessSimulationComponents;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import de.hpi.bpt.scylla.simulation.utils.SimulationUtils;
import desmoj.core.dist.NumericalDist;

public class DataObjectDistributionConversionPlugin extends DistributionConversionPluggable {

    @Override
    public String getName() {
        return DataObjectPluginUtils.PLUGIN_NAME;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<Integer, Object> convertToDesmoJDistributions(ProcessSimulationComponents pSimComponents) {

        SimulationConfiguration simulationConfiguration = pSimComponents.getSimulationConfiguration();
        
		Map<Integer, Object> dataObjects 
        	= (Map<Integer, Object>) simulationConfiguration.getExtensionValue(getName(), "dataObjects");
        
        Long randomSeed = simulationConfiguration.getRandomSeed();
        ProcessModel processModel = pSimComponents.getProcessModel();

        SimulationModel model = pSimComponents.getModel();
        boolean showInReport = model.reportIsOn();
        boolean showInTrace = model.traceIsOn();
        
        for(Integer nodeId : dataObjects.keySet()) {
        	Map<String, DataObjectField> dataObjectFields = (Map<String, DataObjectField>) dataObjects.get(nodeId);
        	String name = processModel.getModelScopeId() + "_" + nodeId.toString();
        	
    		for(String fieldName : dataObjectFields.keySet()) {
    			DataObjectField dataObjectField = dataObjectFields.get(fieldName);
    			DataDistributionWrapper distWrapper = dataObjectField.getDataDistributionWrapper();
    			Distribution dist = distWrapper.getDistribution();
    			if(dist == null) continue;
    			
            	NumericalDist<?> desmojDist = null;
            	try {
            		desmojDist = SimulationUtils.getDistribution(dist, model, name, nodeId, showInReport, showInTrace);
				} catch (InstantiationException e) {
					DebugLogger.error(e.getMessage());
		            DebugLogger.error("Instantiation of dmn model failed.");
		            return null;
				}
            	desmojDist.setSeed(randomSeed);
            	distWrapper.setDesmojDistribution(desmojDist);
    		}
    	}
        
        return dataObjects;
    }
}