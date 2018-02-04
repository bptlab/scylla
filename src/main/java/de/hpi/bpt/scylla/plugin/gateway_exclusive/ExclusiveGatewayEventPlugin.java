package de.hpi.bpt.scylla.plugin.gateway_exclusive;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.exception.ScyllaValidationException;
import de.hpi.bpt.scylla.logger.ProcessNodeInfo;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.model.process.graph.exception.NodeNotFoundException;
import de.hpi.bpt.scylla.model.process.node.GatewayType;
import de.hpi.bpt.scylla.plugin.dataobject.DataObjectField;
import de.hpi.bpt.scylla.plugin_loader.PluginLoader;
import de.hpi.bpt.scylla.plugin_loader.PluginLoader.PluginWrapper;
import de.hpi.bpt.scylla.plugin_type.simulation.event.GatewayEventPluggable;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.ProcessSimulationComponents;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import de.hpi.bpt.scylla.simulation.event.GatewayEvent;
import de.hpi.bpt.scylla.simulation.event.ScyllaEvent;
import de.hpi.bpt.scylla.simulation.utils.SimulationUtils;
import desmoj.core.dist.DiscreteDistEmpirical;
import desmoj.core.simulator.TimeSpan;

public class ExclusiveGatewayEventPlugin extends GatewayEventPluggable {

    @Override
    public String getName() {
        return ExclusiveGatewayPluginUtils.PLUGIN_NAME;
    }

    private void scheduleNextEvent(GatewayEvent desmojEvent, ProcessInstance processInstance, ProcessModel processModel, Integer nextFlowId) {
        Set<Integer> nodeIds = null;
        try {
            nodeIds = processModel.getTargetObjectIds(nextFlowId);
        } catch (NodeNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (nodeIds.size() != 1) {
            try {
                throw new ScyllaValidationException(
                        "Flow " + nextFlowId + " does not connect to 1 node, but" + nodeIds.size() + " .");
            } catch (ScyllaValidationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        int nextNodeId = nodeIds.iterator().next();

        Map<Integer, ScyllaEvent> nextEventMap = desmojEvent.getNextEventMap();
        List<Integer> indicesOfEventsToKeep = new ArrayList<Integer>();
        for (int index : nextEventMap.keySet()) {
            ScyllaEvent eventCandidate = nextEventMap.get(index);
            int nodeIdOfCandidate = eventCandidate.getNodeId();
            if (nodeIdOfCandidate == nextNodeId) {
                indicesOfEventsToKeep.add(index);
                break;
            }
        }
        Map<Integer, TimeSpan> timeSpanToNextEventMap = desmojEvent.getTimeSpanToNextEventMap();
        nextEventMap.keySet().retainAll(indicesOfEventsToKeep);
        timeSpanToNextEventMap.keySet().retainAll(indicesOfEventsToKeep);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void eventRoutine(GatewayEvent desmojEvent, ProcessInstance processInstance) throws ScyllaRuntimeException {
        SimulationModel model = (SimulationModel) desmojEvent.getModel();
        ProcessModel processModel = processInstance.getProcessModel();
        int nodeId = desmojEvent.getNodeId();

        boolean showInTrace = desmojEvent.traceIsOn();

        GatewayType type = processModel.getGateways().get(nodeId);
        ProcessSimulationComponents desmojObjects = desmojEvent.getDesmojObjects();

        try {
            Set<Integer> idsOfNextNodes = processModel.getIdsOfNextNodes(nodeId);

            if (idsOfNextNodes.size() > 1) { // split
                if (type == GatewayType.DEFAULT || type == GatewayType.EXCLUSIVE) {
                    Map<Integer, Object> branchingDistributions = desmojObjects.getExtensionDistributions()
                            .get(getName());
                    DiscreteDistEmpirical<Integer> distribution = (DiscreteDistEmpirical<Integer>) branchingDistributions
                            .get(nodeId);
                    // decide on next node
                    if (distribution != null) { //if a distribution is given take this
                        Integer nextFlowId = distribution.sample().intValue();
                        if (!processModel.getIdentifiers().keySet().contains(nextFlowId)) {
                            throw new ScyllaValidationException("Flow with id " + nextFlowId + " does not exist.");
                        }
                        scheduleNextEvent(desmojEvent, processInstance, processModel, nextFlowId);

                    } else { //otherwise try to get information out of the describing branches and branch on the basis of this
                        Map<Class<?>, ArrayList<PluginWrapper>> a = PluginLoader.getDefaultPluginLoader().getExtensions();
                        Collection<ArrayList<PluginWrapper>> plugins = a.values();
                        Boolean dataObjectPluginOn = false;
                        for (ArrayList<PluginWrapper> plugin : plugins) {
                            for (PluginWrapper p : plugin) {
                                if (p.toString().equals("DataObjectSCParserPlugin")) {
                                    dataObjectPluginOn = true;
                                }
                            }
                        }

                        if (dataObjectPluginOn) {
                            Object[] outgoingRefs = processModel.getGraph().getTargetObjects(nodeId).toArray();
                            Integer DefaultPath = null;
                            Boolean foundAWay = false;
                            for (Object or : outgoingRefs) { //go through all outgoing references
                                if (or.equals(getKeyByValue(processModel.getIdentifiers(), processModel.getNodeAttributes().get(desmojEvent.getNodeId()).get("default")))) { //if it's the default path jump it
                                    DefaultPath = (Integer) or;
                                    continue;
                                }
                                String[] conditions = processModel.getDisplayNames().get(or).split("&&");
                                Integer nextFlowId = (Integer) or;
                                List<Boolean> test = new ArrayList<>();
                                for (String condition : conditions) {
                                    condition = condition.trim();
                                    String field = null;
                                    String value = null;
                                    String comparison = null;

                                    if (condition.contains("==")) {
                                        field = condition.split("==")[0];
                                        value = condition.split("==")[1];
                                        //value = processModel.getDisplayNames().get(or).substring(2, processModel.getDisplayNames().get(or).length());
                                        comparison = "equal";
                                    } else if (condition.contains(">=")) {
                                        field = condition.split(">=")[0];
                                        value = condition.split(">=")[1];
                                        comparison = "greaterOrEqual";
                                    } else if (condition.contains("<=")) {
                                        field = condition.split("<=")[0];
                                        value = condition.split("<=")[1];
                                        comparison = "lessOrEqual";
                                    } else if (condition.contains("!=")) {
                                        field = condition.split("!=")[0];
                                        value = condition.split("!=")[1];
                                        comparison = "notEqual";
                                    } else if (condition.contains("=")) {
                                        field = condition.split("=")[0];
                                        value = condition.split("=")[1];
                                        comparison = "equal";
                                    } else if (condition.contains("<")) {
                                        field = condition.split("<")[0];
                                        value = condition.split("<")[1];
                                        comparison = "less";
                                    } else if (condition.contains(">")) {
                                        field = condition.split(">")[0];
                                        value = condition.split(">")[1];
                                        comparison = "greater";
                                    } else {
                                        throw new ScyllaValidationException("Condition " + condition + " does not have a comparison-operator");
                                    }
                                    value = value.trim();
                                    field = field.trim();

                                    Object fieldValue = DataObjectField.getDataObjectValue(processInstance.getId(), field);

                                    if (!isParsableAsLong(value) || !isParsableAsLong((String.valueOf(fieldValue)))) { //try a long comparison
                                        Integer comparisonResult = (String.valueOf(fieldValue)).trim().compareTo(String.valueOf(value));
                                        if (comparison.equals("equal") && comparisonResult == 0) {
                                            break;
                                        } else if (comparison.equals("notEqual") && comparisonResult != 0) {
                                            break;
                                        } else {
                                            test.add(false);
                                        }

                                    } else { //otherwise do a string compare
                                        Long LongValue = Long.valueOf(value);
                                        Long dOValue = Long.valueOf((String.valueOf(fieldValue)));
                                        Integer comparisonResult = (dOValue.compareTo(LongValue));

                                        if (comparison.equals("equal") && comparisonResult == 0) {
                                        } else if (comparison.equals("less") && comparisonResult < 0) {
                                        } else if (comparison.equals("greater") && comparisonResult > 0) {
                                        } else if (comparison.equals("greaterOrEqual") && comparisonResult >= 0) {
                                        } else if (comparison.equals("lessOrEqual") && comparisonResult <= 0) {
                                        } else {
                                            test.add(false);
                                        }
                                    }

                                }
                                if (test.size() == 0) {
                                    scheduleNextEvent(desmojEvent, processInstance, processModel, nextFlowId);
                                    foundAWay = true;
                                }
                            }
                            if (!foundAWay && DefaultPath != null) {
                                scheduleNextEvent(desmojEvent, processInstance, processModel, DefaultPath);
                            } else if (!foundAWay && DefaultPath == null) {
                                //everything will be killed, logical error
                                throw new ScyllaValidationException("No Default Path for " + desmojEvent.getDisplayName() + " given and outgoing branches not complete. No branch matches, abort.");
                            }
                        } else {
                            Object[] outgoingRefs = processModel.getGraph().getTargetObjects(nodeId).toArray();
                            Integer DefaultPath = null;
                            for (Object or : outgoingRefs) { //try to find default path
                                if (or.equals(getKeyByValue(processModel.getIdentifiers(), processModel.getNodeAttributes().get(desmojEvent.getNodeId()).get("default")))) {
                                    DefaultPath = (Integer) or;
                                    break;
                                }
                            }
                            if (DefaultPath != null) {
                                scheduleNextEvent(desmojEvent, processInstance, processModel, DefaultPath);
                            } else {
                                throw new ScyllaValidationException("No Distribution for " + desmojEvent.getDisplayName() + " given, no DefaultPath given and DataObject PlugIn not activated.");
                            }
                        }
                    }
                }
            }
        } catch (NodeNotFoundException | ScyllaValidationException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            SimulationUtils.abort(model, processInstance, nodeId, showInTrace);
            return;
        }
    }

    private static boolean isParsableAsLong(final String s) {
        try {
            Long.valueOf(s);
            return true;
        } catch (NumberFormatException numberFormatException) {
            return false;
        }
    }

    public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Entry<T, E> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }
}
