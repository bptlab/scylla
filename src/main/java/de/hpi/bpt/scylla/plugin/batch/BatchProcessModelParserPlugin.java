package de.hpi.bpt.scylla.plugin.batch;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jdom2.Element;
import org.jdom2.Namespace;

import de.hpi.bpt.scylla.exception.ScyllaValidationException;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.plugin_type.parser.ProcessModelParserPluggable;

/**
 * I parse batch activities specified in the process model.<br>
 * The activities that I parse can later be found at the {@link ProcessModel#getExtensionValue(String, String) extensions of the process model}
 * with the {@link BatchPluginUtils#PLUGIN_NAME batch plugin name} and {@link BatchPluginUtils#ACTIVITIES_KEY batch activity key}.<br>
 * Use the shortcut method {@link BatchPluginUtils#getBatchActivities(ProcessModel)} to access them.
 * @author Leon Bein
 *
 */
public class BatchProcessModelParserPlugin extends ProcessModelParserPluggable{

	@Override
	public String getName() {
		return BatchPluginUtils.PLUGIN_NAME;
	}

	@Override
	public Map<String, Object> parse(ProcessModel processModel, Element process) throws ScyllaValidationException {
		Namespace bpmnNamespace = process.getNamespace();
		Map<Integer, BatchActivity> batchActivities = new HashMap<Integer, BatchActivity>();
        for (Element element : process.getChildren()) {
            String elementName = element.getName();
            if(elementName.equals("subProcess") || elementName.equals("task") || elementName.endsWith("Task")) {
                String elementId = element.getAttributeValue("id");
                int nodeId = processModel.getIdentifiersToNodeIds().get(elementId);
                try {
                	parseElement(element, bpmnNamespace, nodeId).ifPresent(batchActivity -> batchActivities.put(nodeId, batchActivity));
                } catch (ScyllaValidationException e) {
                	throw new ScyllaValidationException("Error at parsing batch region with id "+elementId+": "+e.getMessage(), e);
                }
    		}
        }
        //Not needed batchActivities.forEach((key, value) -> value.setProcessModel(processModel));
        
		Map<String, Object> extensionAttributes = new HashMap<String, Object>();
        extensionAttributes.put(BatchPluginUtils.ACTIVITIES_KEY, batchActivities);
        return extensionAttributes;
	}
	
	
    private Optional<BatchActivity> parseElement(Element element, Namespace bpmnNamespace, Integer nodeId) throws ScyllaValidationException {

        Element extensions = element.getChild("extensionElements", bpmnNamespace);
        // Check that only elements with extensions get parsed
        if (extensions == null) return Optional.empty();

        String id = element.getAttributeValue("id");
        Namespace camundaNamespace = Namespace.getNamespace("camunda", "http://camunda.org/schema/1.0/bpmn");

        List<Namespace> possibleNamespaces = Arrays.asList(camundaNamespace, bpmnNamespace);
        List<Element> propertyList = possibleNamespaces.stream()
        		.map(namespace -> extensions.getChild("properties", namespace))
        		.filter(Objects::nonNull)
        		.map(propertiesElement -> propertiesElement.getChildren("property", propertiesElement.getNamespace()))
        		.flatMap(List::stream)
        		.collect(Collectors.toList());
        if (propertyList.isEmpty()) return Optional.empty();

        Integer maxBatchSize = null;
        BatchClusterExecutionType executionType = defaultExecutionType();
        ActivationRule activationRule = null;
        List<String> groupingCharacteristic = new ArrayList<String>();

        for (Element property : propertyList) {

            // maximum batch size
            switch (property.getAttributeValue("name")) {
                case "maxBatchSize":
                    maxBatchSize = Integer.parseInt(property.getAttributeValue("value"));
                    break;

                 // execution type. if none is defined, take parallel as default
                case "executionType":
                    executionType = parseExecutionType(property);
                    break;

                // grouping characteristic
                case "groupingCharacteristic":
                	groupingCharacteristic.addAll(parseGroupingCharacteristic(property));
                    break;

                // threshold capacity (minimum batch size) & timeout of activation rule
                case "activationRule":
                	activationRule = parseActivationRule(property);
                    break;
            }
        }
        
        if (maxBatchSize == null){
            throw new ScyllaValidationException("You have to specify a maxBatchSize at "+ id +" .");
        }
                    /*if (groupingCharacteristic.isEmpty()){
                        throw new ScyllaValidationException("You have to specify at least one groupingCharacteristic at "+ id +" .");
                    }*/

        BatchActivity ba = new BatchActivity(nodeId, maxBatchSize, executionType, activationRule, groupingCharacteristic);

        return Optional.of(ba);
    }
    
    private List<String> parseGroupingCharacteristic(Element property) {
        List<String> groupingCharacteristic = new ArrayList<>();
        List<Element> groupings = property.getChildren("property", property.getNamespace());
        for (Element grouping : groupings) {
            if (grouping.getAttributeValue("name").equals("processVariable")) {
				groupingCharacteristic.add(grouping.getAttributeValue("value"));
            }
        }
        return groupingCharacteristic;
	}

	private BatchClusterExecutionType parseExecutionType(Element property) throws ScyllaValidationException {
    	String executionTypeString = property.getAttributeValue("value");
    	try {
        	return BatchClusterExecutionType.ofElementName(executionTypeString);
    	} catch(IllegalArgumentException e) {
    		throw new ScyllaValidationException("Execution type " + executionTypeString + " not supported. Supported types are: "+Arrays.toString(BatchClusterExecutionType.values()), e);
    	}
    }
	
	private ActivationRule parseActivationRule(Element property) throws ScyllaValidationException {
        List<Element> ruleElements = property.getChildren("property", property.getNamespace());
        if (ruleElements.size() > 1) {
            throw new ScyllaValidationException("There must be one or zero activation rules, but there are " + ruleElements.size() + ".");
        } else if (ruleElements.size() == 0){
            return defaultActivationRule();
        } else {
            Element ruleElement = ruleElements.get(0);
            String ruleElementName = ruleElement.getAttributeValue("name");
            switch (ruleElementName) {
                case "thresholdRule": return parseThresholdRule(ruleElement);
                case "minMaxRule": return parseMinMaxRule(ruleElement);
                default: throw new ScyllaValidationException("Activation rule type" + ruleElementName + " not supported.");
            }
        }
	}
	
	private ThresholdRule parseThresholdRule(Element ruleElement) throws ScyllaValidationException {

        // parsing threshold, if it is defined
        int threshold = 0;
        String thresholdString = ruleElement.getAttributeValue("threshold");
        if (thresholdString != null && !thresholdString.isEmpty()) {
            threshold = Integer.parseInt(thresholdString);
        }

        //parsing timeout, if it is defined
        Duration timeout = null;
        String timeoutString = ruleElement.getAttributeValue("timeout");
        if (timeoutString != null) {
            timeout = Duration.parse(timeoutString);
        }

        //parsing dueDate, if it is defined
        String dueDate = ruleElement.getAttributeValue("duedate");

        //either timeout or dueDate should not be null --> two different Constructors for the ThresholdRule
        if (timeout != null) {
            return ThresholdRule.create(threshold, timeout);
        } else if (dueDate != null) {
            return ThresholdRule.create(threshold, dueDate);
        } else {
            throw new ScyllaValidationException("A threshold rule was selected, but neither timeout nor duedate were specified.");
        }
	}
	
	private MinMaxRule parseMinMaxRule(Element ruleElement) {
        int minInstances = Integer.parseInt(ruleElement.getAttributeValue("minInstances"));
        Duration minTimeout = Duration.parse(ruleElement.getAttributeValue("minTimeout"));
        int maxInstances = Integer.parseInt(ruleElement.getAttributeValue("maxInstances"));
        Duration maxTimeout = Duration.parse(ruleElement.getAttributeValue("maxTimeout"));
        return new MinMaxRule(minInstances, minTimeout, maxInstances, maxTimeout);
	}
	
	private ActivationRule defaultActivationRule() {
		int minInstances = 1;
        Duration minTimeout = Duration.ZERO;
        int maxInstances = 1;
        Duration maxTimeout = Duration.ZERO;
        return new MinMaxRule(minInstances, minTimeout, maxInstances, maxTimeout);
	}
	
	private BatchClusterExecutionType defaultExecutionType() {
		return BatchClusterExecutionType.PARALLEL;
	}

}
