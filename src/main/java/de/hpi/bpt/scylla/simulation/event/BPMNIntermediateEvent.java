package de.hpi.bpt.scylla.simulation.event;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import co.paralleluniverse.fibers.SuspendExecution;
import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.exception.ScyllaValidationException;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.model.process.graph.exception.NodeNotFoundException;
import de.hpi.bpt.scylla.model.process.node.EventDefinitionType;
import de.hpi.bpt.scylla.model.process.node.EventType;
import de.hpi.bpt.scylla.plugin_type.simulation.event.BPMNIntermediateEventPluggable;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.ProcessSimulationComponents;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import de.hpi.bpt.scylla.simulation.utils.SimulationUtils;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;

/**
 * DesmoJ event representing BPMN intermediate events.
 * 
 * @author Tsun Yin Wong
 *
 */
public class BPMNIntermediateEvent extends BPMNEvent {

    public BPMNIntermediateEvent(Model owner, String source, TimeInstant simulationTimeOfSource,
            ProcessSimulationComponents desmojObjects, ProcessInstance processInstance, int nodeId) {
        super(owner, source, simulationTimeOfSource, desmojObjects, processInstance, nodeId);
    }

    @Override
    public void eventRoutine(ProcessInstance processInstance) throws SuspendExecution {
        super.eventRoutine(processInstance);
        SimulationModel model = (SimulationModel) getModel();
        ProcessModel processModel = processInstance.getProcessModel();

        // TODO: INTERMEDIATE_THROW events are consumed by parent process or by other process

        try {

            EventType type = processModel.getEventTypes().get(nodeId);

            // Long duration = null;
            // TimeUnit timeUnit = null;

            Map<EventDefinitionType, Map<String, String>> definitions = processModel.getEventDefinitions().get(nodeId);
            Set<String> messages = new HashSet<String>();

            for (EventDefinitionType definition : definitions.keySet()) {
                // TODO what about implicit throw events?
                if (definition == EventDefinitionType.CANCEL) {
                    // TODO: cancel event only in transaction sub-process
                    boolean cancelActivity = processModel.getCancelActivities().get(nodeId); // isInterrupting?
                    if (type == EventType.BOUNDARY && cancelActivity) {
                        String message = "Boundary Cancel Event: " + displayName;
                        messages.add(message);
                    }
                    else {
                        SimulationUtils.sendElementNotSupportedTraceNote(model, processModel, displayName, nodeId);
                        SimulationUtils.abort(model, processInstance, nodeId, traceIsOn());
                        return;
                    }
                }
                else if (definition == EventDefinitionType.COMPENSATION) {
                    boolean cancelActivity = processModel.getCancelActivities().get(nodeId); // isInterrupting?
                    if (type == EventType.BOUNDARY && cancelActivity) {
                        String message = "Boundary Compensation Event: " + displayName;
                        messages.add(message);
                    }
                    else {
                        SimulationUtils.sendElementNotSupportedTraceNote(model, processModel, displayName, nodeId);
                        SimulationUtils.abort(model, processInstance, nodeId, traceIsOn());
                        return;
                    }
                }
                else if (definition == EventDefinitionType.CONDITIONAL) {
                    if (type == EventType.BOUNDARY) {
                        boolean cancelActivity = processModel.getCancelActivities().get(nodeId); // isInterrupting?
                        if (cancelActivity) {
                            String message = "Boundary Conditional Event (Interrupting): " + displayName;
                            messages.add(message);
                        }
                        else {
                            String message = "Boundary Conditional Event (Non-Interrupting): " + displayName;
                            messages.add(message);
                        }
                    }
                    else if (type == EventType.INTERMEDIATE_CATCH) {
                        String message = "Intermediate Conditional Event (Catching): " + displayName;
                        messages.add(message);

                    }
                    else {
                        SimulationUtils.sendElementNotSupportedTraceNote(model, processModel, displayName, nodeId);
                        SimulationUtils.abort(model, processInstance, nodeId, traceIsOn());
                        return;
                    }
                }
                else if (definition == EventDefinitionType.ERROR) {
                    boolean cancelActivity = processModel.getCancelActivities().get(nodeId); // isInterrupting?
                    if (type == EventType.BOUNDARY && cancelActivity) {
                        String message = "Boundary Error Event: " + displayName;
                        messages.add(message);
                    }
                    else {
                        SimulationUtils.sendElementNotSupportedTraceNote(model, processModel, displayName, nodeId);
                        SimulationUtils.abort(model, processInstance, nodeId, traceIsOn());
                        return;
                    }
                }
                else if (definition == EventDefinitionType.ESCALATION) {
                    if (type == EventType.BOUNDARY) {
                        boolean cancelActivity = processModel.getCancelActivities().get(nodeId); // isInterrupting?
                        if (cancelActivity) {
                            String message = "Boundary Escalation Event (Interrupting): " + displayName;
                            messages.add(message);

                        }
                        else {
                            String message = "Boundary Escalation Event (Non-Interrupting): " + displayName;
                            messages.add(message);
                        }
                    }
                    else if (type == EventType.INTERMEDIATE_THROW) {
                        String message = "Intermediate Escalation Event (Throwing): " + displayName;
                        messages.add(message);

                    }
                    else {
                        SimulationUtils.sendElementNotSupportedTraceNote(model, processModel, displayName, nodeId);
                        SimulationUtils.abort(model, processInstance, nodeId, traceIsOn());
                        return;
                    }
                }
                else if (definition == EventDefinitionType.LINK) {
                    if (type == EventType.INTERMEDIATE_CATCH) {
                        String message = "Intermediate Link Event (Catching): " + displayName;
                        messages.add(message);

                    }
                    else if (type == EventType.INTERMEDIATE_THROW) {
                        String message = "Intermediate Link Event (Throwing): " + displayName;
                        messages.add(message);

                    }
                    else {
                        SimulationUtils.sendElementNotSupportedTraceNote(model, processModel, displayName, nodeId);
                        SimulationUtils.abort(model, processInstance, nodeId, traceIsOn());
                        return;
                    }
                }
                else if (definition == EventDefinitionType.MESSAGE) {
                    if (type == EventType.BOUNDARY) {
                        boolean cancelActivity = processModel.getCancelActivities().get(nodeId); // isInterrupting?
                        if (cancelActivity) {
                            String message = "Boundary Message Event (Interrupting): " + displayName;
                            messages.add(message);

                        }
                        else {
                            String message = "Boundary Message Event (Non-Interrupting): " + displayName;
                            messages.add(message);
                        }
                    }
                    else if (type == EventType.INTERMEDIATE_CATCH) {
                        String message = "Intermediate Message Event (Catching): " + displayName;
                        messages.add(message);

                    }
                    else if (type == EventType.INTERMEDIATE_THROW) {
                        String message = "Intermediate Message Event (Throwing): " + displayName;
                        messages.add(message);

                    }
                    else {
                        SimulationUtils.sendElementNotSupportedTraceNote(model, processModel, displayName, nodeId);
                        SimulationUtils.abort(model, processInstance, nodeId, traceIsOn());
                        return;
                    }
                }
                else if (definition == EventDefinitionType.SIGNAL) {
                    if (type == EventType.BOUNDARY) {
                        boolean cancelActivity = processModel.getCancelActivities().get(nodeId); // isInterrupting?
                        if (cancelActivity) {
                            String message = "Boundary Signal Event (Interrupting): " + displayName;
                            messages.add(message);
                        }
                        else {
                            String message = "Boundary Signal Event (Non-Interrupting): " + displayName;
                            messages.add(message);
                        }
                    }
                    else if (type == EventType.INTERMEDIATE_CATCH) {
                        String message = "Intermediate Signal Event (Catching): " + displayName;
                        messages.add(message);

                    }
                    else if (type == EventType.INTERMEDIATE_THROW) {
                        String message = "Intermediate Signal Event (Throwing): " + displayName;
                        messages.add(message);

                    }
                    else {
                        SimulationUtils.sendElementNotSupportedTraceNote(model, processModel, displayName, nodeId);
                        SimulationUtils.abort(model, processInstance, nodeId, traceIsOn());
                        return;
                    }
                }
                else if (definition == EventDefinitionType.TIMER) {

                    if (type == EventType.BOUNDARY) {
                        boolean cancelActivity = processModel.getCancelActivities().get(nodeId); // isInterrupting?
                        if (cancelActivity) {
                            String message = "Boundary Timer Event (Interrupting): " + displayName;
                            messages.add(message);

                        }
                        else {
                            String message = "Boundary Timer Event (Non-Interrupting): " + displayName;
                            messages.add(message);
                        }
                    }
                    else if (type == EventType.INTERMEDIATE_CATCH) {
                        String message = "Intermediate Timer Event (Catching): " + displayName;
                        messages.add(message);
                    }
                    else {
                        SimulationUtils.sendElementNotSupportedTraceNote(model, processModel, displayName, nodeId);
                        SimulationUtils.abort(model, processInstance, nodeId, traceIsOn());
                        return;
                    }
                }
                else { // None Event
                    if (messages.isEmpty()) {
                        if (type == EventType.INTERMEDIATE_THROW) {

                            String message = "Intermediate None Event (Throwing): " + displayName;
                            messages.add(message);
                        }
                        else {
                            SimulationUtils.sendElementNotSupportedTraceNote(model, processModel, displayName, nodeId);
                            SimulationUtils.abort(model, processInstance, nodeId, traceIsOn());
                        }
                    }
                }
            }

            for (String message : messages) {
                sendTraceNote(message);
            }

            // get next node(s)
            Set<Integer> idsOfNextNodes = processModel.getIdsOfNextNodes(nodeId);
            // BPMN intermediate event must not have more than successor
            if (idsOfNextNodes.size() != 1) {
                throw new ScyllaValidationException(
                        "Event " + nodeId + " does not have 1 successor, but " + idsOfNextNodes.size() + ".");
            }
            Integer nextNodeId = idsOfNextNodes.iterator().next();

            // schedule event for next node
            List<ScyllaEvent> events = SimulationUtils.createEventsForNextNode(this, pSimComponents, processInstance,
                    nextNodeId);
            // next event occurs immediately after start event
            TimeSpan timeSpan = new TimeSpan(0);

            for (ScyllaEvent event : events) {
                int index = getNewEventIndex();
                nextEventMap.put(index, event);
                timeSpanToNextEventMap.put(index, timeSpan);
            }

            // unless current one is BPMN timer event with timerDuration
            BPMNIntermediateEventPluggable.runPlugins(this, processInstance);

            scheduleNextEvents();
        }
        catch (NodeNotFoundException | ScyllaValidationException | ScyllaRuntimeException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            SimulationUtils.abort(model, processInstance, nodeId, traceIsOn());
            return;
        }
    }
}
