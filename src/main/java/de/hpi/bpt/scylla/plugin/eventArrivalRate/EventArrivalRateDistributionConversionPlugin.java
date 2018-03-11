package de.hpi.bpt.scylla.plugin.eventArrivalRate;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.AbstractMap.SimpleEntry;
import java.util.concurrent.TimeUnit;


import de.hpi.bpt.scylla.model.configuration.SimulationConfiguration;
import de.hpi.bpt.scylla.model.configuration.distribution.Distribution;
import de.hpi.bpt.scylla.model.configuration.distribution.TimeDistributionWrapper;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.plugin_type.simulation.DistributionConversionPluggable;
import de.hpi.bpt.scylla.simulation.ProcessSimulationComponents;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import de.hpi.bpt.scylla.simulation.utils.SimulationUtils;
import desmoj.core.dist.NumericalDist;

/**
 * This plugin class converts scylla distributions to distributions that can be used by desmoJ,
 * and stores them as distribution extensional.
 * @author Leon Bein
 *
 */
public class EventArrivalRateDistributionConversionPlugin extends DistributionConversionPluggable{

	@Override
	public String getName() {
		return EventArrivalRatePluginUtils.PLUGIN_NAME;
	}

	
	/**
	 * Converts scylla distributions from extension to desmoJ distributions to be stored als distribution extension.
	 * @see {@link de.hpi.bpt.scylla.simulation.utils.SimulationUtils#getDistribution(Distribution, SimulationModel, String, Integer, boolean, boolean)}
	 */
	@Override
	public Map<Integer, Object> convertToDesmoJDistributions(ProcessSimulationComponents pSimComponents) {
		Map<Integer, Object> arrivalRateDistributions = new HashMap<Integer, Object>();
		
        SimulationConfiguration simulationConfiguration = pSimComponents.getSimulationConfiguration();
        Long randomSeed = simulationConfiguration.getRandomSeed();
        /**Get saved extension from sc parser*/
        @SuppressWarnings("unchecked")
		HashMap<Integer, TimeDistributionWrapper> arrivalRates = (HashMap<Integer, TimeDistributionWrapper>) simulationConfiguration.getExtensionValue(getName(),EventArrivalRatePluginUtils.ARRIVALRATES_KEY);

        ProcessModel processModel = pSimComponents.getProcessModel();
        SimulationModel model = pSimComponents.getModel();
        boolean showInReport = model.reportIsOn();
        boolean showInTrace = model.traceIsOn();
        
        for(Entry<Integer, TimeDistributionWrapper> entry : arrivalRates.entrySet()) {
        	Integer nodeId = entry.getKey();
        	TimeDistributionWrapper arrivalRate = entry.getValue();
        	TimeUnit timeUnit = arrivalRate.getTimeUnit();
            Distribution distribution = arrivalRate.getDistribution();
            

            String name = processModel.getModelScopeId() + "_" + nodeId.toString();
        	try {
				NumericalDist<?> desmojDist = SimulationUtils.getDistribution(distribution, model, name, nodeId, showInReport, showInTrace);
				desmojDist.setSeed(randomSeed);
				arrivalRateDistributions.put(nodeId, new SimpleEntry<NumericalDist<?>,TimeUnit>(desmojDist,timeUnit));
        	} catch (InstantiationException e) {
				e.printStackTrace();
				continue;
			}
        }
        
		
		return arrivalRateDistributions;
	}

}
