package de.hpi.bpt.scylla.plugin.eventArrivalRate;

import java.util.HashMap;
import java.util.Map;

import org.jdom2.Element;
import org.jdom2.Namespace;

import de.hpi.bpt.scylla.exception.ScyllaValidationException;
import de.hpi.bpt.scylla.logger.DebugLogger;
import de.hpi.bpt.scylla.model.configuration.SimulationConfiguration;
import de.hpi.bpt.scylla.model.configuration.distribution.TimeDistributionWrapper;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.parser.SimulationConfigurationParser;
import de.hpi.bpt.scylla.plugin_type.parser.SimulationConfigurationParserPluggable;

/**
 * This plugin class parses intermediate event arrival rates from the simulation configuration,
 * so they can be put to an extension attribute.
 * @author Leon Bein
 *
 */
public class EventArrivalRateSCParserPlugin extends SimulationConfigurationParserPluggable{

	@Override
	public String getName() {
		return EventArrivalRatePluginUtils.PLUGIN_NAME;
	}

	/**
	 * Parses all occurences of event arrival rates to be stored as extension attribute.
	 */
	@Override
	public Map<String, Object> parse(SimulationConfiguration simulationInput, Element sim)
			throws ScyllaValidationException {
		
		Map<Integer, TimeDistributionWrapper> arrivalRates = new HashMap<Integer, TimeDistributionWrapper>();
        Namespace simNamespace = sim.getNamespace();
        ProcessModel processModel = simulationInput.getProcessModel();

        for(Element el : sim.getChildren()) {
            String elementName = el.getName();
        	
        	if(elementName.equals(EventArrivalRatePluginUtils.ELEMENT_NAME)) {
        		
                String identifier = el.getAttributeValue("id");
                if (identifier == null) {
                    DebugLogger.log("Warning: Simulation configuration definition catch event element '" + elementName
                            + "' does not have an identifier, skip.");
                    continue;
                }
                
                Integer nodeId = processModel.getIdentifiersToNodeIds().get(identifier);
                if (nodeId == null) {
                    DebugLogger.log("Warning: There is no matching catch event in the process model for "
                    		+ "simulation configuration definition '" + identifier+ ", skip.");
                    continue;
                }
                
                Element elem = el.getChild("arrivalRate", simNamespace);
                if (elem != null) {
                    TimeDistributionWrapper distribution = SimulationConfigurationParser.getTimeDistributionWrapper(elem, simNamespace);
                    arrivalRates.put(nodeId, distribution);
                }
        	}
        	
        }
		
		
        Map<String, Object> extensionAttributes = new HashMap<String, Object>();
        extensionAttributes.put(EventArrivalRatePluginUtils.ARRIVALRATES_KEY, arrivalRates);
        return extensionAttributes;
	}

}
