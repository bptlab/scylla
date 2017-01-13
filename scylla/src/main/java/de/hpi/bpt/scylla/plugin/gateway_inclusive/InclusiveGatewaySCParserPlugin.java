package de.hpi.bpt.scylla.plugin.gateway_inclusive;

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
import de.hpi.bpt.scylla.plugin_type.parser.SimulationConfigurationParserPluggable;

public class InclusiveGatewaySCParserPlugin extends SimulationConfigurationParserPluggable {

    @Override
    public String getName() {
        return InclusiveGatewayPluginUtils.PLUGIN_NAME;
    }

    @Override
    public Map<String, Object> parse(SimulationConfiguration simulationInput, Element sim)
            throws ScyllaValidationException {

        Map<Integer, BranchingBehavior> branchingBehaviors = new HashMap<Integer, BranchingBehavior>();

        Namespace simNamespace = sim.getNamespace();

        ProcessModel processModel = simulationInput.getProcessModel();

        for (Element el : sim.getChildren()) {
            String elementName = el.getName();

            if (elementName.equals("inclusiveGateway")) {

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
                    for (Element elem : outgoingSequenceFlows) {
                        Integer nodeIdOfSequenceFlow = processModel.getIdentifiersToNodeIds()
                                .get(elem.getAttributeValue("id"));
                        if (nodeIdOfSequenceFlow != null) {
                            Double branchingProbability = Double
                                    .valueOf(elem.getChildText("branchingProbability", simNamespace));
                            if (branchingProbability < 0 || branchingProbability > 1) {
                                throw new ScyllaValidationException("Inclusive gateway branching probability for "
                                        + identifier + " is out of bounds [0,1].");
                            }
                            branchingProbabilities.put(nodeIdOfSequenceFlow, branchingProbability);
                        }
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
