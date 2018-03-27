package de.hpi.bpt.scylla.plugin.batch;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Element;
import org.jdom2.Namespace;

import de.hpi.bpt.scylla.exception.ScyllaValidationException;
import de.hpi.bpt.scylla.logger.DebugLogger;
import de.hpi.bpt.scylla.model.configuration.SimulationConfiguration;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.plugin_type.parser.SimulationConfigurationParserPluggable;

// This class is not needed anymore. The parsing is done in the process model parser, because the batch activities are now defined as extension attributes in the BPMN file


/*public class BatchSCParserPlugin extends SimulationConfigurationParserPluggable {

	@Override
    public String getName() {
        return BatchPluginUtils.PLUGIN_NAME;
    }

    @Override
    public Map<String, Object> parse(SimulationConfiguration simulationInput, Element sim)
            throws ScyllaValidationException {

        Map<Integer, BatchActivity> batchActivities = new HashMap<Integer, BatchActivity>();

        Namespace simNamespace = sim.getNamespace();

        ProcessModel processModel = simulationInput.getProcessModel();

        for (Element el : sim.getChildren()) {
            String elementName = el.getName();

            String identifier = el.getAttributeValue("id");
            if (identifier == null) {
                DebugLogger.log("Warning: Simulation configuration definition element '" + elementName
                        + "' does not have an identifier, skip.");
                continue; // no matching element in process, so skip definition
            }
            Integer nodeId = processModel.getIdentifiersToNodeIds().get(identifier);
            if (nodeId == null) {
                DebugLogger.log("Simulation configuration definition for process element '" + identifier
                        + "', but not available in process, skip.");
                continue; // no matching element in process, so skip definition
            }

            if (elementName.equals("subProcess")) {
                Element elem = el.getChild("batchActivity", simNamespace);
                if (elem != null) {
                    // maximum batch size
                    Integer maxBatchSize = null;
                    Element maxBatchSizeElement = elem.getChild("maxBatchSize", simNamespace);
                    if (maxBatchSizeElement != null) {
                        maxBatchSize = Integer.parseInt(maxBatchSizeElement.getText());
                    }
                    // threshold capacity (minimum batch size) & timeout of activation rule
                    ActivationRule activationRule = null;
                    Element activationRuleElement = elem.getChild("activationRule", simNamespace);
                    if (activationRuleElement != null) {
                        List<Element> ruleElements = activationRuleElement.getChildren();
                        if (ruleElements.size() != 1) {
                            throw new ScyllaValidationException(
                                    "There must be exactly one activation rule for batch activity " + identifier
                                            + ", but there are " + ruleElements.size());
                        }
                        Element ruleElement = ruleElements.get(0);
                        String ruleElementName = ruleElement.getName();
                        if ("minMaxRule".equals(ruleElementName)) {
                            int minInstances = Integer.parseInt(ruleElement.getAttributeValue("minInstances"));
                            Duration minTimeout = Duration.parse(ruleElement.getAttributeValue("minTimeout"));
                            int maxInstances = Integer.parseInt(ruleElement.getAttributeValue("maxInstances"));
                            Duration maxTimeout = Duration.parse(ruleElement.getAttributeValue("maxTimeout"));
                            activationRule = new MinMaxRule(minInstances, minTimeout, maxInstances, maxTimeout);
                        }
                        else if ("thresholdRule".equals(ruleElementName)){
                        	
                        	// parsing threshold, if it is defined
                        	int threshold = 0;
                        	String thresholdString = ruleElement.getAttributeValue("threshold");
                        	if (thresholdString == null || thresholdString.isEmpty()){
                        	}else{
                        		threshold = Integer.parseInt(thresholdString);
                        	}
                        	
                        	//parsing timeout, if it is defined
                        	Duration timeout = null;
                            String timeoutString = ruleElement.getAttributeValue("timeout");
                            if (timeoutString != null){
                            	timeout = Duration.parse(timeoutString);
                            }
                            
                            //parsing dueDate, if it is defined
                            String dueDate = ruleElement.getAttributeValue("duedate");
                            
                            //either timeout or dueDate should not be null --> two different Constructors for the ThresholdRule
                            if (timeout != null){
                            	activationRule = new ThresholdRule(threshold, timeout);
                            }else if (dueDate != null){
                            	activationRule = new ThresholdRule(threshold, dueDate);
                            }else{
                            	throw new ScyllaValidationException("A threshold rule was selected for" + ruleElementName
                                        + " then either timeout or duedate must be specified.");
                            }
                            
                        }else{
                            throw new ScyllaValidationException("Activation rule type" + ruleElementName
                                    + " for batch activity " + identifier + " not supported.");
                        }
                    }
                    // grouping characteristic TODO not supported in simulation until data views are supported
                    List<String> groupingCharacteristic = new ArrayList<String>();
                    Element groupingCharacteristicElement = elem.getChild("groupingCharacteristic", simNamespace);
                    if (groupingCharacteristicElement != null) {
                        for (Element processVariableElement : groupingCharacteristicElement.getChildren()) {
                            if ("processVariable".equals(processVariableElement.getName())) {
                                groupingCharacteristic.add(processVariableElement.getText());
                            }
                        };
                    }

                    BatchActivity br = new BatchActivity(processModel, nodeId, maxBatchSize, activationRule,
                            groupingCharacteristic);
                    batchActivities.put(nodeId, br);
                }
            }
        }

        HashMap<String, Object> extensionAttributes = new HashMap<String, Object>();
        extensionAttributes.put("batchActivities", batchActivities);

        return extensionAttributes;
    }

}
*/