package de.hpi.bpt.scylla.simulation.event;

import java.util.HashSet;
import java.util.Set;

import co.paralleluniverse.fibers.SuspendExecution;
import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.logger.DebugLogger;
import de.hpi.bpt.scylla.logger.ProcessNodeInfo;
import de.hpi.bpt.scylla.logger.ProcessNodeTransitionType;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.model.process.node.TaskType;
import de.hpi.bpt.scylla.plugin_type.simulation.event.TaskEnableEventPluggable;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.ProcessSimulationComponents;
import de.hpi.bpt.scylla.simulation.QueueManager;
import de.hpi.bpt.scylla.simulation.ResourceObjectTuple;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import de.hpi.bpt.scylla.simulation.utils.DateTimeUtils;
import de.hpi.bpt.scylla.simulation.utils.SimulationUtils;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;

/**
 * DesmoJ event representing enable transition of a BPMN task.
 * 
 * @author Tsun Yin Wong
 *
 */
public class TaskEnableEvent extends TaskEvent {
	
	private TaskBeginEvent beginEvent;


	public TaskEnableEvent(Model owner, String source, TimeInstant simulationTimeOfSource,
            ProcessSimulationComponents desmojObjects, ProcessInstance processInstance, int nodeId) {
        super(owner, source, simulationTimeOfSource, desmojObjects, processInstance, nodeId);
    }

    @Override
    public void eventRoutine(ProcessInstance processInstance) throws SuspendExecution {
        super.eventRoutine(processInstance);
        SimulationModel model = (SimulationModel) getModel();
        source = getName();
        TimeInstant currentSimulationTime = model.presentTime();
        ProcessModel processModel = processInstance.getProcessModel();
        // int processInstanceId = processInstance.getId();

        ProcessModel subProcess = processModel.getSubProcesses().get(nodeId);
        TaskType type = processModel.getTasks().get(nodeId);

        String message = null;
        if (subProcess != null) {
            message = "Enable Subprocess: " + displayName;
        }
        else if (type == TaskType.DEFAULT) {
            message = "Enable Default Task: " + displayName;
        }
        else if (type == TaskType.SERVICE) {
            message = "Enable Service Task: " + displayName;
        }
        else if (type == TaskType.SEND) {
            message = "Enable Send Task: " + displayName;
        }
        else if (type == TaskType.RECEIVE) {
            message = "Enable Receive Task: " + displayName;
        }
        else if (type == TaskType.USER) {
            message = "Enable User Task: " + displayName;
        }
        else if (type == TaskType.MANUAL) {
            message = "Enable Manual Task: " + displayName;
        }
        else if (type == TaskType.BUSINESS_RULE) {
            message = "Enable Business Rule: " + displayName;
        }
        else if (type == TaskType.SCRIPT) {
            message = "Enable Script Task: " + displayName;
        }
        else {
            SimulationUtils.sendElementNotSupportedTraceNote(model, processModel, displayName, nodeId);
            SimulationUtils.abort(model, processInstance, nodeId, traceIsOn());
            return;
        }

        sendTraceNote(message);

        try {

            // poll available resources and if not available, put TaskBeginEvent on hold

            beginEvent = new TaskBeginEvent(model, source, currentSimulationTime, pSimComponents,
                    processInstance, nodeId);

            ResourceObjectTuple resources = QueueManager.getResourcesForEvent(model, beginEvent);

            if (resources == null) {
                QueueManager.addToEventQueues(model, beginEvent);
                sendTraceNote("Not enough resources available, task " + displayName + " is put in a queue.");
            }
            else {
                QueueManager.assignResourcesToEvent(model, beginEvent, resources);

                int index = getNewEventIndex();
                nextEventMap.put(index, beginEvent);
                timeSpanToNextEventMap.put(index, new TimeSpan(0));
            }

            TaskEnableEventPluggable.runPlugins(this, processInstance);

            scheduleNextEvents();
        }
        catch (ScyllaRuntimeException e) {
            DebugLogger.error(e.getMessage());
            DebugLogger.log("Simulation aborted.");
        }
    }

    @Override
    protected void addToLog(ProcessInstance processInstance) {
        long timestamp = Math.round(getModel().presentTime().getTimeRounded(DateTimeUtils.getReferenceTimeUnit()));
        String taskName = displayName;
        Set<String> resources = new HashSet<String>();

        ProcessNodeTransitionType transition = ProcessNodeTransitionType.ENABLE;

        SimulationModel model = (SimulationModel) getModel();
        ProcessModel processModel = processInstance.getProcessModel();
        String processScopeNodeId = SimulationUtils.getProcessScopeNodeId(processModel, nodeId);

        ProcessNodeInfo info = new ProcessNodeInfo(nodeId, processScopeNodeId, getName(), timestamp, taskName, resources,
                transition);
        model.addNodeInfo(processModel, processInstance, info);

    }
    
    public TaskBeginEvent getBeginEvent() {
		return beginEvent;
	}

}
