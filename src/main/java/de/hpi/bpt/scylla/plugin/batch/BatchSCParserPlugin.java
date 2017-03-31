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
import de.hpi.bpt.scylla.plugin_type.parser.ProcessModelParserPluggable;
import de.hpi.bpt.scylla.plugin_type.parser.SimulationConfigurationParserPluggable;

public class BatchSCParserPlugin extends ProcessModelParserPluggable {

    @Override
    public String getName() {
        return BatchPluginUtils.PLUGIN_NAME;
    }

    @Override
    public Map<String, Object> parse(ProcessModel processModel, Element bpmn)
            throws ScyllaValidationException {

        Map<Integer, BatchRegion> batchRegions = new HashMap<Integer, BatchRegion>();
        HashMap<String, Object> extensionAttributes = new HashMap<String, Object>();

        Namespace bpmnNamespace = bpmn.getNamespace();
        Namespace bptNamespace = bpmn.getNamespace("bpt");

        Element process = bpmn.getChild("process", bpmnNamespace);
        if (process == null) {
            return extensionAttributes;
        }
        for (Element el : process.getChildren()) {
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

            if (!elementName.equals("subProcess")) {
                continue;
            }
            
            Element extensionElement = el.getChild("extensionElements", bpmnNamespace);
            if (extensionElement == null) {
                
                continue;
            }
            
            Element elem = extensionElement.getChild("batchRegion", bptNamespace);
            if (elem == null) {
                continue;
            }
            
            // maximum batch size
            Integer maxBatchSize = null;
            Element maxBatchSizeElement = elem.getChild("maxBatchSize", bptNamespace);
            if (maxBatchSizeElement != null) {
                maxBatchSize = Integer.parseInt(maxBatchSizeElement.getText());
            }
            // threshold capacity (minimum batch size) & timeout of activation rule
            MinMaxRule minMaxRule = null;
            Element activationRuleElement = elem.getChild("activationRule", bptNamespace);
            if (activationRuleElement != null) {
                List<Element> ruleElements = activationRuleElement.getChildren();
                if (ruleElements.size() != 1) {
                    throw new ScyllaValidationException(
                            "There must be exactly one activation rule for batch region " + identifier
                                    + ", but there are " + ruleElements.size());
                }
                Element ruleElement = ruleElements.get(0);
                String ruleElementName = ruleElement.getName();
                if ("minMaxRule".equals(ruleElementName)) {
                    int minInstances = Integer.parseInt(ruleElement.getAttributeValue("minInstances"));
                    Duration minTimeout = Duration.parse(ruleElement.getAttributeValue("minTimeout"));
                    int maxInstances = Integer.parseInt(ruleElement.getAttributeValue("maxInstances"));
                    Duration maxTimeout = Duration.parse(ruleElement.getAttributeValue("maxTimeout"));
                    minMaxRule = new MinMaxRule(minInstances, minTimeout, maxInstances, maxTimeout);
                }
                else {
                    throw new ScyllaValidationException("Activation rule type" + ruleElementName
                            + " for batch region " + identifier + " not supported.");
                }
            }
            // grouping characteristic TODO not supported in simulation until data views are supported
            List<String> groupingCharacteristic = new ArrayList<String>();
            Element groupingCharacteristicElement = elem.getChild("groupingCharacteristic", bptNamespace);
            if (groupingCharacteristicElement != null) {
                for (Element processVariableElement : groupingCharacteristicElement.getChildren()) {
                    if ("processVariable".equals(processVariableElement.getName())) {
                        groupingCharacteristic.add(processVariableElement.getText());
                    }
                };
            }
            BatchRegion br = new BatchRegion(processModel, nodeId, maxBatchSize, minMaxRule,
                    groupingCharacteristic);
            batchRegions.put(nodeId, br);
        }

        extensionAttributes.put("batchRegions", batchRegions);

        return extensionAttributes;
    }

}
