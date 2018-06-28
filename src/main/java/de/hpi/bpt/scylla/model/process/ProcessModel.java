package de.hpi.bpt.scylla.model.process;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.hpi.bpt.scylla.plugin.batch.BatchActivity;
import org.jdom2.Element;

import de.hpi.bpt.scylla.exception.ScyllaValidationException;
import de.hpi.bpt.scylla.model.SimulationInput;
import de.hpi.bpt.scylla.model.process.graph.Graph;
import de.hpi.bpt.scylla.model.process.graph.exception.MultipleStartNodesException;
import de.hpi.bpt.scylla.model.process.graph.exception.NoStartNodeException;
import de.hpi.bpt.scylla.model.process.graph.exception.NodeNotFoundException;
import de.hpi.bpt.scylla.model.process.node.DataObjectType;
import de.hpi.bpt.scylla.model.process.node.EventDefinitionType;
import de.hpi.bpt.scylla.model.process.node.EventType;
import de.hpi.bpt.scylla.model.process.node.GatewayType;
import de.hpi.bpt.scylla.model.process.node.TaskType;

/**
 * Represents all elements of a process model which are relevant for simulation.
 * 
 * @author Tsun Yin Wong
 */
public class ProcessModel extends SimulationInput {

    private String modelScopeId;
    private ProcessModel parent;
    private Element xmlDom;
    private Graph<Integer> graph;
    private Map<Integer, String> identifiers;
    private Map<String, Integer> identifiersToNodeIds;
    private Map<Integer, String> displayNames;

    // node id must be key of any of the following
    private Integer nodeIdInParent;
    private Map<Integer, ProcessModel> subProcesses;
    private Map<Integer, String> calledElementsOfCallActivities;
    private Map<Integer, TaskType> tasks;
    private Map<Integer, GatewayType> gateways;
    private Map<Integer, EventType> eventTypes;

    // optional
    private String name;
    private Map<Integer, Map<String, String>> nodeAttributes = new HashMap<Integer, Map<String, String>>();
    private Map<Integer, String> conditionExpressions;
    private Map<Integer, Set<String>> resourceReferences;
    private Map<Integer, Map<EventDefinitionType, Map<String, String>>> eventDefinitions;
    private Map<Integer, Boolean> cancelActivities;
    private Map<Integer, List<Integer>> referencesToBoundaryEvents;
    private Map<Integer, BatchActivity> batchActivities;

    private Graph<Integer> dataObjectsGraph;
    private Map<Integer, DataObjectType> dataObjectTypes;
    private Map<Integer, String> dataObjectReferences;

    private String participant;
    private Set<ProcessModel> processModelsInCollaboration;

    /**
     * Constructor.
     * 
     * @param id
     *            process identifier
     * @param xmlDom
     *            JDOM element of the process model
     * @param graph
     *            graph representation of the process model - describes nodes and flows
     * @param identifiers
     *            map of graph node identifiers to identifiers of parsed elements
     * @param identifiersToNodeIds
     *            reversed map of {@link #identifiers}
     * @param displayNames
     *            map of graph node identifiers to display names
     * @param subProcesses
     *            map of graph node identifiers to sub processes
     * @param calledElementsOfCallActivities
     *            map of graph node identifiers to called elements of call activities
     * @param tasks
     *            map of graph node identifiers to BPMN task types
     * @param gateways
     *            map of graph node identifiers to BPMN gateway types
     * @param eventTypes
     *            map of graph node identifiers to BPMN event types
     */
    public ProcessModel(String id, Element xmlDom, Graph<Integer> graph, Map<Integer, String> identifiers,
            Map<String, Integer> identifiersToNodeIds, Map<Integer, String> displayNames,
            Map<Integer, ProcessModel> subProcesses, Map<Integer, String> calledElementsOfCallActivities,
            Map<Integer, TaskType> tasks, Map<Integer, GatewayType> gateways, Map<Integer, EventType> eventTypes) {
        super(id);
        this.xmlDom = xmlDom;
        this.graph = graph;
        this.identifiers = identifiers;
        this.identifiersToNodeIds = identifiersToNodeIds;
        this.displayNames = displayNames;
        this.subProcesses = subProcesses;
        this.calledElementsOfCallActivities = calledElementsOfCallActivities;
        this.tasks = tasks;
        this.gateways = gateways;
        this.eventTypes = eventTypes;
        this.modelScopeId = buildModelScopeId(this);
    }

    private String buildModelScopeId(ProcessModel processModel) {
        if (processModel.getParent() == null) {
            return processModel.getId();
        }
        return buildModelScopeId(processModel.getParent()) + "_" + processModel.getId();
    }

    public String getModelScopeId() {
        return modelScopeId;
    }

    public Integer getNodeIdInParent() {
        return nodeIdInParent;
    }

    public void setNodeIdInParent(Integer nodeIdInParent) {
        this.nodeIdInParent = nodeIdInParent;
    }

    public ProcessModel getParent() {
        return parent;
    }

    public void setParent(ProcessModel parent) {
        this.parent = parent;
        this.modelScopeId = buildModelScopeId(this);
    }

    public Map<Integer, ProcessModel> getSubProcesses() {
        return subProcesses;
    }

    public void setSubProcesses(Map<Integer, ProcessModel> subProcesses) {
        this.subProcesses = subProcesses;
    }

    public Map<Integer, String> getCalledElementsOfCallActivities() {
        return calledElementsOfCallActivities;
    }

    public void setCalledElementsOfCallActivities(Map<Integer, String> calledElementsOfCallActivities) {
        this.calledElementsOfCallActivities = calledElementsOfCallActivities;
    }

    public Map<Integer, TaskType> getTasks() {
        return tasks;
    }

    public void setTasks(Map<Integer, TaskType> tasks) {
        this.tasks = tasks;
    }

    public Map<Integer, GatewayType> getGateways() {
        return gateways;
    }

    public void setGateways(Map<Integer, GatewayType> gateways) {
        this.gateways = gateways;
    }

    public Map<Integer, EventType> getEventTypes() {
        return eventTypes;
    }

    public void setEventTypes(Map<Integer, EventType> eventTypes) {
        this.eventTypes = eventTypes;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Map<Integer, Map<String, String>> getNodeAttributes() {
        return nodeAttributes;
    }

    public void setNodeAttributes(Map<Integer, Map<String, String>> nodeAttributes) {
        this.nodeAttributes = nodeAttributes;
    }

    public Map<Integer, String> getConditionExpressions() {
        return conditionExpressions;
    }

    public void setConditionExpressions(Map<Integer, String> conditionExpressions) {
        this.conditionExpressions = conditionExpressions;
    }

    public Map<Integer, Set<String>> getResourceReferences() {
        return resourceReferences;
    }

    public void setResourceReferences(Map<Integer, Set<String>> resourceReferences) {
        this.resourceReferences = resourceReferences;
    }

    public Map<Integer, Map<EventDefinitionType, Map<String, String>>> getEventDefinitions() {
        return eventDefinitions;
    }

    public void setEventDefinitions(Map<Integer, Map<EventDefinitionType, Map<String, String>>> eventDefinitions) {
        this.eventDefinitions = eventDefinitions;
    }

    public Map<Integer, Boolean> getCancelActivities() {
        return cancelActivities;
    }

    public void setCancelActivities(Map<Integer, Boolean> cancelActivities) {
        this.cancelActivities = cancelActivities;
    }

    public Map<Integer, List<Integer>> getReferencesToBoundaryEvents() {
        return referencesToBoundaryEvents;
    }

    public void setReferencesToBoundaryEvents(Map<Integer, List<Integer>> referencesToBoundaryEvents) {
        this.referencesToBoundaryEvents = referencesToBoundaryEvents;
    }

    public Graph<Integer> getDataObjectsGraph() {
        return dataObjectsGraph;
    }

    public void setDataObjectsGraph(Graph<Integer> dataObjectsGraph) {
        this.dataObjectsGraph = dataObjectsGraph;
    }

    public Map<Integer, DataObjectType> getDataObjectTypes() {
        return dataObjectTypes;
    }

    public void setDataObjectTypes(Map<Integer, DataObjectType> dataObjectTypes) {
        this.dataObjectTypes = dataObjectTypes;
    }
    
    public Map<Integer, String> getDataObjectReferences() {
    	return dataObjectReferences;
    }
    
    public void setDataObjectReferences(Map<Integer, String> dataObjectReferences) {
    	this.dataObjectReferences = dataObjectReferences;
    }

    public void setBatchActivities(Map<Integer, BatchActivity> batchActivities){
        this.batchActivities = batchActivities;
    }

    public Map<Integer, BatchActivity>  getBatchActivities() { return batchActivities; }
    
    public String getParticipant() {
        return participant;
    }

    public void setParticipant(String participant) {
        this.participant = participant;
    }

    public Element getXmlDom() {
        return xmlDom;
    }

    public Graph<Integer> getGraph() {
        return graph;
    }

    public Map<Integer, String> getIdentifiers() {
        return identifiers;
    }

    public Map<String, Integer> getIdentifiersToNodeIds() {
        return identifiersToNodeIds;
    }

    public Map<Integer, String> getDisplayNames() {
        return displayNames;
    }

    /**
     * Returns preceding object(s) (nodes or flows) of given node.
     * 
     * @param nodeId
     *            id of graph node in question
     * @return ids of preceding graph objects
     * @throws NodeNotFoundException
     */
    public Set<Integer> getSourceObjectIds(int nodeId) throws NodeNotFoundException {
        return graph.getSourceObjects(nodeId);
    }

    /**
     * Returns subsequent object(s) (nodes or flows) of given node.
     * 
     * @param nodeId
     *            id of graph node in question
     * @return ids of subsequent graph objects
     * @throws NodeNotFoundException
     */
    public Set<Integer> getTargetObjectIds(int nodeId) throws NodeNotFoundException {
        return graph.getTargetObjects(nodeId);
    }

    /**
     * Returns preceding nodes of given node.
     * 
     * @param nId
     *            id of graph node
     * @return ids of preceding graph nodes
     * @throws NodeNotFoundException
     * @throws ScyllaValidationException
     */
    public Set<Integer> getIdsOfPreviousNodes(int nId) throws NodeNotFoundException, ScyllaValidationException {

        Set<Integer> flowIds = getSourceObjectIds(nId);
        Set<Integer> sourceNodeIds = new HashSet<Integer>();
        for (Integer flowId : flowIds) {
            Set<Integer> nodeIds = getSourceObjectIds(flowId);
            if (nodeIds.size() != 1) {
                throw new ScyllaValidationException(
                        "Flow " + flowId + " does not connect to 1 node, but" + nodeIds.size() + " .");
            }
            for (Integer nodeId : nodeIds) {
                sourceNodeIds.add(nodeId);
            }
        }
        return sourceNodeIds;
    }

    /**
     * Returns subsequent nodes of given node.
     * 
     * @param nId
     *            id of given node
     * @return ids of subequent graph nodes
     * @throws NodeNotFoundException
     * @throws ScyllaValidationException
     */
    public Set<Integer> getIdsOfNextNodes(int nId) throws NodeNotFoundException, ScyllaValidationException {

        Set<Integer> flowIds = getTargetObjectIds(nId);
        Set<Integer> targetNodeIds = new HashSet<Integer>();
        for (Integer flowId : flowIds) {
            Set<Integer> nodeIds = getTargetObjectIds(flowId);
            if (nodeIds.size() != 1) {
                throw new ScyllaValidationException(
                        "Flow " + flowId + " does not connect to 1 node, but" + nodeIds.size() + " .");
            }
            for (Integer nodeId : nodeIds) {
                targetNodeIds.add(nodeId);
            }
        }
        return targetNodeIds;
    }

    public Set<Integer> getBoundaryEventIds() {
        Set<Integer> boundaryEventIds = new HashSet<Integer>();
        for (List<Integer> eventIds : referencesToBoundaryEvents.values()) {
            boundaryEventIds.addAll(eventIds);
        }
        return boundaryEventIds;
    }

    /**
     * Returns start node of the process model (i.e. which does not have any predecessors).
     * 
     * @return graph node id of start node
     * @throws NodeNotFoundException
     * @throws MultipleStartNodesException
     * @throws NoStartNodeException
     */
    public Integer getStartNode() throws NodeNotFoundException, MultipleStartNodesException, NoStartNodeException {
        Set<Integer> nodesWithoutSource = graph.getNodesWithoutSource();
        Set<Integer> boundaryEventIds = getBoundaryEventIds();
        nodesWithoutSource.removeAll(boundaryEventIds);
        if (nodesWithoutSource.size() > 1) {
            throw new MultipleStartNodesException("Multiple start nodes found for process " + id);
        }
        else if (nodesWithoutSource.size() == 0) {
            throw new NoStartNodeException("No start node found for process " + id);
        }
        return nodesWithoutSource.iterator().next();
    }

    public Set<ProcessModel> getProcessModelsInCollaboration() {
        return processModelsInCollaboration;
    }

    public void setProcessModelsInCollaboration(Set<ProcessModel> processModelsTriggeredInCollaboration) {
        this.processModelsInCollaboration = processModelsTriggeredInCollaboration;
    }
}
