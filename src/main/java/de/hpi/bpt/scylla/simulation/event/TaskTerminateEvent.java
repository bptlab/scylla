package de.hpi.bpt.scylla.simulation.event;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import co.paralleluniverse.fibers.SuspendExecution;
import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.exception.ScyllaValidationException;
import de.hpi.bpt.scylla.logger.DebugLogger;
import de.hpi.bpt.scylla.logger.ProcessNodeInfo;
import de.hpi.bpt.scylla.logger.ProcessNodeTransitionType;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.model.process.graph.exception.NodeNotFoundException;
import de.hpi.bpt.scylla.model.process.node.TaskType;
import de.hpi.bpt.scylla.plugin_type.simulation.event.TaskTerminateEventPluggable;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.ProcessSimulationComponents;
import de.hpi.bpt.scylla.simulation.QueueManager;
import de.hpi.bpt.scylla.simulation.ResourceObject;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import de.hpi.bpt.scylla.simulation.utils.DateTimeUtils;
import de.hpi.bpt.scylla.simulation.utils.SimulationUtils;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;

/**
 * DesmoJ event representing terminate transition of a BPMN task.
 * 
 * @author Tsun Yin Wong
 *
 */
public class TaskTerminateEvent extends TaskEvent {

    public TaskTerminateEvent(Model owner, String source, TimeInstant simulationTimeOfSource,
            ProcessSimulationComponents desmojObjects, ProcessInstance processInstance, int nodeId) {
        super(owner, source, simulationTimeOfSource, desmojObjects, processInstance, nodeId);
    }

    @Override
    public void eventRoutine(ProcessInstance processInstance) throws SuspendExecution {
        super.eventRoutine(processInstance);
        SimulationModel model = (SimulationModel) getModel();
        ProcessModel processModel = processInstance.getProcessModel();
        // int processInstanceId = processInstance.getId();

        try {
            ProcessModel subProcess = processModel.getSubProcesses().get(nodeId);
            TaskType type = processModel.getTasks().get(nodeId);

            String message = null;
            if (subProcess != null) {
                message = "End of Subprocess: " + displayName;
            }
            else if (type == TaskType.DEFAULT) {
                message = "End of Default Task: " + displayName;
            }
            else if (type == TaskType.SERVICE) {
                message = "End of Service Task: " + displayName;
            }
            else if (type == TaskType.SEND) {
                message = "End of Send Task: " + displayName;
            }
            else if (type == TaskType.RECEIVE) {
                message = "End of Receive Task: " + displayName;
            }
            else if (type == TaskType.USER) {
                message = "End of User Task: " + displayName;
            }
            else if (type == TaskType.MANUAL) {
                message = "End of Manual Task: " + displayName;
            }
            else if (type == TaskType.BUSINESS_RULE) {
                message = "End of Business Rule: " + displayName;
            }
            else if (type == TaskType.SCRIPT) {
                message = "End of Script Task: " + displayName;
            }
            else {
                // TODO write to log because element not supported
                SimulationUtils.abort(model, processInstance, nodeId, traceIsOn());
                return;
            }
            sendTraceNote(message);

            // 1: check queues if there are any events waiting, schedule them first
            // 2: schedule event for next node

            QueueManager.releaseResourcesAndScheduleQueuedEvents(model, this);

            // get next node(s)
            Set<Integer> idsOfNextNodes = processModel.getIdsOfNextNodes(nodeId);
            // start event must not have more than successor
            if (idsOfNextNodes.size() != 1) {
                throw new ScyllaValidationException(
                        "Task " + nodeId + " does not have 1 successor, but " + idsOfNextNodes.size() + ".");
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
            TaskTerminateEventPluggable.runPlugins(this, processInstance);

            scheduleNextEvents();
        }
        catch (NodeNotFoundException | ScyllaValidationException | ScyllaRuntimeException e) {
            DebugLogger.error(e.getMessage());
            e.printStackTrace();
            SimulationUtils.abort(model, processInstance, nodeId, traceIsOn());
            return;
        }
    }

    @Override
    protected void addToLog(ProcessInstance processInstance) {
        long timestamp = Math.round(getModel().presentTime().getTimeRounded(DateTimeUtils.getReferenceTimeUnit()));
        String taskName = displayName;
        Set<String> resources = new HashSet<String>();
        Set<ResourceObject> resourceObjects = processInstance.getAssignedResources().get(source).getResourceObjects();

        for (ResourceObject res : resourceObjects) {
            String resourceName = res.getResourceType() + "_" + res.getId();
            resources.add(resourceName);
        }
        ProcessNodeTransitionType transition = ProcessNodeTransitionType.TERMINATE;

        SimulationModel model = (SimulationModel) getModel();
        ProcessModel processModel = processInstance.getProcessModel();
        String processScopeNodeId = SimulationUtils.getProcessScopeNodeId(processModel, nodeId);

        ProcessNodeInfo info = new ProcessNodeInfo(processScopeNodeId, source, timestamp, taskName, resources,
                transition);
        model.addNodeInfo(processModel, processInstance, info);
    }

}
