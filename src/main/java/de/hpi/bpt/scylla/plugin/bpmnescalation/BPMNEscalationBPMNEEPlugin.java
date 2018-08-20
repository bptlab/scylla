package de.hpi.bpt.scylla.plugin.bpmnescalation;

import java.util.List;
import java.util.Map;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.exception.ScyllaValidationException;
import de.hpi.bpt.scylla.logger.DebugLogger;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.model.process.graph.exception.NodeNotFoundException;
import de.hpi.bpt.scylla.model.process.node.EventDefinitionType;
import de.hpi.bpt.scylla.plugin_type.simulation.event.BPMNEndEventPluggable;
import de.hpi.bpt.scylla.simulation.ProcessSimulationComponents;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import de.hpi.bpt.scylla.simulation.event.BPMNEndEvent;
import de.hpi.bpt.scylla.simulation.event.ScyllaEvent;
import de.hpi.bpt.scylla.simulation.utils.SimulationUtils;
import desmoj.core.simulator.TimeSpan;

public class BPMNEscalationBPMNEEPlugin extends BPMNEndEventPluggable {

    @Override
    public String getName() {
        return "bpmnescalationevent";
    }

    @Override
    public void eventRoutine(BPMNEndEvent desmojEvent, ProcessInstance processInstance) throws ScyllaRuntimeException {
        SimulationModel model = (SimulationModel) desmojEvent.getModel();
        ProcessModel processModel = processInstance.getProcessModel();
        int nodeId = desmojEvent.getNodeId();
        Map<EventDefinitionType, Map<String, String>> definitions = processModel.getEventDefinitions().get(nodeId);
        ProcessSimulationComponents simulationComponents = desmojEvent.getSimulationComponents();
        boolean showInTrace = model.traceIsOn();
        try {
            for (EventDefinitionType definition : definitions.keySet()) {
                if (definition == EventDefinitionType.ESCALATION) {
                    if (processModel.getParent() != null) {

                        Map<String, String> eventAttributes = processModel.getEventDefinitions().get(nodeId)
                                .get(definition);
                        String escalationRef = eventAttributes.get("escalationRef");

                        // Map<String, Map<String, String>> escalations =
                        // model.getCommonProcessElements().getEscalations();
                        // Map<String, String> escalation = escalations.get("escalationRef");

                        ProcessSimulationComponents parentDesmojObjects = simulationComponents.getParent();
                        ProcessModel parentModel = processModel.getParent();

                        int nodeIdInParent = processModel.getNodeIdInParent();

                        Integer nextNodeId = null;
                        // find boundary event of parentModel which has the same escalationRef
                        List<Integer> referencesToBoundaryEvents = parentModel.getReferencesToBoundaryEvents()
                                .get(nodeIdInParent);
                        for (int nId : referencesToBoundaryEvents) {
                            Map<EventDefinitionType, Map<String, String>> boundaryEventDefinitions = parentModel
                                    .getEventDefinitions().get(nId);
                            Map<String, String> boundaryEscalationEventDefinition = boundaryEventDefinitions
                                    .get(EventDefinitionType.ESCALATION);
                            if (boundaryEscalationEventDefinition != null) {
                                if (escalationRef.equals(boundaryEscalationEventDefinition.get("escalationRef"))) {
                                    nextNodeId = nId;
                                    break;
                                }
                            }
                        }

                        if (nextNodeId == null) {
                            DebugLogger.error("Could not find referenced escalation " + escalationRef + ".");
                            SimulationUtils.abort(model, processInstance, nodeId, showInTrace);
                            return;
                        }

                        ProcessInstance parentProcessInstance = processInstance.getParent();

                        List<ScyllaEvent> events = SimulationUtils.createEventsForNextNode(desmojEvent,
                                parentDesmojObjects, parentProcessInstance, nextNodeId);
                        TimeSpan timeSpan = new TimeSpan(0);

                        /**
                         * first event in the map is the node that comes after the subprocess when normal behavior
                         * applies, so remove it;
                         */
                        int indexOfTaskTerminateEvent = 0;
                        desmojEvent.getNextEventMap().remove(indexOfTaskTerminateEvent);
                        desmojEvent.getTimeSpanToNextEventMap().remove(indexOfTaskTerminateEvent);

                        for (ScyllaEvent event : events) {
                            int index = desmojEvent.getNewEventIndex();
                            desmojEvent.getNextEventMap().put(index, event);
                            desmojEvent.getTimeSpanToNextEventMap().put(index, timeSpan);
                        }

                        processInstance.cancel();
                    }
                }
            }
        }
        catch (NodeNotFoundException | ScyllaValidationException e) {
            DebugLogger.error(e.getMessage());
            e.printStackTrace();
            SimulationUtils.abort(model, processInstance, nodeId, showInTrace);
        }
    }

}
