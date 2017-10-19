package de.hpi.bpt.scylla.simulation.event;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import co.paralleluniverse.fibers.SuspendExecution;
import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.logger.ProcessNodeInfo;
import de.hpi.bpt.scylla.logger.ProcessNodeTransitionType;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.model.process.node.TaskType;
import de.hpi.bpt.scylla.plugin_type.simulation.event.TaskBeginEventPluggable;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.ProcessSimulationComponents;
import de.hpi.bpt.scylla.simulation.ResourceObject;
import de.hpi.bpt.scylla.simulation.ResourceObjectTuple;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import de.hpi.bpt.scylla.simulation.utils.DateTimeUtils;
import de.hpi.bpt.scylla.simulation.utils.SimulationUtils;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;

/**
 * DesmoJ event representing begin transition of a BPMN task.
 * 
 * @author Tsun Yin Wong
 *
 */
public class TaskBeginEvent extends TaskEvent {

    public TaskBeginEvent(Model owner, String source, TimeInstant simulationTimeOfSource,
            ProcessSimulationComponents desmojObjects, ProcessInstance processInstance, int nodeId) {
        super(owner, source, simulationTimeOfSource, desmojObjects, processInstance, nodeId);
    }

    @Override
    public void eventRoutine(ProcessInstance processInstance) throws SuspendExecution {
        super.eventRoutine(processInstance);

        SimulationModel model = (SimulationModel) getModel();
        TimeInstant currentSimulationTime = model.presentTime();
        ProcessModel processModel = processInstance.getProcessModel();

        ProcessModel subProcess = processModel.getSubProcesses().get(nodeId);
        TaskType type = processModel.getTasks().get(nodeId);

        String message = null;
        if (subProcess != null) {
            message = "Begin Subprocess: " + displayName;
        }
        else if (type == TaskType.DEFAULT) {
            message = "Begin Default Task: " + displayName;
        }
        else if (type == TaskType.SERVICE) {
            message = "Begin Service Task: " + displayName;
        }
        else if (type == TaskType.SEND) {
            message = "Begin Send Task: " + displayName;
        }
        else if (type == TaskType.RECEIVE) {
            message = "Begin Receive Task: " + displayName;
        }
        else if (type == TaskType.USER) {
            message = "Begin User Task: " + displayName;
        }
        else if (type == TaskType.MANUAL) {
            message = "Begin Manual Task: " + displayName;
        }
        else if (type == TaskType.BUSINESS_RULE) {
            message = "Begin Business Rule: " + displayName;
        }
        else if (type == TaskType.SCRIPT) {
            message = "Begin Script Task: " + displayName;
        }
        else {
            SimulationUtils.sendElementNotSupportedTraceNote(model, processModel, displayName, nodeId);
            SimulationUtils.abort(model, processInstance, nodeId, traceIsOn());
            return;
        }

        sendTraceNote(message);

        try {

            double duration = pSimComponents.getDistributionSample(nodeId);
            TimeUnit unit = pSimComponents.getDistributionTimeUnit(nodeId);

            ScyllaEvent event = new TaskTerminateEvent(model, source, currentSimulationTime, pSimComponents,
                    processInstance, nodeId);
            TimeSpan timeSpan = new TimeSpan(duration, unit);

            ResourceObjectTuple tuple = processInstance.getAssignedResources().get(source);
            TimeInstant nextEventTime = DateTimeUtils.getTaskTerminationTime(timeSpan, currentSimulationTime, tuple,
                    event);

            timeSpan = new TimeSpan(nextEventTime.getTimeAsDouble() - currentSimulationTime.getTimeAsDouble());

            int index = getNewEventIndex();
            nextEventMap.put(index, event);
            timeSpanToNextEventMap.put(index, timeSpan);

            TaskBeginEventPluggable.runPlugins(this, processInstance);

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
        ProcessNodeTransitionType transition = ProcessNodeTransitionType.BEGIN;

        SimulationModel model = (SimulationModel) getModel();
        ProcessModel processModel = processInstance.getProcessModel();
        String processScopeNodeId = SimulationUtils.getProcessScopeNodeId(processModel, nodeId);

        ProcessNodeInfo info = new ProcessNodeInfo(nodeId, processScopeNodeId, source, timestamp, taskName, resources,
                transition);
        model.addNodeInfo(processModel, processInstance, info);
    }
}
