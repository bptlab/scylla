package de.hpi.bpt.scylla.plugin.subprocess;

import java.util.List;
import java.util.Set;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.exception.ScyllaValidationException;
import de.hpi.bpt.scylla.logger.DebugLogger;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.model.process.graph.exception.NodeNotFoundException;
import de.hpi.bpt.scylla.plugin_type.simulation.event.BPMNEndEventPluggable;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.ProcessSimulationComponents;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import de.hpi.bpt.scylla.simulation.event.BPMNEndEvent;
import de.hpi.bpt.scylla.simulation.event.ScyllaEvent;
import de.hpi.bpt.scylla.simulation.event.TaskCancelEvent;
import de.hpi.bpt.scylla.simulation.event.TaskTerminateEvent;
import de.hpi.bpt.scylla.simulation.utils.SimulationUtils;
import desmoj.core.simulator.EventAbstract;
import desmoj.core.simulator.TimeSpan;

public class SubprocessBPMNEEPlugin extends BPMNEndEventPluggable {

    @Override
    public String getName() {
        return SubprocessPluginUtils.PLUGIN_NAME;
    }

    @Override
    public void eventRoutine(BPMNEndEvent desmojEvent, ProcessInstance processInstance) throws ScyllaRuntimeException {
        ProcessModel processModel = processInstance.getProcessModel();
        if (processModel.getParent() != null && processInstanceIsCompleted(processInstance)) { // TODO check if
                                                                                               // getScheduledEvents().isEmpty()
                                                                                               // works
            try {
                ProcessSimulationComponents desmojObjects = desmojEvent.getDesmojObjects();
                ProcessSimulationComponents parentDesmojObjects = desmojObjects.getParent();
                ProcessModel parentModel = processModel.getParent();
                int nodeIdInParent = processModel.getNodeIdInParent();
                ProcessInstance parentProcessInstance = processInstance.getParent();

                // behavior when sub-process sends events:
                // none -> back to normal flow
                // message -> "send it"
                // error -> to parent
                // escalation -> to parent
                // cancel -> nonono!
                // compensation -> ... not now
                // signal -> ... not now
                // terminate -> terminate sub-process (kill all events of sub-process instance (MI sub-process
                // not affected))
                // ...
                // timer -> special treatment

                Set<Integer> idsOfNextNodes = parentModel.getIdsOfNextNodes(nodeIdInParent);
                // normal flow: must not have more than one successor
                if (idsOfNextNodes.size() != 1) {
                    int nodeId = desmojEvent.getNodeId();
                    throw new ScyllaValidationException(
                            "Subprocess " + nodeId + " does not have 1 successor, but " + idsOfNextNodes.size() + ".");
                }
                Integer nextNodeId = idsOfNextNodes.iterator().next();

                // TODO let the parent create the next node, so remove the lines below
                List<ScyllaEvent> events = SimulationUtils.createEventsForNextNode(desmojEvent, parentDesmojObjects,
                        parentProcessInstance, nextNodeId);
                // next event occurs immediately after start event
                TimeSpan timeSpan = new TimeSpan(0);

                String parentProcessInstanceName = parentProcessInstance.getName();
                SubprocessPluginUtils pluginInstance = SubprocessPluginUtils.getInstance();
                TaskTerminateEvent eventOfParent = pluginInstance.getEventsOnHold().get(parentProcessInstanceName)
                        .get(nodeIdInParent);

                if (eventOfParent != null) {
                    events.add(eventOfParent);
                    pluginInstance.getEventsOnHold().get(parentProcessInstanceName).remove(nodeIdInParent);
                    pluginInstance.getNameOfEventsThatWereOnHold().add(eventOfParent.getName());
                }

                for (ScyllaEvent event : events) {
                    int index = desmojEvent.getNewEventIndex();
                    desmojEvent.getNextEventMap().put(index, event);
                    desmojEvent.getTimeSpanToNextEventMap().put(index, timeSpan);
                }
            }
            catch (NodeNotFoundException | ScyllaValidationException | ScyllaRuntimeException e) {
                SimulationModel model = (SimulationModel) desmojEvent.getModel();
                int nodeId = desmojEvent.getNodeId();
                boolean showInTrace = model.traceIsOn();

                DebugLogger.error(e.getMessage());
                e.printStackTrace();
                SimulationUtils.abort(model, processInstance, nodeId, showInTrace);
            }
        }
    }

    private boolean processInstanceIsCompleted(ProcessInstance processInstance) {
        for (EventAbstract event : processInstance.getScheduledEvents()) {
            if (!(event instanceof TaskCancelEvent)) {
                return false;
            }
        }
        return true;
    }
}
