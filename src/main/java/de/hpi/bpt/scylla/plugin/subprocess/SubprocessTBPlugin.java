package de.hpi.bpt.scylla.plugin.subprocess;

import java.util.HashMap;
import java.util.Map;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.logger.DebugLogger;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.model.process.graph.exception.MultipleStartNodesException;
import de.hpi.bpt.scylla.model.process.graph.exception.NoStartNodeException;
import de.hpi.bpt.scylla.model.process.graph.exception.NodeNotFoundException;
import de.hpi.bpt.scylla.plugin_type.simulation.event.TaskBeginEventPluggable;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.ProcessSimulationComponents;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import de.hpi.bpt.scylla.simulation.event.BPMNStartEvent;
import de.hpi.bpt.scylla.simulation.event.ScyllaEvent;
import de.hpi.bpt.scylla.simulation.event.TaskBeginEvent;
import de.hpi.bpt.scylla.simulation.event.TaskTerminateEvent;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;

public class SubprocessTBPlugin extends TaskBeginEventPluggable {

    @Override
    public String getName() {
        return SubprocessPluginUtils.PLUGIN_NAME;
    }

    @Override
    public void eventRoutine(TaskBeginEvent desmojEvent, ProcessInstance processInstance)
            throws ScyllaRuntimeException {
        ProcessModel processModel = processInstance.getProcessModel();
        int nodeId = desmojEvent.getNodeId();

        ProcessModel subProcess = processModel.getSubProcesses().get(nodeId);
        if (subProcess != null) {
            int indexOfTaskTerminateEvent = 0;

            desmojEvent.getTimeSpanToNextEventMap().remove(indexOfTaskTerminateEvent);

            TaskTerminateEvent event = (TaskTerminateEvent) desmojEvent.getNextEventMap()
                    .get(indexOfTaskTerminateEvent);
            String name = processInstance.getName();
            SubprocessPluginUtils pluginInstance = SubprocessPluginUtils.getInstance();
            Map<Integer, TaskTerminateEvent> eventsOnHoldMap = pluginInstance.getEventsOnHold().get(name);
            if (eventsOnHoldMap == null) {
                pluginInstance.getEventsOnHold().put(name, new HashMap<Integer, TaskTerminateEvent>());
            }
            pluginInstance.getEventsOnHold().get(name).put(nodeId, event);
            desmojEvent.getNextEventMap().remove(indexOfTaskTerminateEvent);

            String source = desmojEvent.getSource();
            ProcessSimulationComponents simulationComponents = desmojEvent.getSimulationComponents();
            SimulationModel model = (SimulationModel) desmojEvent.getModel();
            TimeInstant currentSimulationTime = model.presentTime();
            boolean showInTrace = model.traceIsOn();
            int processInstanceId = processInstance.getId();

            try {
                ProcessSimulationComponents desmojObjectsOfSubProcess = simulationComponents.getChildren().get(nodeId);
                Integer startNodeId = subProcess.getStartNode();
                ProcessInstance subProcessInstance = new ProcessInstance(model, subProcess, processInstanceId,
                        showInTrace);
                subProcessInstance.setParent(processInstance);
                ScyllaEvent subProcessEvent = new BPMNStartEvent(model, source, currentSimulationTime,
                        desmojObjectsOfSubProcess, subProcessInstance, startNodeId);

                TimeSpan timeSpan = new TimeSpan(0);

                int index = desmojEvent.getNewEventIndex();
                desmojEvent.getNextEventMap().put(index, subProcessEvent);
                desmojEvent.getTimeSpanToNextEventMap().put(index, timeSpan);
            }
            catch (NodeNotFoundException | MultipleStartNodesException | NoStartNodeException e) {
                DebugLogger.error(e.getMessage());
                DebugLogger.log("Start node of process model " + subProcess.getId() + " not found.");
                throw new ScyllaRuntimeException("Start node of process model " + subProcess.getId() + " not found.");
            }
        }

    }

}
