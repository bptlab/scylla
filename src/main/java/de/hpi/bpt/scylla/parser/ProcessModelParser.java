package de.hpi.bpt.scylla.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.jdom2.Element;
import org.jdom2.Namespace;

import de.hpi.bpt.scylla.SimulationManager;
import de.hpi.bpt.scylla.exception.ScyllaValidationException;
import de.hpi.bpt.scylla.logger.DebugLogger;
import de.hpi.bpt.scylla.model.process.CommonProcessElements;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.model.process.graph.Graph;
//import de.hpi.bpt.scylla.model.process.graph.Node;
import de.hpi.bpt.scylla.model.process.graph.exception.MultipleStartNodesException;
import de.hpi.bpt.scylla.model.process.graph.exception.NoStartNodeException;
import de.hpi.bpt.scylla.model.process.graph.exception.NodeNotFoundException;
import de.hpi.bpt.scylla.model.process.node.DataObjectType;
import de.hpi.bpt.scylla.model.process.node.EventDefinitionType;
import de.hpi.bpt.scylla.model.process.node.EventType;
import de.hpi.bpt.scylla.model.process.node.GatewayType;
import de.hpi.bpt.scylla.model.process.node.MessageFlow;
import de.hpi.bpt.scylla.model.process.node.TaskType;

/**
 * Parses all elements of a process model which are relevant for simulation.
 * 
 * @author Tsun Yin Wong
 */
public class ProcessModelParser extends Parser<ProcessModel> {

    CommonProcessElements commonProcessElements;

    public ProcessModelParser(SimulationManager simEnvironment) {
        super(simEnvironment);
    }

    @Override
    public ProcessModel parse(Element rootElement) throws ScyllaValidationException {

        Namespace bpmnNamespace = rootElement.getNamespace();

        List<Element> processElements = rootElement.getChildren("process", bpmnNamespace);

        if (processElements.isEmpty()) {
            throw new ScyllaValidationException("No process in file.");
        }

        // pool references to process models
        Map<String, String> processIdToPoolName = new HashMap<String, String>();
        Map<String, MessageFlow> messageFlows = new HashMap<String, MessageFlow>();

        Element collaboration = rootElement.getChild("collaboration", bpmnNamespace);
        if (collaboration != null) {

            for (Element el : collaboration.getChildren()) {
                String elementName = el.getName();
                if (elementName.equals("participant")) {
                    if (el.getAttributeValue("processRef") != null) {
                        String participantName = el.getAttributeValue("name");
                        String processId = el.getAttributeValue("processRef");
                        processIdToPoolName.put(processId, participantName);
                    }
                }
                else if (elementName.equals("messageFlow")) {
                    String id = el.getAttributeValue("id");
                    String sourceRef = el.getAttributeValue("sourceRef");
                    String targetRef = el.getAttributeValue("targetRef");
                    MessageFlow messageFlow = new MessageFlow(id, sourceRef, targetRef);
                    messageFlows.put(id, messageFlow);
                }
                else {
                    DebugLogger.log("Element " + el.getName() + " of collaboration not supported.");
                }
            }
        }

        Map<String, ProcessModel> processModels = new HashMap<String, ProcessModel>();

        for (Element process : processElements) {
            ProcessModel processModel = parseProcess(process, bpmnNamespace, false, commonProcessElements);
            String processId = processModel.getId();
            if (processIdToPoolName.containsKey(processId)) {
                String participant = processIdToPoolName.get(processId);
                processModel.setParticipant(participant);
            }
            processModels.put(processId, processModel);
        }

        if (processModels.size() == 1) {
            return processModels.values().iterator().next();
        }
        else {
            try {
                Set<ProcessModel> processModelsTriggeredInCollaboration = new HashSet<ProcessModel>();
                ProcessModel processModelTriggeredExternally = null;
                for (String processId : processModels.keySet()) {
                    ProcessModel pm = processModels.get(processId);
                    int startNodeId = pm.getStartNode();

                    String identifierOfStartNode = pm.getIdentifiers().get(startNodeId);
                    boolean isTriggeredInCollaboration = false;
                    for (MessageFlow mf : messageFlows.values()) {
                        if (mf.getTargetRef().equals(identifierOfStartNode)) {
                            isTriggeredInCollaboration = true;
                            break;
                        }
                    }
                    if (isTriggeredInCollaboration) {
                        processModelsTriggeredInCollaboration.add(pm);
                    }
                    else {
                        if (processModelTriggeredExternally != null) {
                            throw new ScyllaValidationException(
                                    "BPMN file contains multiple process models that are triggered externally.");
                        }
                        processModelTriggeredExternally = pm;
                    }
                }
                processModelTriggeredExternally.setProcessModelsInCollaboration(processModelsTriggeredInCollaboration);
                return processModelTriggeredExternally;
            }
            catch (NodeNotFoundException | MultipleStartNodesException | NoStartNodeException e) {
                e.printStackTrace();
                throw new ScyllaValidationException(e.getMessage());
            }
        }

    }

    private ProcessModel parseProcess(Element process, Namespace bpmnNamespace, boolean hasParentModel,
            CommonProcessElements commonProcessElements) throws ScyllaValidationException {

        if (!hasParentModel && process.getChildren("startEvent", bpmnNamespace).size() == 0) {
            throw new ScyllaValidationException("No start event in the top process.");
        }

        String processId = process.getAttributeValue("id");
        String processName = process.getAttributeValue("name");

        Graph<Integer> graph = new Graph<Integer>();
        Map<Integer, String> identifiers = new HashMap<Integer, String>();
        Map<String, Integer> identifiersToNodeIds = new HashMap<String, Integer>();
        Map<Integer, String> displayNames = new HashMap<Integer, String>();

        // node id must be key of any of the following
        Map<Integer, ProcessModel> subProcesses = new HashMap<Integer, ProcessModel>();
        Map<Integer, String> calledElementsOfCallActivities = new HashMap<Integer, String>();
        Map<Integer, TaskType> tasks = new HashMap<Integer, TaskType>();
        Map<Integer, GatewayType> gateways = new HashMap<Integer, GatewayType>();
        Map<Integer, EventType> eventTypes = new HashMap<Integer, EventType>();

        // optional
        Map<Integer, Map<String, String>> nodeAttributes = new HashMap<Integer, Map<String, String>>();
        Map<Integer, String> conditionExpressions = new HashMap<Integer, String>();
        Map<Integer, Set<String>> resourceReferences = new HashMap<Integer, Set<String>>();
        Map<Integer, Map<EventDefinitionType, Map<String, String>>> eventDefinitions = new HashMap<Integer, Map<EventDefinitionType, Map<String, String>>>();
        Map<Integer, Boolean> cancelActivities = new HashMap<Integer, Boolean>();
        Map<Integer, List<Integer>> referencesToBoundaryEvents = new HashMap<Integer, List<Integer>>();

        // TODO support more than standard data objects (i.e. support input, output)
        Graph<Integer> dataObjectsGraph = new Graph<Integer>();
        Map<Integer, String> dataObjectReferences = new HashMap<Integer, String>();
        Map<Integer, DataObjectType> dataObjectTypes = new HashMap<Integer, DataObjectType>();

        // TODO parse loop / multi instance elements

        Map<Integer, Element> boundaryEvents = new HashMap<Integer, Element>();
        Map<Integer, Element> sequenceFlows = new HashMap<Integer, Element>();
        Map<Integer, List<Element>> tasksWithDataInputAssociations = new HashMap<Integer, List<Element>>();
        Map<Integer, List<Element>> tasksWithDataOutputAssociations = new HashMap<Integer, List<Element>>();
        int nodeId = 1;
        for (Element el : process.getChildren()) {
            String elementName = el.getName();
            if (isKnownElement(elementName)) {
                String identifier = el.getAttributeValue("id");
                identifiers.put(nodeId, identifier);
                identifiersToNodeIds.put(identifier, nodeId);
                String displayName = el.getAttributeValue("name");
                if (displayName != null && !displayName.isEmpty()) {
                    displayNames.put(nodeId, displayName);
                }
                if (elementName.equals("sequenceFlow")) {
                    // just store them for now, use later to build graph
                    sequenceFlows.put(nodeId, el);
                    // store expressions after gateway conditions
                    Element conditionExpression = el.getChild("conditionExpression", bpmnNamespace);
                    if (conditionExpression != null) {
                        conditionExpressions.put(nodeId, conditionExpression.getText());
                    }
                }
                else if (elementName.equals("subProcess")) {
                    ProcessModel subProcessModel = parseProcess(el, bpmnNamespace, true, commonProcessElements);
                    subProcesses.put(nodeId, subProcessModel);
                }
                else if (elementName.equals("callActivity")) {
                    String calledElement = el.getAttributeValue("calledElement");
                    if (calledElement != null) {
                        calledElementsOfCallActivities.put(nodeId, calledElement);
                    }
                }
                else if (elementName.equals("task") || elementName.endsWith("Task")) {
                    tasks.put(nodeId, TaskType.getEnum(el.getName()));
                    if (elementName.equals("userTask") || elementName.equals("manualTask")) {
                        String[] resourceElementNames = new String[] { "resourceRole", "performer", "humanPerformer",
                            "potentialOwner" };
                        for (String ren : resourceElementNames) {
                            Element elem = el.getChild(ren, bpmnNamespace);
                            if (elem != null) {
                                String resourceRefOrAssignmentExpression = null;
                                Element resourceRefElement = elem.getChild("resourceRef", bpmnNamespace);
                                if (resourceRefElement != null) {
                                    resourceRefOrAssignmentExpression = resourceRefElement.getText();
                                }
                                Element resourceAssignmentExpressionElement = elem
                                        .getChild("resourceAssignmentExpression", bpmnNamespace);
                                if (resourceAssignmentExpressionElement != null) {
                                    resourceRefOrAssignmentExpression = resourceAssignmentExpressionElement.getText();
                                }
                                if (resourceRefOrAssignmentExpression != null) {
                                    if (!resourceReferences.containsKey(nodeId)) {
                                        resourceReferences.put(nodeId, new TreeSet<String>());
                                    }
                                    resourceReferences.get(nodeId).add(resourceRefOrAssignmentExpression);
                                    break;
                                }
                            }
                        }
                    }
                    List<Element> dataInElements = el.getChildren("dataInputAssociation", bpmnNamespace);
                    if (!dataInElements.isEmpty()) {
                        tasksWithDataInputAssociations.put(nodeId, dataInElements);
                    }
                    List<Element> dataOutElements = el.getChildren("dataOutputAssociation", bpmnNamespace);
                    if (!dataOutElements.isEmpty()) {
                        tasksWithDataOutputAssociations.put(nodeId, dataOutElements);
                    }
                }
                else if (elementName.endsWith("Gateway") || elementName.equals("gateway")) {
                    gateways.put(nodeId, GatewayType.getEnum(el.getName()));

                    Map<String, String> gatewayAttributes = new HashMap<String, String>();
                    String defaultFlow = el.getAttributeValue("default");
                    if (defaultFlow != null) {
                        gatewayAttributes.put("default", defaultFlow);
                    }
                    String gatewayDirection = el.getAttributeValue("gatewayDirection");
                    if (gatewayDirection != null) {
                        gatewayAttributes.put("gatewayDirection", gatewayDirection);
                    }
                    nodeAttributes.put(nodeId, gatewayAttributes);
                }
                else if (elementName.endsWith("Event") || elementName.equals("event")) {
                    eventTypes.put(nodeId, EventType.getEnum(el.getName()));
                    Map<EventDefinitionType, Map<String, String>> eventDefinitionsOfElement = new HashMap<EventDefinitionType, Map<String, String>>();
                    for (EventDefinitionType edt : EventDefinitionType.values()) {
                        Element edElem = el.getChild(edt.toString(), bpmnNamespace);
                        if (edElem != null) {
                            Map<String, String> eventAttributes = new HashMap<String, String>();
                            if (edt == EventDefinitionType.CANCEL) {
                                // TODO transaction subprocesses only
                            }
                            else if (edt == EventDefinitionType.COMPENSATION) {
                                String activityRef = edElem.getAttributeValue("activityRef");
                                if (activityRef != null) {
                                    eventAttributes.put("activityRef", activityRef);
                                }
                                String waitForCompletion = edElem.getAttributeValue("waitForCompletion");
                                if (waitForCompletion != null) {
                                    eventAttributes.put("waitForCompletion", waitForCompletion);
                                }
                            }
                            else if (edt == EventDefinitionType.CONDITIONAL) {
                                Element conditionElement = edElem.getChild("condition", bpmnNamespace);
                                if (conditionElement != null) {
                                    eventAttributes.put("condition", conditionElement.getText());
                                }
                            }
                            else if (edt == EventDefinitionType.ERROR) {
                                String errorRef = edElem.getAttributeValue("errorRef");
                                if (errorRef != null) {
                                    if (!commonProcessElements.getErrors().containsKey(errorRef)) {
                                        throw new ScyllaValidationException("Referenced object of error event "
                                                + identifier + " is unknown: " + errorRef);
                                    }
                                    eventAttributes.put("errorRef", errorRef);
                                }
                                else {
                                    throw new ScyllaValidationException(
                                            "Error event " + identifier + " has no reference to an error.");
                                }
                            }
                            else if (edt == EventDefinitionType.ESCALATION) {
                                String escalationRef = edElem.getAttributeValue("escalationRef");
                                if (escalationRef != null) {
                                    if (!commonProcessElements.getEscalations().containsKey(escalationRef)) {
                                        throw new ScyllaValidationException("Referenced object of escalation event "
                                                + identifier + " is unknown: " + escalationRef);
                                    }
                                    eventAttributes.put("escalationRef", escalationRef);
                                }
                                else {
                                    throw new ScyllaValidationException(
                                            "Escalation event " + identifier + " has no reference to an escalation.");
                                }
                            }
                            else if (edt == EventDefinitionType.LINK) {
                                String name = edElem.getAttributeValue("name");
                                if (name != null) {
                                    eventAttributes.put("name", name);
                                }
                                List<Element> sourceElements = edElem.getChildren("source", bpmnNamespace);
                                int i = 0;
                                for (Element sourceElement : sourceElements) {
                                    eventAttributes.put("source" + ++i, sourceElement.getText());
                                }
                                Element targetElement = edElem.getChild("target", bpmnNamespace);
                                if (targetElement != null) {
                                    eventAttributes.put("target", targetElement.getText());
                                }
                            }
                            else if (edt == EventDefinitionType.MESSAGE) {
                                String messageRef = edElem.getAttributeValue("messageRef");
                                if (messageRef != null) {
                                    if (!commonProcessElements.getMessages().containsKey(messageRef)) {
                                        throw new ScyllaValidationException("Referenced object of message event "
                                                + identifier + " is unknown: " + messageRef);
                                    }
                                    eventAttributes.put("messageRef", messageRef);
                                }
                                Element operationElement = edElem.getChild("operationRef", bpmnNamespace);
                                if (operationElement != null) {
                                    eventAttributes.put("operationRef", operationElement.getText());
                                }
                            }
                            else if (edt == EventDefinitionType.SIGNAL) {
                                String signalRef = edElem.getAttributeValue("signalRef");
                                if (signalRef != null) {
                                    eventAttributes.put("signalRef", signalRef);
                                }
                            }
                            else if (edt == EventDefinitionType.TIMER) {
                                Element timeDateElement = edElem.getChild("timeDate", bpmnNamespace);
                                // we do not validate time expressions and duration here
                                // this is the reponsibility of the plug-ins
                                if (timeDateElement != null) {
                                    eventAttributes.put("timeDate", timeDateElement.getText());
                                }
                                Element timeCycleElement = edElem.getChild("timeCycle", bpmnNamespace);
                                if (timeCycleElement != null) {
                                    eventAttributes.put("timeCycle", timeCycleElement.getText());
                                }
                                Element timeDurationElement = edElem.getChild("timeDuration", bpmnNamespace);
                                if (timeDurationElement != null) {
                                    eventAttributes.put("timeDuration", timeDurationElement.getText());
                                }
                                // time attributes are mutually exclusive
                                if (eventAttributes.size() > 1) {
                                    throw new ScyllaValidationException("Timer event " + identifier
                                            + " is invalid. Time definitions are mutually exclusive.");
                                }
                            }

                            eventDefinitionsOfElement.put(edt, eventAttributes);
                        }

                        eventDefinitions.put(nodeId, eventDefinitionsOfElement);

                        if (elementName.equals("boundaryEvent")) {
                            // just store them for now, use later to create references to boundary events
                            boundaryEvents.put(nodeId, el);
                        }
                    }
                }
                else if (elementName.equals("dataObjectReference")) {
                	String dataObjectRef = el.getAttributeValue("dataObjectRef");
                    dataObjectReferences.put(nodeId, dataObjectRef);
                }
                else if (elementName.equals("ioSpecification")){
                	Element input = el.getChild("dataInput", bpmnNamespace);
                	if(input != null){
                		// remove the ioSpecification element
                		identifiersToNodeIds.remove(identifier);
                	
                		// override the values with the inner input element
	                	identifier = input.getAttributeValue("id");
	                    identifiers.put(nodeId, identifier);
	                    identifiersToNodeIds.put(identifier, nodeId);
	                    displayName = input.getAttributeValue("name");
	                    if (displayName != null && !displayName.isEmpty()) {
	                        displayNames.put(nodeId, displayName);
	                    }
	                    
	                    dataObjectTypes.put(nodeId, DataObjectType.INPUT);
                	}
                }
                else if (elementName.equals("dataObject")) {
                	dataObjectTypes.put(nodeId, DataObjectType.DEFAULT);
                }
                else {
                    DebugLogger.log("Element " + el.getName()
                            + " of process model is expected to be known, but not supported.");
                }
                nodeId++;
            }
            else {
                DebugLogger.log("Element " + el.getName() + " of process model not supported.");
            }
        }

        // create resource references
        for (Element laneSet : process.getChildren("laneSet", bpmnNamespace)) {
            for (Element lane : laneSet.getChildren("lane", bpmnNamespace)) {
                String resourceName = lane.getAttributeValue("name");
                for (Element flowNodeRef : lane.getChildren("flowNodeRef", bpmnNamespace)) {
                    String elementId = flowNodeRef.getText();
                    Integer nId = identifiersToNodeIds.get(elementId);
                    if (!resourceReferences.containsKey(nId)) {
                        resourceReferences.put(nId, new TreeSet<String>());
                    }
                    resourceReferences.get(nId).add(resourceName);
                }
            }
        }

        for (Integer nId : boundaryEvents.keySet()) {
            Element boundaryEvent = boundaryEvents.get(nId);
            // interrupting or not?
            boolean cancelActivity = Boolean.valueOf(boundaryEvent.getAttributeValue("cancelActivity"));
            cancelActivities.put(nId, cancelActivity);

            // attached to?

            String attachedTo = boundaryEvent.getAttributeValue("attachedToRef");
            int nodeIdOfAttachedTo = identifiersToNodeIds.get(attachedTo);
            if (!referencesToBoundaryEvents.containsKey(nodeIdOfAttachedTo)) {
                referencesToBoundaryEvents.put(nodeIdOfAttachedTo, new ArrayList<Integer>());
            }
            referencesToBoundaryEvents.get(nodeIdOfAttachedTo).add(nId);
        }

        //System.out.println("-----------NORMALFLOW-------------");
        for (Integer nId : sequenceFlows.keySet()) {
            Element sequenceFlow = sequenceFlows.get(nId);
            String sourceRef = sequenceFlow.getAttributeValue("sourceRef");
            String targetRef = sequenceFlow.getAttributeValue("targetRef");
            int sourceId = identifiersToNodeIds.get(sourceRef);
            int targetId = identifiersToNodeIds.get(targetRef);
            graph.addEdge(sourceId, nId);
            //System.out.println(identifiers.get(sourceId)+" -> "+identifiers.get(nId)+" -> "+identifiers.get(targetId));
            graph.addEdge(nId, targetId);
        }
        //System.out.println("-----------DATAFLOWOUT-------------");
        for (Integer nId : tasksWithDataInputAssociations.keySet()) {
            List<Element> dataInputAssociations = tasksWithDataInputAssociations.get(nId);
            for (Element elem : dataInputAssociations) {
                String sourceRef = elem.getChild("sourceRef", bpmnNamespace).getText();
                if (identifiersToNodeIds.containsKey(sourceRef)) {
                    int dataObjectNodeId = identifiersToNodeIds.get(sourceRef);
                    //System.out.println(identifiers.get(dataObjectNodeId)+" -> "+identifiers.get(nId));
                    dataObjectsGraph.addEdge(dataObjectNodeId, nId);
                    /*Map<Integer, Node<Integer>> nodes = dataObjectsGraph.getNodes();
                    Node<Integer> currentNode = nodes.get(nId);
                    currentNode.setId(identifiers.get(nId));   
                    currentNode = nodes.get(dataObjectNodeId);
                    currentNode.setId(identifiers.get(dataObjectNodeId));*/
                }
                // String targetRef = elem.getChild("targetRef", bpmnNamespace).getText();
            }
        }
        
        //System.out.println("-----------DATAFLOWIN-------------");
        for (Integer nId : tasksWithDataOutputAssociations.keySet()) {
            List<Element> dataOutputAssociations = tasksWithDataOutputAssociations.get(nId);
            for (Element elem : dataOutputAssociations) {
                // String sourceRef = elem.getChild("sourceRef", bpmnNamespace).getText();
                String targetRef = elem.getChild("targetRef", bpmnNamespace).getText();
                if (identifiersToNodeIds.containsKey(targetRef)) {
                    int dataObjectNodeId = identifiersToNodeIds.get(targetRef);
                    //System.out.println(identifiers.get(nId)+" -> "+identifiers.get(dataObjectNodeId));
                    dataObjectsGraph.addEdge(nId, dataObjectNodeId);
                    /*Map<Integer, Node<Integer>> nodes = dataObjectsGraph.getNodes();
                    Node<Integer> currentNode = nodes.get(nId);
                    currentNode.setId(identifiers.get(nId));   
                    currentNode = nodes.get(dataObjectNodeId);
                    currentNode.setId(identifiers.get(dataObjectNodeId));*/
                }
            }
        }

        ProcessModel processModel = new ProcessModel(processId, process, graph, identifiers, identifiersToNodeIds,
                displayNames, subProcesses, calledElementsOfCallActivities, tasks, gateways, eventTypes);
        processModel.setName(processName);
        processModel.setNodeAttributes(nodeAttributes);
        processModel.setConditionExpressions(conditionExpressions);
        processModel.setResourceReferences(resourceReferences);
        processModel.setEventDefinitions(eventDefinitions);
        processModel.setCancelActivities(cancelActivities);
        processModel.setReferencesToBoundaryEvents(referencesToBoundaryEvents);

        processModel.setDataObjectsGraph(dataObjectsGraph);
        processModel.setDataObjectTypes(dataObjectTypes);
        processModel.setDataObjectReferences(dataObjectReferences);

        for (Integer subProcessId : subProcesses.keySet()) {
            ProcessModel subProcessModel = subProcesses.get(subProcessId);
            subProcessModel.setNodeIdInParent(subProcessId);
            subProcessModel.setParent(processModel);
        }

        return processModel;
    }

    private boolean isKnownElement(String name) {
        return name.equals("sequenceFlow") || name.equals("task") || name.endsWith("Task") || name.endsWith("Event")
                || name.endsWith("Gateway") || name.equals("subProcess") || name.equals("callActivity")
                || name.equals("laneSet") || name.equals("dataObjectReference") || name.equals("ioSpecification")
                || name.equals("dataObject");
    }

    public void setCommonProcessElements(CommonProcessElements commonProcessElements) {
        this.commonProcessElements = commonProcessElements;
    }

}