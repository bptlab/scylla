package de.hpi.bpt.scylla.simulation.event;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import co.paralleluniverse.fibers.SuspendExecution;
import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.model.process.node.EventDefinitionType;
import de.hpi.bpt.scylla.plugin_type.simulation.event.BPMNEndEventPluggable;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.ProcessSimulationComponents;
import de.hpi.bpt.scylla.simulation.QueueManager;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import de.hpi.bpt.scylla.simulation.utils.SimulationUtils;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

/**
 * DesmoJ event representing BPMN end events.
 * 
 * @author Tsun Yin Wong
 *
 */
public class BPMNEndEvent extends BPMNEvent {

    public BPMNEndEvent(Model owner, String source, TimeInstant simulationTimeOfSource,
            ProcessSimulationComponents desmojObjects, ProcessInstance processInstance, int nodeId) {
        super(owner, source, simulationTimeOfSource, desmojObjects, processInstance, nodeId);

    }

    @Override
    public void eventRoutine(ProcessInstance processInstance) throws SuspendExecution {
        super.eventRoutine(processInstance);
        SimulationModel model = (SimulationModel) getModel();
        ProcessModel processModel = processInstance.getProcessModel();

        try {
            Map<EventDefinitionType, Map<String, String>> definitions = processModel.getEventDefinitions().get(nodeId);

            Set<String> messages = new HashSet<String>();

            for (EventDefinitionType definition : definitions.keySet()) {

                if (definition == EventDefinitionType.MESSAGE) {
                    String message = "Message End Event: " + displayName;
                    messages.add(message);
                }
                else if (definition == EventDefinitionType.ESCALATION) {
                    String message = "Escalation End Event: " + displayName;
                    messages.add(message);
                }
                else if (definition == EventDefinitionType.ERROR) {
                    String message = "Error End Event: " + displayName;
                    messages.add(message);
                }
                else if (definition == EventDefinitionType.CANCEL) {
                    String message = "Cancel End Event: " + displayName;
                    messages.add(message);

                    // TODO for transaction subprocesses only
                }
                else if (definition == EventDefinitionType.COMPENSATION) {
                    String message = "Compensation End Event: " + displayName;
                    messages.add(message);

                    // TODO run compensation
                }
                else if (definition == EventDefinitionType.SIGNAL) {
                    String message = "Signal End Event: " + displayName;
                    messages.add(message);
                }
                else if (definition == EventDefinitionType.TERMINATE) {
                    String message = "Terminate End Event: " + displayName;
                    messages.add(message);

                    processInstance.cancel(); // unschedule events of this process instance
                }
            }

            if (messages.isEmpty()) {
                String message = "None End Event: " + displayName;
                messages.add(message);
            }

            for (String message : messages) {
                sendTraceNote(message);
            }

            // unless current one is BPMN timer event with timerDuration
            BPMNEndEventPluggable.runPlugins(this, processInstance);

            scheduleNextEvents();

            if (!QueueManager.isAnyEventScheduledOrQueued(model)) {
                model.getExperiment().stop();
            }
        }
        catch (ScyllaRuntimeException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            SimulationUtils.abort(model, processInstance, nodeId, traceIsOn());
        }

    }

}
