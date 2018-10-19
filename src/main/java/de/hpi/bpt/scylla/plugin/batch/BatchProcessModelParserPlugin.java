package de.hpi.bpt.scylla.plugin.batch;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.output.XMLOutputter;

import de.hpi.bpt.scylla.exception.ScyllaValidationException;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.plugin_type.parser.ProcessModelParserPluggable;

public class BatchProcessModelParserPlugin extends ProcessModelParserPluggable{

	@Override
	public String getName() {
		return BatchPluginUtils.PLUGIN_NAME;
	}

	@Override
	public Map<String, Object> parse(ProcessModel processModel, Element process) throws ScyllaValidationException {
		Namespace bpmnNamespace = process.getNamespace();
		System.out.println(new XMLOutputter().outputString(process));
		Map<String, Object> extensionAttributes = new HashMap<String, Object>();
		Map<Integer, BatchActivity> batchActivities = new HashMap<Integer, BatchActivity>();
        for (Element element : process.getChildren()) {
            String elementName = element.getName();
            System.out.println(elementName+" "+process);
            if(elementName.equals("subProcess") || elementName.equals("task") || elementName.endsWith("Task")) {
                String elementId = element.getAttributeValue("id");
                int nodeId = processModel.getIdentifiersToNodeIds().get(elementId);
                batchActivities = parseExtensions(element, bpmnNamespace, nodeId, batchActivities);
    		}
        }

        batchActivities.forEach((key, value) -> value.setProcessModel(processModel));
        extensionAttributes.put(BatchPluginUtils.ATTRIBUTE_NAME, batchActivities);
        if(batchActivities.isEmpty())
        	System.out.println();
        return extensionAttributes;
	}
	
	
    private Map<Integer, BatchActivity> parseExtensions(Element el, Namespace bpmnNamespace, Integer nodeId, Map<Integer, BatchActivity> batchActivities) throws ScyllaValidationException {

        // Check that only elements with extensions get parsed
        if (el.getChild("extensionElements", bpmnNamespace) == null) return batchActivities;

        String id = el.getAttributeValue("id");
        Namespace camundaNamespace = Namespace.getNamespace("camunda", "http://camunda.org/schema/1.0/bpmn");

        List<Namespace> namespaces = Arrays.asList(camundaNamespace, bpmnNamespace);

        Predicate<Namespace> isOneOfUsedNamespaces = namespace -> el.getChild("extensionElements", bpmnNamespace).getChild("properties", namespace) == null;
        if (namespaces.stream().allMatch(isOneOfUsedNamespaces)) return batchActivities;



        Integer maxBatchSize = null;
        BatchClusterExecutionType executionType = BatchClusterExecutionType.PARALLEL;
        ActivationRule activationRule = null;
        List<String> groupingCharacteristic = new ArrayList<String>();

        for (Namespace namespace : namespaces) {
            if (el.getChild("extensionElements", bpmnNamespace).getChild("properties", namespace) ==  null) continue;
            List<Element> propertiesList = el.getChild("extensionElements", bpmnNamespace).getChild("properties", namespace).getChildren("property", namespace);

            for (Element property : propertiesList) {

                // maximum batch size
                switch (property.getAttributeValue("name")) {
                    case "maxBatchSize":
                        maxBatchSize = Integer.parseInt(property.getAttributeValue("value"));
                        break;

                     // execution type. if none is defined, take parallel as default
                    case "executionType":
                        String tmpExecutionType = property.getAttributeValue("value");
                        /*if (!(tmpExecutionType.equals("parallel") || tmpExecutionType.equals("sequential-taskbased") || tmpExecutionType.equals("sequential-casebased"))){
                            throw new ScyllaValidationException("Execution type " + tmpExecutionType + " not supported. Pleause us either parallel or sequential (either task or case-based)");
                        }
                        BatchClusterExecutionType bce = BatchClusterExecutionType.PARALLEL;
                        executionType = property.getAttributeValue("value");*/
                        switch (property.getAttributeValue("value")){
                            case "parallel":
                                executionType = BatchClusterExecutionType.PARALLEL;break;
                            case "sequential-taskbased":
                                executionType = BatchClusterExecutionType.SEQUENTIAL_TASKBASED;break;
                            case "sequential-casebased":
                                executionType = BatchClusterExecutionType.SEQUENTIAL_CASEBASED;break;
                        }
                        break;

                    // grouping characteristic
                    case "groupingCharacteristic":
                        List<Element> groupings = property.getChildren("property", namespace);
                        for (Element grouping : groupings) {
                            if (grouping.getAttributeValue("name").equals("processVariable")) {
                                groupingCharacteristic.add(grouping.getAttributeValue("value"));
                            }
                        }
                        break;

                    // threshold capacity (minimum batch size) & timeout of activation rule
                    case "activationRule":
                        List<Element> ruleElements = property.getChildren("property", namespace);
                        if (ruleElements.size() > 1) {
                            throw new ScyllaValidationException(
                                    "There must be one or zero activation rules for batch activity " + id + ", but there are " + ruleElements.size() + ".");
                        } else if (ruleElements.size() == 0){
                            int minInstances = 1;
                            Duration minTimeout = Duration.ZERO;
                            int maxInstances = 1;
                            Duration maxTimeout = Duration.ZERO;
                            activationRule = new MinMaxRule(minInstances, minTimeout, maxInstances, maxTimeout);
                        } else {
                            Element ruleElement = property.getChild("property", namespace);
                            String ruleElementName = ruleElement.getName();
                            switch (ruleElement.getAttributeValue("name")) {
                                case "thresholdRule":
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
                                        activationRule = new ThresholdRule(threshold, timeout);
                                    } else if (dueDate != null) {
                                        activationRule = new ThresholdRule(threshold, dueDate);
                                    } else {
                                        throw new ScyllaValidationException("A threshold rule was selected for" + ruleElementName
                                                + " then either timeout or duedate must be specified.");
                                    }
                                    break;
                                case "minMaxRule":
                                    int minInstances = Integer.parseInt(ruleElement.getAttributeValue("minInstances"));
                                    Duration minTimeout = Duration.parse(ruleElement.getAttributeValue("minTimeout"));
                                    int maxInstances = Integer.parseInt(ruleElement.getAttributeValue("maxInstances"));
                                    Duration maxTimeout = Duration.parse(ruleElement.getAttributeValue("maxTimeout"));
                                    activationRule = new MinMaxRule(minInstances, minTimeout, maxInstances, maxTimeout);
                                    break;
                                default:
                                    throw new ScyllaValidationException("Activation rule type" + ruleElementName
                                            + " for batch activity " + id + " not supported.");
                            }
                        }
                        break;
                }
            }
        }
        if (maxBatchSize==null){
            throw new ScyllaValidationException("You have to specify a maxBatchSize at "+ id +" .");
        }
                    /*if (groupingCharacteristic.isEmpty()){
                        throw new ScyllaValidationException("You have to specify at least one groupingCharacteristic at "+ id +" .");
                    }*/

        BatchActivity ba = new BatchActivity(nodeId, maxBatchSize, executionType, activationRule,
                groupingCharacteristic);

        batchActivities.put(nodeId, ba);

        return batchActivities;
    }

}
