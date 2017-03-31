package de.hpi.bpt.scylla.parser;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.jdom2.Element;
import org.jdom2.Namespace;

import de.hpi.bpt.scylla.SimulationManager;
import de.hpi.bpt.scylla.exception.ScyllaValidationException;
import de.hpi.bpt.scylla.logger.DebugLogger;
import de.hpi.bpt.scylla.model.configuration.ResourceReference;
import de.hpi.bpt.scylla.model.configuration.SimulationConfiguration;
import de.hpi.bpt.scylla.model.configuration.distribution.BinomialDistribution;
import de.hpi.bpt.scylla.model.configuration.distribution.ConstantDistribution;
import de.hpi.bpt.scylla.model.configuration.distribution.Distribution;
import de.hpi.bpt.scylla.model.configuration.distribution.EmpiricalDistribution;
import de.hpi.bpt.scylla.model.configuration.distribution.EmpiricalStringDistribution;
import de.hpi.bpt.scylla.model.configuration.distribution.ErlangDistribution;
import de.hpi.bpt.scylla.model.configuration.distribution.ExponentialDistribution;
import de.hpi.bpt.scylla.model.configuration.distribution.NormalDistribution;
import de.hpi.bpt.scylla.model.configuration.distribution.PoissonDistribution;
import de.hpi.bpt.scylla.model.configuration.distribution.TimeDistributionWrapper;
import de.hpi.bpt.scylla.model.configuration.distribution.TriangularDistribution;
import de.hpi.bpt.scylla.model.configuration.distribution.UniformDistribution;
import de.hpi.bpt.scylla.model.global.resource.Resource;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.simulation.utils.DateTimeUtils;

/**
 * Parses all process-specific simulation parameters which are necessary for conducting the simulation.
 * 
 * @author Tsun Yin Wong
 * 
 */
public class SimulationConfigurationParser extends Parser<SimulationConfiguration> {

    public SimulationConfigurationParser(SimulationManager simulationEnvironment) {
        super(simulationEnvironment);
    }

    @Override
    public SimulationConfiguration parse(Element rootElement) throws ScyllaValidationException {
        Namespace simNamespace = rootElement.getNamespace();
        List<Element> simElements = rootElement.getChildren("simulationConfiguration", simNamespace);

        if (simElements.isEmpty()) {
            throw new ScyllaValidationException("No simulation configuration in file.");
        }
        else if (simElements.size() > 1) {
            throw new ScyllaValidationException(
                    "Multiple simulation configurations in file. If you want to simulate mulitple scenarios, then store them in separate simulation configuration files.");
        }

        Element sim = simElements.get(0);

        String processRef = sim.getAttributeValue("processRef");
        ProcessModel processModel = simulationEnvironment.getProcessModels().get(processRef);
        Long randomSeed = simulationEnvironment.getGlobalConfiguration().getRandomSeed();

        SimulationConfiguration simulationConfiguration = parseSimulationConfiguration(sim, simNamespace, processRef,
                processModel, randomSeed);

        // if (processIdToPoolName.containsKey(processId)) {
        // String participant = processIdToPoolName.get(processId);
        // processModel.setParticipant(participant);
        // }
        // if (!globalTasks.isEmpty()) {
        // processModel.setGlobalTasks(globalTasks);
        // processModel.setGlobalTaskElements(globalTaskElements);
        // }
        return simulationConfiguration;
    }

    private SimulationConfiguration parseSimulationConfiguration(Element sim, Namespace simNamespace,
            String processIdFromSimElement, ProcessModel processModel, Long randomSeed)
                    throws ScyllaValidationException {

        Map<String, Resource> resources = simulationEnvironment.getGlobalConfiguration().getResources();

        if (processModel == null) {
            throw new ScyllaValidationException("Simulation configuration is for (sub)process '"
                    + processIdFromSimElement + "', which is not found in the simulation environment.");
        }

        String processRef = processIdFromSimElement;
        String simId = null;
        Integer numberOfProcessInstances = null;
        ZonedDateTime startDateTime = null;
        ZonedDateTime endDateTime = null;

        if (processModel.getParent() == null) {
            List<Element> startEvents = sim.getChildren("startEvent", simNamespace);
            if (startEvents.size() == 0)
                throw new ScyllaValidationException("No definition of start event in simulation scenario.");
            else {
                for (Element el : startEvents) {
                    // it is sufficient if at least one of the start events has an arrival rate defined
                    if (el.getChild("arrivalRate", simNamespace) != null) {
                        break;
                    }
                    throw new ScyllaValidationException(
                            "No arrival rate defined in any of the start events in simulation scenario.");
                }
            }
            // store identifier of simulation configuration only if it is for top level process
            simId = sim.getAttributeValue("id");

            numberOfProcessInstances = Integer.valueOf(sim.getAttributeValue("processInstances"));

            startDateTime = DateTimeUtils.parse(sim.getAttributeValue("startDateTime"));

            String endDateTimeString = sim.getAttributeValue("endDateTime");
            if (endDateTimeString != null) {
                endDateTime = DateTimeUtils.parse(endDateTimeString);
            }

            String randomSeedString = sim.getAttributeValue("randomSeed");
            if (randomSeedString != null) {
                randomSeed = Long.valueOf(sim.getAttributeValue("randomSeed"));
                DebugLogger.log("Random seed for simulation configuration " + processRef + ": " + randomSeed);
            }
        }

        Map<Integer, TimeDistributionWrapper> arrivalRates = new HashMap<Integer, TimeDistributionWrapper>();
        Map<Integer, TimeDistributionWrapper> durations = new HashMap<Integer, TimeDistributionWrapper>();
        Map<Integer, Set<ResourceReference>> resourceReferences = new HashMap<Integer, Set<ResourceReference>>();
        // gateways and events
        // Map<Integer, BranchingBehavior> branchingBehaviors = new HashMap<Integer, BranchingBehavior>();
        Map<Integer, SimulationConfiguration> configurationsOfSubProcesses = new HashMap<Integer, SimulationConfiguration>();

        // take resource definitions from process model
        Map<Integer, Set<String>> resourceReferencesFromProcessModel = processModel.getResourceReferences();
        for (Integer nodeId : resourceReferencesFromProcessModel.keySet()) {
            Set<String> resourceRefFromModel = resourceReferencesFromProcessModel.get(nodeId);
            Set<ResourceReference> resourceRefs = new HashSet<ResourceReference>();
            for (String resourceId : resourceRefFromModel) {
                int amount = 1;
                Map<String, String> assignmentDefinition = new HashMap<String, String>();
                ResourceReference resourceReference = new ResourceReference(resourceId, amount, assignmentDefinition);
                resourceRefs.add(resourceReference);
            }
            resourceReferences.put(nodeId, resourceRefs);
        }

        for (Element el : sim.getChildren()) {
            String elementName = el.getName();
            if (isKnownElement(elementName)) {

                if (elementName.equals("resources")) {
                    // are childs of subProcess element, but have already been handled like children of a task element
                    // in parent process
                    continue;
                }

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

                if (elementName.equals("startEvent")) {
                    Element elem = el.getChild("arrivalRate", simNamespace);
                    if (elem != null) {
                        TimeDistributionWrapper distribution = getTimeDistributionWrapper(elem, simNamespace);
                        arrivalRates.put(nodeId, distribution);
                    }
                }
                else if (elementName.equals("task") || elementName.endsWith("Task")
                        || elementName.equals("subProcess")) {
                    Element durationElem = el.getChild("duration", simNamespace);
                    if (durationElem != null) {
                    	TimeDistributionWrapper distribution = getTimeDistributionWrapper(durationElem, simNamespace);
                        durations.put(nodeId, distribution);
                    }
                    Element resourcesElem = el.getChild("resources", simNamespace);
                    if (resourcesElem != null) {
                        List<Element> resourceElements = resourcesElem.getChildren("resource", simNamespace);
                        // use resource assignment from simulation configuration (eventually overrides the one from
                        // process model)
                        if (resourceElements.size() > 0) {
                            Set<ResourceReference> resourceRefs = new HashSet<ResourceReference>();
                            Set<String> resourceIdentifiers = new HashSet<String>();
                            for (Element elem : resourceElements) {
                                String resourceId = elem.getAttributeValue("id");
                                if (resources.get(resourceId) == null) {
                                    throw new ScyllaValidationException("Simulation configuration " + simId
                                            + " refers to unknown resource " + resourceId + ".");
                                }
                                if (resourceIdentifiers.contains(resourceId)) {
                                    throw new ScyllaValidationException("Simulation configuration " + simId
                                            + " defines multiple resources for task / subprocess " + identifier);
                                }
                                resourceIdentifiers.add(resourceId);

                                // XXX implementation currently supports more than one instance per resource. however
                                // BPMN standard does not support that.
                                // int amount = 1;
                                int amount = Integer.parseInt(elem.getAttributeValue("amount"));
                                Map<String, String> assignmentDefinition = new HashMap<String, String>();
                                Element assignmentDefinitionElement = elem.getChild("assignmentDefinition",
                                        simNamespace);
                                if (assignmentDefinitionElement != null) {
                                    List<Element> adElements = assignmentDefinitionElement.getChildren(null,
                                            simNamespace);
                                    for (Element adElem : adElements) {
                                        assignmentDefinition.put(adElem.getName(), adElem.getText());
                                    }
                                }
                                ResourceReference resourceReference = new ResourceReference(resourceId, amount,
                                        assignmentDefinition);
                                resourceRefs.add(resourceReference);
                            }
                            resourceReferences.put(nodeId, resourceRefs);
                        }
                    }
                    if (elementName.equals("subProcess")) {
                        ProcessModel subProcess = processModel.getSubProcesses().get(nodeId);
                        String subProcessIdFromSimElement = el.getAttributeValue("id");
                        SimulationConfiguration simulationConfiguration = parseSimulationConfiguration(el, simNamespace,
                                subProcessIdFromSimElement, subProcess, randomSeed);
                        configurationsOfSubProcesses.put(nodeId, simulationConfiguration);
                    }
                }
                else {
                    DebugLogger.log("Element " + el.getName()
                            + " of simulation scenario is expected to be known, but not supported.");
                }
            }
            else {
                DebugLogger.log("Element " + el.getName() + " of simulation scenario not supported.");
            }
        }

        SimulationConfiguration simulationConfiguration = new SimulationConfiguration(simId, processModel,
                numberOfProcessInstances, startDateTime, endDateTime, randomSeed, arrivalRates, durations,
                resourceReferences, configurationsOfSubProcesses);

        return simulationConfiguration;
    }

    private boolean isKnownElement(String name) {
        return name.equals("task") || name.endsWith("Task") || name.equals("startEvent") || name.equals("subProcess")
                || name.equals("resources");
    }    
    
	public static TimeDistributionWrapper getTimeDistributionWrapper(Element element, Namespace simNamespace) 
			throws ScyllaValidationException {
    	Distribution distribution = getDistribution(element, simNamespace);
    	TimeUnit timeUnit = TimeUnit.valueOf(element.getAttributeValue("timeUnit"));
    	TimeDistributionWrapper distWrapper = new TimeDistributionWrapper(timeUnit);
    	distWrapper.setDistribution(distribution);
    	return distWrapper;
    }
    
    public static Distribution getDistribution(Element element, Namespace simNamespace)
            throws ScyllaValidationException {
    	Distribution distribution;
        if (element.getChild("binomialDistribution", simNamespace) != null) {
            Element el = element.getChild("binomialDistribution", simNamespace);
            double probability = Double.valueOf(el.getChildText("probability", simNamespace));
            int amount = Integer.valueOf(el.getChildText("amount", simNamespace));
            distribution = new BinomialDistribution(probability, amount);
        }
        else if (element.getChild("constantDistribution", simNamespace) != null) {
            Element el = element.getChild("constantDistribution", simNamespace);
            double constantValue = Double.valueOf(el.getChildText("constantValue", simNamespace));
            distribution = new ConstantDistribution(constantValue);
        }
        else if (element.getChild("empiricalDistribution", simNamespace) != null) {
            Element el = element.getChild("empiricalDistribution", simNamespace);
            EmpiricalDistribution dist = new EmpiricalDistribution();
            List<Element> entries = el.getChildren("entry", simNamespace);
            if (entries.isEmpty()) {
                throw new ScyllaValidationException("No entries in empirical distribution.");
            }
            double sum = 0;
            for (Element entry : entries) { // normalize frequency to 1.0
                sum += Double.valueOf(entry.getAttributeValue("frequency"));
            }
            for (Element entry : entries) {
                dist.addEntry(Double.valueOf(entry.getAttributeValue("value")),
                        Double.valueOf(entry.getAttributeValue("frequency")) / sum);
            }
            distribution = dist;
        }
        else if (element.getChild("empiricalStringDistribution", simNamespace) != null) {
            Element el = element.getChild("empiricalStringDistribution", simNamespace);
            EmpiricalStringDistribution dist = new EmpiricalStringDistribution();
            List<Element> entries = el.getChildren("entry", simNamespace);
            if (entries.isEmpty()) {
                throw new ScyllaValidationException("No entries in empirical distribution.");
            }
            double sum = 0;
            for (Element entry : entries) { // normalize frequency to 1.0
                sum += Double.valueOf(entry.getAttributeValue("frequency"));
            }
            for (Element entry : entries) {
                dist.addEntry(entry.getAttributeValue("value"),
                        Double.valueOf(entry.getAttributeValue("frequency")) / sum);
            }
            distribution = dist;
        }
        else if (element.getChild("erlangDistribution", simNamespace) != null) {
            Element el = element.getChild("erlangDistribution", simNamespace);
            long order = Long.valueOf(el.getChildText("order", simNamespace));
            double mean = Double.valueOf(el.getChildText("mean", simNamespace));
            distribution = new ErlangDistribution(order, mean);
        }
        else if (element.getChild("exponentialDistribution", simNamespace) != null) {
            Element el = element.getChild("exponentialDistribution", simNamespace);
            double mean = Double.valueOf(el.getChildText("mean", simNamespace));
            distribution = new ExponentialDistribution(mean);
        }
        else if (element.getChild("triangularDistribution", simNamespace) != null) {
            Element el = element.getChild("triangularDistribution", simNamespace);
            double lower = Double.valueOf(el.getChildText("lower", simNamespace));
            double upper = Double.valueOf(el.getChildText("upper", simNamespace));
            double peak = Double.valueOf(el.getChildText("peak", simNamespace));
            distribution = new TriangularDistribution(lower, upper, peak);
        }
        else if (element.getChild("normalDistribution", simNamespace) != null) {
            Element el = element.getChild("normalDistribution", simNamespace);
            double mean = Double.valueOf(el.getChildText("mean", simNamespace));
            double standardDeviation = Double.valueOf(el.getChildText("standardDeviation", simNamespace));
            distribution = new NormalDistribution(mean, standardDeviation);
        }
        else if (element.getChild("poissonDistribution", simNamespace) != null) {
            Element el = element.getChild("poissonDistribution", simNamespace);
            double mean = Double.valueOf(el.getChildText("mean", simNamespace));
            distribution = new PoissonDistribution(mean);
        }
        else if (element.getChild("uniformDistribution", simNamespace) != null) {
            Element el = element.getChild("uniformDistribution", simNamespace);
            double lower = Double.valueOf(el.getChildText("lower", simNamespace));
            double upper = Double.valueOf(el.getChildText("upper", simNamespace));
            distribution = new UniformDistribution(lower, upper);
        }
        else {
            throw new ScyllaValidationException("Distribution definition not found or not supported.");
        }
        return distribution;
    }
}
