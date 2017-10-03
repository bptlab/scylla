package de.hpi.bpt.scylla.simulation.event;

import java.util.HashSet;
import java.util.Set;

import co.paralleluniverse.fibers.SuspendExecution;
import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.logger.ProcessNodeInfo;
import de.hpi.bpt.scylla.logger.ProcessNodeTransitionType;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.model.process.node.TaskType;
import de.hpi.bpt.scylla.plugin_type.simulation.event.TaskCancelEventPluggable;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.ProcessSimulationComponents;
import de.hpi.bpt.scylla.simulation.QueueManager;
import de.hpi.bpt.scylla.simulation.ResourceObject;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import de.hpi.bpt.scylla.simulation.utils.DateTimeUtils;
import de.hpi.bpt.scylla.simulation.utils.SimulationUtils;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

/**
 * DesmoJ event representing cancel transition of a BPMN task.
 * 
 * @author Tsun Yin Wong
 *
 */
public class TaskCancelEvent extends TaskEvent {

    public TaskCancelEvent(Model owner, String creator, TimeInstant simulationTimeOrigin,
            ProcessSimulationComponents desmojObjects, ProcessInstance processInstance, int nodeId) {
        super(owner, creator, simulationTimeOrigin, desmojObjects, processInstance, nodeId);
    }

    @Override
    public void eventRoutine(ProcessInstance processInstance) throws SuspendExecution {
        super.eventRoutine(processInstance);
        SimulationModel model = (SimulationModel) getModel();
        ProcessModel processModel = processInstance.getProcessModel();
        // int processInstanceId = processInstance.getId();

        /**
         * Cancel:
         * 
         * Weske 2nd ed, page 85 (fig 3.10)
         * 
         * slides POIS1 WS2015/16, deck 2 slide 9
         * 
         */

        try {
            ProcessModel subProcess = processModel.getSubProcesses().get(nodeId);
            TaskType type = processModel.getTasks().get(nodeId);

            String message = null;
            if (subProcess != null) {
                message = "Cancel Subprocess: " + displayName;
            }
            else if (type == TaskType.DEFAULT) {
                message = "Cancel Default Task: " + displayName;
            }
            else if (type == TaskType.SERVICE) {
                message = "Cancel Service Task: " + displayName;
            }
            else if (type == TaskType.SEND) {
                message = "Cancel Send Task: " + displayName;
            }
            else if (type == TaskType.RECEIVE) {
                message = "Cancel Receive Task: " + displayName;
            }
            else if (type == TaskType.USER) {
                message = "Cancel User Task: " + displayName;
            }
            else if (type == TaskType.MANUAL) {
                message = "Cancel Manual Task: " + displayName;
            }
            else if (type == TaskType.BUSINESS_RULE) {
                message = "Cancel Business Rule: " + displayName;
            }
            else if (type == TaskType.SCRIPT) {
                message = "Cancel Script Task: " + displayName;
            }
            else {
                // TODO write to log because element not supported
                SimulationUtils.abort(model, processInstance, nodeId, traceIsOn());
                return;
            }
            sendTraceNote(message);

            TaskCancelEventPluggable.runPlugins(this, processInstance);

            // 1: check queues if there are any events waiting, schedule them

            QueueManager.releaseResourcesAndScheduleQueuedEvents(model, this);

            // by default: cancel -> do not schedule any next event
            scheduleNextEvents();
        }
        catch (ScyllaRuntimeException e) {
            System.err.println(e.getMessage());
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
        ProcessNodeTransitionType transition = ProcessNodeTransitionType.CANCEL;

        SimulationModel model = (SimulationModel) getModel();
        ProcessModel processModel = processInstance.getProcessModel();
        String processScopeNodeId = SimulationUtils.getProcessScopeNodeId(processModel, nodeId);

        ProcessNodeInfo info = new ProcessNodeInfo(nodeId, processScopeNodeId, source, timestamp, taskName, resources,
                transition);
        model.addNodeInfo(processModel, processInstance, info);
    }
}
