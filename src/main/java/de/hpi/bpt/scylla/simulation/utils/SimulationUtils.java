package de.hpi.bpt.scylla.simulation.utils;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import co.paralleluniverse.fibers.SuspendExecution;
import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.exception.ScyllaValidationException;
import de.hpi.bpt.scylla.model.configuration.distribution.BinomialDistribution;
import de.hpi.bpt.scylla.model.configuration.distribution.ConstantDistribution;
import de.hpi.bpt.scylla.model.configuration.distribution.Distribution;
import de.hpi.bpt.scylla.model.configuration.distribution.EmpiricalDistribution;
import de.hpi.bpt.scylla.model.configuration.distribution.EmpiricalStringDistribution;
import de.hpi.bpt.scylla.model.configuration.distribution.ErlangDistribution;
import de.hpi.bpt.scylla.model.configuration.distribution.ExponentialDistribution;
import de.hpi.bpt.scylla.model.configuration.distribution.NormalDistribution;
import de.hpi.bpt.scylla.model.configuration.distribution.PoissonDistribution;
import de.hpi.bpt.scylla.model.configuration.distribution.TriangularDistribution;
import de.hpi.bpt.scylla.model.configuration.distribution.UniformDistribution;
import de.hpi.bpt.scylla.model.global.resource.TimetableItem;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.model.process.graph.exception.NodeNotFoundException;
import de.hpi.bpt.scylla.model.process.node.EventType;
import de.hpi.bpt.scylla.model.process.node.GatewayType;
import de.hpi.bpt.scylla.plugin_type.simulation.EventCreationPluggable;
import de.hpi.bpt.scylla.plugin_type.simulation.EventSchedulingPluggable;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.ProcessSimulationComponents;
import de.hpi.bpt.scylla.simulation.ResourceObject;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import de.hpi.bpt.scylla.simulation.event.BPMNEndEvent;
import de.hpi.bpt.scylla.simulation.event.BPMNIntermediateEvent;
import de.hpi.bpt.scylla.simulation.event.GatewayEvent;
import de.hpi.bpt.scylla.simulation.event.ResourceAvailabilityEvent;
import de.hpi.bpt.scylla.simulation.event.ScyllaEvent;
import de.hpi.bpt.scylla.simulation.event.TaskEnableEvent;
import desmoj.core.dist.ContDistErlang;
import desmoj.core.dist.ContDistExponential;
import desmoj.core.dist.ContDistNormal;
import desmoj.core.dist.ContDistTriangular;
import desmoj.core.dist.ContDistUniform;
import desmoj.core.dist.DiscreteDistBinomial;
import desmoj.core.dist.DiscreteDistConstant;
import desmoj.core.dist.DiscreteDistEmpirical;
import desmoj.core.dist.DiscreteDistPoisson;
import desmoj.core.dist.NumericalDist;
import desmoj.core.simulator.ExternalEventStop;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;

public class SimulationUtils {

    /**
     * Gets type of next node and prepare respective event.
     * 
     * @param model
     *            the simulation model
     * @param processModel
     *            the process model
     * @param processInstanceId
     *            the identifier of the process instance
     * @param nextNodeId
     *            the identifier of the next node
     * @return the DesmoJ representing the next node plus DesmoJ events from plug-ins
     * @throws ScyllaRuntimeException
     * @throws NodeNotFoundException
     * @throws ScyllaValidationException
     */
    public static List<ScyllaEvent> createEventsForNextNode(ScyllaEvent currentEvent,
            ProcessSimulationComponents desmojObjects, ProcessInstance processInstance, int nextNodeId)
                    throws ScyllaRuntimeException, NodeNotFoundException, ScyllaValidationException {

        SimulationModel model = (SimulationModel) processInstance.getModel();
        TimeInstant currentSimulationTime = model.presentTime();
        ProcessModel processModel = processInstance.getProcessModel();

        String source = currentEvent.getSource();

        List<ScyllaEvent> events = new ArrayList<ScyllaEvent>();

        if (processModel.getTasks().containsKey(nextNodeId) || processModel.getSubProcesses().containsKey(nextNodeId)) {
            // TaskType tType = processModel.getTasks().get(nextNodeId);
            ScyllaEvent event = new TaskEnableEvent(model, source, currentSimulationTime, desmojObjects,
                    processInstance, nextNodeId);
            events.add(event);
        }
        else if (processModel.getGateways().containsKey(nextNodeId)) {

            GatewayType gType = processModel.getGateways().get(nextNodeId);

            Set<Integer> idsOfNodesBeforeGateway = processModel.getIdsOfPreviousNodes(nextNodeId);

            if (gType == GatewayType.PARALLEL && idsOfNodesBeforeGateway.size() > 1) {
                Map<Integer, Set<Integer>> referenceToEventsOnHold = processInstance.getNodesAndTriggers();
                if (!referenceToEventsOnHold.containsKey(nextNodeId)) {
                    referenceToEventsOnHold.put(nextNodeId, new HashSet<Integer>());
                }
                Set<Integer> nodesTriggeredFrom = referenceToEventsOnHold.get(nextNodeId);
                int currentNodeId = currentEvent.getNodeId();
                nodesTriggeredFrom.add(currentNodeId);
                if (idsOfNodesBeforeGateway.equals(nodesTriggeredFrom)) {
                    ScyllaEvent event = new GatewayEvent(model, source, currentSimulationTime, desmojObjects,
                            processInstance, nextNodeId);
                    events.add(event);
                    // clear list of fired incoming flows
                    referenceToEventsOnHold.remove(nextNodeId);
                }
            }
            else {
                ScyllaEvent event = new GatewayEvent(model, source, currentSimulationTime, desmojObjects,
                        processInstance, nextNodeId);
                events.add(event);
            }
        }
        else if (processModel.getEventTypes().containsKey(nextNodeId)) {
            EventType eType = processModel.getEventTypes().get(nextNodeId);
            if (eType == EventType.START) {
                throw new ScyllaRuntimeException(
                        "Start event " + nextNodeId + " must be at the beginning of the process.");
            }
            else if (eType == EventType.END) {
                ScyllaEvent event = new BPMNEndEvent(model, source, currentSimulationTime, desmojObjects,
                        processInstance, nextNodeId);
                events.add(event);
            }
            else {
                ScyllaEvent event = new BPMNIntermediateEvent(model, source, currentSimulationTime, desmojObjects,
                        processInstance, nextNodeId);
                events.add(event);
            }
        }
        else {
            throw new ScyllaRuntimeException("Next node " + nextNodeId + " not found or not supported.");
        }

        List<ScyllaEvent> eventsFromPlugins = EventCreationPluggable.runPlugins(currentEvent, desmojObjects,
                processInstance, nextNodeId);
        events.addAll(eventsFromPlugins);

        return events;
    }

    /**
     * Schedules given DesmoJ event.
     * 
     * @param model
     *            the simulation model which the event is attached to
     * @param processInstance
     *            the entity which the event is scheduled for
     * @param processInstanceId
     *            the identifier of the process instance (= entity)
     * @param event
     *            the DesmoJ event to be scheduled
     * @param timeSpan
     *            the time which the event is scheduled for, relatively to the simulation time
     * @throws SuspendExecution
     * @throws ScyllaRuntimeException
     */
    public static void scheduleEvent(ScyllaEvent event, TimeSpan timeSpan) throws ScyllaRuntimeException {
        boolean normalBehavior = EventSchedulingPluggable.runPlugins(event, timeSpan);
        if (normalBehavior) {
            ProcessInstance processInstance = event.getProcessInstance();
            event.schedule(processInstance, timeSpan);
        }
    }

    /**
     * Aborts the simulation of the given process instance.
     * 
     * @param model
     *            the simulation model
     * @param processModel
     *            the process model
     * @param processInstanceId
     *            the identifier of the process instance
     * @param nodeId
     *            identifier of node at which the simulation is aborted (for logging purposes)
     * @param showInTrace
     *            true if DesmoJ trace logging is enabled
     */
    public static void abort(Model model, ProcessInstance processInstance, int nodeId, boolean showInTrace) {
        ProcessModel processModel = processInstance.getProcessModel();
        int processInstanceId = processInstance.getId();

        String name = ScyllaEvent.buildEventName(processModel, processInstanceId, nodeId);
        name += "_Exception";
        ExternalEventStop abortEvent = new ExternalEventStop(model, name, showInTrace);
        abortEvent.schedule();
    }

    /**
     * Logs unknown BPMN element.
     * 
     * @param model
     *            the simulation model
     * @param processModel
     *            the process model
     * @param displayName
     *            the display name of the unsupported element
     * @param nodeId
     *            the identifier of the unsupported node
     */
    public static void sendElementNotSupportedTraceNote(SimulationModel model, ProcessModel processModel,
            String displayName, int nodeId) {
        StringBuffer sb = new StringBuffer();
        if (displayName != null && !displayName.isEmpty()) {
            sb.append("BPMNElementNotSupported: " + displayName);
        }
        else {
            String identifier = processModel.getIdentifiers().get(nodeId);
            sb.append("BPMNElementNotSupported: " + identifier);
        }
        model.sendTraceNote(sb.toString());
    }

    /**
     * Builds a identifier of a node which is unique across all levels of a BPMN process.
     * 
     * @param processModel
     *            the (sub-)process model
     * @param nodeId
     *            the identifier of the node
     * @return the identifier which is unique across all levels of the BPMN process
     */
    public static String getProcessScopeNodeId(ProcessModel processModel, Integer nodeId) {
        String processScopeNodeId = nodeId.toString();
        ProcessModel parent = processModel.getParent();
        while (parent != null) {
            Integer nodeIdInParent = processModel.getNodeIdInParent();
            processScopeNodeId = nodeIdInParent + "_" + processScopeNodeId;

            parent = parent.getParent();
        }
        return processScopeNodeId;
    }

    /**
     * Creates and schedules a DesmoJ event which represents a resource instance which returns from idle
     * for the beginning of the next timetable item for this resource instance.
     * 
     * @param model
     *            the simulation model
     * @param resourceObject
     *            the resource instance returning from idle
     * @param currentDateTime
     *            the current date time
     * @param currentlyInTimetableItem
     *            true if the resource instance is currently active
     */
    public static void scheduleNextResourceAvailableEvent(SimulationModel model, ResourceObject resourceObject,
            ZonedDateTime currentDateTime, boolean currentlyInTimetableItem) {
        boolean showInTrace = model.traceIsOn();
        TimeUnit timeUnit = DateTimeUtils.getReferenceTimeUnit();
        long currentTime = DateTimeUtils.getTimeInstant(currentDateTime).getTimeRounded(timeUnit);
        List<TimetableItem> timetable = resourceObject.getTimetable();
        if (timetable == null) {
            return;
        }
        int index = DateTimeUtils.getTimeTableIndexWithinOrNext(currentDateTime, timetable);
        if (currentlyInTimetableItem) {
            index++;
            if (index == timetable.size()) {
                index = 0;
            }
        }
        TimetableItem nextTimetableItem = timetable.get(index);
        DayOfWeek weekday = nextTimetableItem.getWeekdayFrom();
        LocalTime time = nextTimetableItem.getBeginTime();
        ZonedDateTime nextDateTime = DateTimeUtils.getNextZonedDateTime(currentDateTime, weekday, time);
        long durationToNextResourceAvailableEvent = DateTimeUtils.getDuration(currentDateTime, nextDateTime);

        if (model.getEndDateTime() != null) {
            long endTimeRelativeToGlobalStart = DateTimeUtils.getDuration(model.getStartDateTime(),
                    model.getEndDateTime());
            if (currentTime + durationToNextResourceAvailableEvent >= endTimeRelativeToGlobalStart) {
                return;
            }
        }
        ResourceAvailabilityEvent event = new ResourceAvailabilityEvent(model, resourceObject, showInTrace);
        event.schedule(new TimeSpan(durationToNextResourceAvailableEvent, timeUnit));
    }

    public static NumericalDist<?> getDistribution(Distribution dist, SimulationModel model, String name, 
    						Integer nodeId, boolean showInReport, boolean showInTrace) throws InstantiationException{
        if (dist instanceof BinomialDistribution) {
            BinomialDistribution binDist = (BinomialDistribution) dist;
            double probability = binDist.getProbability();
            int amount = binDist.getAmount();
            return new DiscreteDistBinomial(model, name, probability, amount, showInReport, showInTrace);
        }
        else if (dist instanceof ConstantDistribution) {
            ConstantDistribution conDist = (ConstantDistribution) dist;
            double constantValue = conDist.getConstantValue();
            return new DiscreteDistConstant<Number>(model, name, constantValue, showInReport, showInTrace);
        }
        else if (dist instanceof EmpiricalDistribution) {
            EmpiricalDistribution empDist = (EmpiricalDistribution) dist;
            Map<Double, Double> entries = empDist.getEntries();
            DiscreteDistEmpirical<Double> cde = new DiscreteDistEmpirical<Double>(model, name, showInReport,
                    showInTrace);
            for (Double value : entries.keySet()) {
                Double frequency = entries.get(value);
                cde.addEntry(value, frequency);
            }
            return cde;
        }
        else if (dist instanceof EmpiricalStringDistribution) {
        	EmpiricalStringDistribution empDist = (EmpiricalStringDistribution) dist;
            Map<Double, Double> entries = empDist.getEntries();
            DiscreteDistEmpirical<Double> cde = new DiscreteDistEmpirical<Double>(model, name, showInReport,
                    showInTrace);
            for (Double value : entries.keySet()) {
                Double frequency = entries.get(value);
                cde.addEntry(value, frequency);
            }
            return cde;
        }
        else if (dist instanceof ErlangDistribution) {
            ErlangDistribution erlDist = (ErlangDistribution) dist;
            double mean = erlDist.getMean();
            long order = erlDist.getOrder();
            return new ContDistErlang(model, name, order, mean, showInReport, showInTrace);
        }
        else if (dist instanceof ExponentialDistribution) {
            ExponentialDistribution expDist = (ExponentialDistribution) dist;
            double mean = expDist.getMean();
            return new ContDistExponential(model, name, mean, showInReport, showInTrace);
        }
        else if (dist instanceof TriangularDistribution) {
            TriangularDistribution triDist = (TriangularDistribution) dist;
            double lower = triDist.getLower();
            double upper = triDist.getUpper();
            double peak = triDist.getPeak();
            return new ContDistTriangular(model, name, lower, upper, peak, showInReport, showInTrace);
        }
        else if (dist instanceof NormalDistribution) {
            NormalDistribution norDist = (NormalDistribution) dist;
            double mean = norDist.getMean();
            double standardDeviation = norDist.getStandardDeviation();
            return new ContDistNormal(model, name, mean, standardDeviation, showInReport, showInTrace);
        }
        else if (dist instanceof PoissonDistribution) {
            PoissonDistribution poiDist = (PoissonDistribution) dist;
            double mean = poiDist.getMean();
            return new DiscreteDistPoisson(model, name, mean, showInReport, showInTrace);
        }
        else if (dist instanceof UniformDistribution) {
            UniformDistribution uniDist = (UniformDistribution) dist;
            double lower = uniDist.getLower();
            double upper = uniDist.getUpper();
            return new ContDistUniform(model, name, lower, upper, showInReport, showInTrace);
        }
        else {
            throw new InstantiationException("Distribution of node " + nodeId + " not supported.");
        }
    }
}
