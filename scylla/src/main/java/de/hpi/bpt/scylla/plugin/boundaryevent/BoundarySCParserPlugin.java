package de.hpi.bpt.scylla.plugin.boundaryevent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Element;
import org.jdom2.Namespace;

import de.hpi.bpt.scylla.exception.ScyllaValidationException;
import de.hpi.bpt.scylla.logger.DebugLogger;
import de.hpi.bpt.scylla.model.configuration.BranchingBehavior;
import de.hpi.bpt.scylla.model.configuration.SimulationConfiguration;
import de.hpi.bpt.scylla.model.configuration.distribution.TimeDistributionWrapper;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.parser.SimulationConfigurationParser;
import de.hpi.bpt.scylla.plugin_type.parser.SimulationConfigurationParserPluggable;

public class BoundarySCParserPlugin extends SimulationConfigurationParserPluggable {

    @Override
    public String getName() {
        return BoundaryEventPluginUtils.PLUGIN_NAME;
    }

    @Override
    public Map<String, Object> parse(SimulationConfiguration simulationInput, Element sim)
            throws ScyllaValidationException {

        Map<Integer, BranchingBehavior> branchingBehaviors = new HashMap<Integer, BranchingBehavior>();

        Namespace simNamespace = sim.getNamespace();

        ProcessModel processModel = simulationInput.getProcessModel();

        for (Element el : sim.getChildren()) {
            String elementName = el.getName();

            if (elementName.equals("boundaryEvents")) {
                // are childs of subProcess element, but have already been handled like children of a task element
                // in parent process
                continue;
            }

            if (elementName.equals("task") || elementName.endsWith("Task") || elementName.equals("subProcess")) {

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

                Element boundaryEventsElem = el.getChild("boundaryEvents", simNamespace);
                if (boundaryEventsElem != null) {
                    List<Element> boundaryEventElements = boundaryEventsElem.getChildren("boundaryEvent", simNamespace);
                    if (boundaryEventElements.size() > 0) {
                        Map<Integer, Double> probabilities = new HashMap<Integer, Double>();
                        double probabilityOfStandardFlow = 1;
                        for (Element elem : boundaryEventElements) {
                            // collect probabilities of boundary event occurrence
                            String id = elem.getAttributeValue("id");
                            Integer nodeIdOfBoundaryEvent = processModel.getIdentifiersToNodeIds().get(id);
                            if (nodeIdOfBoundaryEvent == null) {
                                throw new ScyllaValidationException(
                                        "Simulation configuration refers to unknown boundary event: " + id);
                            }
                            Element eventProbabilityElement = elem.getChild("eventProbability", simNamespace);
                            Double probabilityOfBoundaryEvent = Double.parseDouble(eventProbabilityElement.getText());
                            probabilityOfStandardFlow -= probabilityOfBoundaryEvent;
                            probabilities.put(nodeIdOfBoundaryEvent, probabilityOfBoundaryEvent);
                            // add arrival rate (= which is here the time at which the boundary event shall occur)
                            Element arrivalRateElement = elem.getChild("arrivalRate", simNamespace);
                            if (arrivalRateElement != null) {
                                TimeDistributionWrapper distribution = SimulationConfigurationParser
                                        .getTimeDistributionWrapper(arrivalRateElement, simNamespace);
                                // TODO: should be put into an extension attribute
                                simulationInput.getArrivalRates().put(nodeIdOfBoundaryEvent, distribution);
                            }
                        }
                        if (probabilityOfStandardFlow < 0) {
                            throw new ScyllaValidationException(
                                    "Simulation configuration defines probabilities for boundary events of task "
                                            + identifier + ", exceeding 1 in total.");
                        }
                        else if (probabilityOfStandardFlow == 0) {
                            DebugLogger
                                    .log("Warning: Simulation configuration defines probabilities for boundary events of task "
                                            + identifier + ", but does not allow the normal flow to fire. \n"
                                            + "This may result in an infinite number of firings of boundary events "
                                            + "if none of them is interrupting.");
                        }
                        // XXX timer events do not have probabilities
                        // XXX probability of normal flow is stored under nodeId of task
                        probabilities.put(nodeId, probabilityOfStandardFlow);
                        BranchingBehavior branchingBehavior = new BranchingBehavior(probabilities);
                        branchingBehaviors.put(nodeId, branchingBehavior);
                    }
                }
            }
        }

        HashMap<String, Object> extensionAttributes = new HashMap<String, Object>();
        extensionAttributes.put("branchingBehaviors", branchingBehaviors);

        return extensionAttributes;
    }

}
