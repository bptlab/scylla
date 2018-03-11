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
    // This is the extra simulation configuration parser for the boundary events that extends the default parser.
    @Override
    public String getName() {
        return BoundaryEventPluginUtils.PLUGIN_NAME;
    }

    //Therefore this method should be called just once for a sim.xml file
    @Override
    public Map<String, Object> parse(SimulationConfiguration simulationInput, Element sim)
            throws ScyllaValidationException {

        Map<Integer, BranchingBehavior> branchingBehaviors = new HashMap<Integer, BranchingBehavior>();

        Namespace simNamespace = sim.getNamespace();

        ProcessModel processModel = simulationInput.getProcessModel();

        for (Element element : sim.getChildren()) {
            String elementName = element.getName();

            // Why should that be needed? The next if clause should filter them out anyway...
            /*if (elementName.equals("boundaryEvents")) {
                // Are childs of subProcess element, but have already been handled like children of a task element
                // in parent process.
                continue;
            }*/

            if (elementName.equals("task") || elementName.endsWith("Task") || elementName.equals("subProcess")) {

                String identifier = element.getAttributeValue("id");
                if (identifier == null) {
                    DebugLogger.log("Warning: Simulation configuration definition element '" + elementName
                            + "' does not have an identifier, skip.");
                    continue; // No matching element in process, so skip definition.
                }
                Integer nodeId = processModel.getIdentifiersToNodeIds().get(identifier);
                if (nodeId == null) {
                    DebugLogger.log("Simulation configuration definition for process element '" + identifier
                            + "', but not available in process, skip.");
                    continue; // No matching element in process, so skip definition
                }

                // Get the list of all corresponding boundary events.
                Element boundaryEventsElem = element.getChild("boundaryEvents", simNamespace);
                if (boundaryEventsElem != null) {
                    List<Element> boundaryEventElements = boundaryEventsElem.getChildren("boundaryEvent", simNamespace);
                    if (boundaryEventElements.size() > 0) {
                        Map<Integer, Double> probabilities = new HashMap<Integer, Double>();
                        double probabilityOfStandardFlow = 1;
                        for (Element elem : boundaryEventElements) {
                            // Collect probabilities of boundary event occurrence...

                            String id = elem.getAttributeValue("id");
                            Integer nodeIdOfBoundaryEvent = processModel.getIdentifiersToNodeIds().get(id);
                            if (nodeIdOfBoundaryEvent == null) {
                                throw new ScyllaValidationException(
                                        "Simulation configuration refers to unknown boundary event: " + id);
                            }

                            // ... and subtract them from the default path behavior. The percentage of the default path is just
                            // relevant for interrupting events because its implicit 1 at tasks with only nn-interrupting events.
                            Element eventProbabilityElement = elem.getChild("eventProbability", simNamespace);
                            Double probabilityOfBoundaryEvent = Double.parseDouble(eventProbabilityElement.getText());
                            probabilityOfStandardFlow -= probabilityOfBoundaryEvent;
                            probabilities.put(nodeIdOfBoundaryEvent, probabilityOfBoundaryEvent);

                            // Add the arrival rate (= which is here the time at which the boundary event shall occur) relatively to the start of its task.
                            Element arrivalRateElement = elem.getChild("arrivalRate", simNamespace);
                            if (arrivalRateElement != null) {
                                TimeDistributionWrapper distribution = SimulationConfigurationParser
                                        .getTimeDistributionWrapper(arrivalRateElement, simNamespace);
                                // TODO: should be put into an extension attribute
                                simulationInput.getArrivalRates().put(nodeIdOfBoundaryEvent, distribution);
                            }
                        }
                        if (probabilityOfStandardFlow < 0) { // The probability values of boundary events are added why they should not exceed 1 in total.
                            throw new ScyllaValidationException(
                                    "Simulation configuration defines probabilities for boundary events of task "
                                            + identifier + ", exceeding 1 in total.");
                        }
                        /*else if (probabilityOfStandardFlow == 0) { // Does not happen anymore and should not happen semantically correct.
                            DebugLogger
                                    .log("Warning: Simulation configuration defines probabilities for boundary events of task "
                                            + identifier + ", but does not allow the normal flow to fire. \n"
                                            + "This may result in an infinite number of firings of boundary events "
                                            + "if none of them is interrupting.");
                        }*/
                        // XXX timer events do not have probabilities
                        // XXX probability of normal flow is stored under nodeId of task

                        // And save them for now.
                        probabilities.put(nodeId, probabilityOfStandardFlow);
                        BranchingBehavior branchingBehavior = new BranchingBehavior(probabilities);
                        branchingBehaviors.put(nodeId, branchingBehavior);
                    }
                }
            }
        }

        // Store all branching behaviors collected for that task.
        HashMap<String, Object> extensionAttributes = new HashMap<String, Object>();
        extensionAttributes.put("branchingBehaviors", branchingBehaviors);

        return extensionAttributes;
    }

}
