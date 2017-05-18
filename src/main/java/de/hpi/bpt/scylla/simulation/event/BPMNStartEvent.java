package de.hpi.bpt.scylla.simulation.event;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import co.paralleluniverse.fibers.SuspendExecution;
import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.exception.ScyllaValidationException;
import de.hpi.bpt.scylla.logger.DebugLogger;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.model.process.graph.exception.NodeNotFoundException;
import de.hpi.bpt.scylla.model.process.node.EventDefinitionType;
import de.hpi.bpt.scylla.plugin_type.simulation.event.BPMNStartEventPluggable;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.ProcessSimulationComponents;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import de.hpi.bpt.scylla.simulation.utils.SimulationUtils;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;

/**
 * DesmoJ event representing BPMN start events.
 * 
 * @author Tsun Yin Wong
 *
 */
public class BPMNStartEvent extends BPMNEvent {

    public BPMNStartEvent(Model owner, String source, TimeInstant simulationTimeOfSource,
            ProcessSimulationComponents desmojObjects, ProcessInstance processInstance, int nodeId) {
        super(owner, source, simulationTimeOfSource, desmojObjects, processInstance, nodeId);
    }

    @Override
    public void eventRoutine(ProcessInstance processInstance) throws SuspendExecution {
        super.eventRoutine(processInstance);
        SimulationModel model = (SimulationModel) getModel();
        ProcessModel processModel = processInstance.getProcessModel();

        Map<EventDefinitionType, Map<String, String>> definitions = processModel.getEventDefinitions().get(nodeId);

        Set<String> messages = new HashSet<String>();

        for (EventDefinitionType definition : definitions.keySet()) {

            if (definition == EventDefinitionType.MESSAGE) {
                String message = "Message Start Event: " + displayName;
                messages.add(message);
            }
            else if (definition == EventDefinitionType.TIMER) {
                String message = "Timer Start Event: " + displayName;
                messages.add(message);
            }
            else if (definition == EventDefinitionType.CONDITIONAL) {
                String message = "Conditional Start Event: " + displayName;
                messages.add(message);
            }
            else if (definition == EventDefinitionType.SIGNAL) {
                String message = "Signal Start Event: " + displayName;
                messages.add(message);
            }
        }

        if (messages.isEmpty()) {
            String message = "None Start Event: " + displayName;
            messages.add(message);
        }

        for (String message : messages) {
            sendTraceNote(message);
        }

        try {
            // get next node(s)
            Set<Integer> idsOfNextNodes = processModel.getIdsOfNextNodes(nodeId);
            // BPMN start event must not have more than successor
            if (idsOfNextNodes.size() != 1) {
                throw new ScyllaValidationException(
                        "Start event " + nodeId + " does not have 1 successor, but " + idsOfNextNodes.size() + ".");
            }
            Integer nextNodeId = idsOfNextNodes.iterator().next();

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
            BPMNStartEventPluggable.runPlugins(this, processInstance);

            scheduleNextEvents();
        }
        catch (NodeNotFoundException | ScyllaValidationException | ScyllaRuntimeException e) {
            DebugLogger.error(e.getMessage());
            e.printStackTrace();
            SimulationUtils.abort(model, processInstance, nodeId, traceIsOn());
            return;
        }
    }

}
