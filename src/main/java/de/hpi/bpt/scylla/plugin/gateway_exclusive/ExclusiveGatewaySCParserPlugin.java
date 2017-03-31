package de.hpi.bpt.scylla.plugin.gateway_exclusive;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Element;
import org.jdom2.Namespace;

import de.hpi.bpt.scylla.exception.ScyllaValidationException;
import de.hpi.bpt.scylla.logger.DebugLogger;
import de.hpi.bpt.scylla.model.configuration.BranchingBehavior;
import de.hpi.bpt.scylla.model.configuration.SimulationConfiguration;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.model.process.graph.exception.NodeNotFoundException;
import de.hpi.bpt.scylla.plugin_type.parser.SimulationConfigurationParserPluggable;

public class ExclusiveGatewaySCParserPlugin extends SimulationConfigurationParserPluggable {

    @Override
    public String getName() {
        return ExclusiveGatewayPluginUtils.PLUGIN_NAME;
    }

    @Override
    public Map<String, Object> parse(SimulationConfiguration simulationInput, Element sim)
            throws ScyllaValidationException {

        Map<Integer, BranchingBehavior> branchingBehaviors = new HashMap<Integer, BranchingBehavior>();

        Namespace simNamespace = sim.getNamespace();

        ProcessModel processModel = simulationInput.getProcessModel();

        for (Element el : sim.getChildren()) {
            String elementName = el.getName();

            if (elementName.equals("exclusiveGateway")) {

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

                List<Element> outgoingSequenceFlows = el.getChildren("outgoingSequenceFlow", simNamespace);
                if (outgoingSequenceFlows.size() > 0) {
                    Map<Integer, Double> branchingProbabilities = new HashMap<Integer, Double>();
                    Double probabilitySum = 0d;
                    for (Element elem : outgoingSequenceFlows) {
                        Integer nodeIdOfSequenceFlow = processModel.getIdentifiersToNodeIds()
                                .get(elem.getAttributeValue("id"));
                        if (nodeIdOfSequenceFlow != null) {
                            Double branchingProbability = Double
                                    .parseDouble(elem.getChildText("branchingProbability", simNamespace));
                            if (branchingProbability < 0 || branchingProbability > 1) {
                                throw new ScyllaValidationException("Exclusive gateway branching probability for "
                                        + identifier + " is out of bounds [0,1].");
                            }
                            probabilitySum += branchingProbability;
                            branchingProbabilities.put(nodeIdOfSequenceFlow, branchingProbability);

                        }
                    }

                    if (probabilitySum <= 0) {
                        throw new ScyllaValidationException(
                                "Simulation configuration defines branching probabilities for exclusive gateway "
                                        + identifier + ", where the sum of probabilities is negative or zero.");
                    }
                    if (probabilitySum > 1) { // XXX imprecision by IEEE 754 floating point representation
                        throw new ScyllaValidationException(
                                "Simulation configuration defines branching probabilities for exclusive gateway "
                                        + identifier + ", exceeding 1 in total.");
                    }

                    // complete probabilities with the default flow probability
                    if (probabilitySum > 0 && probabilitySum <= 1) {
                        Map<String, String> gatewayAttributes = processModel.getNodeAttributes().get(nodeId);
                        String defaultFlowIdentifier = gatewayAttributes.get("default");
                        if (defaultFlowIdentifier != null) {
                            double probabilityOfDefaultFlow = 1 - probabilitySum;
                            int defaultFlowNodeId = processModel.getIdentifiersToNodeIds().get(defaultFlowIdentifier);
                            if (!branchingProbabilities.containsKey(defaultFlowNodeId)) {
                                branchingProbabilities.put(defaultFlowNodeId, probabilityOfDefaultFlow);
                            }
                            else {
                                branchingProbabilities.put(defaultFlowNodeId,
                                        branchingProbabilities.get(defaultFlowNodeId) + probabilityOfDefaultFlow);
                            };
                        }
                    }

                    try {
                        if (branchingProbabilities.keySet().size() != processModel.getIdsOfNextNodes(nodeId).size()) {
                            throw new ScyllaValidationException(
                                    "Number of branching probabilities defined in simulation configuration "
                                            + "does not match to number of outgoing flows of exclusive gateway "
                                            + identifier + ".");
                        }
                    }
                    catch (NodeNotFoundException e) {
                        throw new ScyllaValidationException("Node not found: " + e.getMessage());
                    }

                    BranchingBehavior branchingBehavior = new BranchingBehavior(branchingProbabilities);
                    branchingBehaviors.put(nodeId, branchingBehavior);
                }
            }

        }

        HashMap<String, Object> extensionAttributes = new HashMap<String, Object>();
        extensionAttributes.put("branchingBehaviors", branchingBehaviors);

        return extensionAttributes;
    }

}
