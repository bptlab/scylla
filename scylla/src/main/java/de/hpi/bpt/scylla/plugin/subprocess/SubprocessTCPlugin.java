package de.hpi.bpt.scylla.plugin.subprocess;

import java.util.Map;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.plugin_type.simulation.event.TaskCancelEventPluggable;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import de.hpi.bpt.scylla.simulation.event.TaskCancelEvent;
import de.hpi.bpt.scylla.simulation.event.TaskTerminateEvent;
import desmoj.core.simulator.TimeSpan;

public class SubprocessTCPlugin extends TaskCancelEventPluggable {

    @Override
    public String getName() {
        return SubprocessPluginUtils.PLUGIN_NAME;
    }

    @Override
    public void eventRoutine(TaskCancelEvent desmojEvent, ProcessInstance processInstance)
            throws ScyllaRuntimeException {
        ProcessModel processModel = processInstance.getProcessModel();
        if (processModel.getParent() != null) {
            int nodeIdInParent = processModel.getNodeIdInParent();
            ProcessInstance parentProcessInstance = processInstance.getParent();
            String parentProcessInstanceName = parentProcessInstance.getName();
            SubprocessPluginUtils pluginInstance = SubprocessPluginUtils.getInstance();
            Map<Integer, TaskTerminateEvent> eventsOnHoldMap = pluginInstance.getEventsOnHold()
                    .get(parentProcessInstanceName);
            TaskTerminateEvent event = eventsOnHoldMap.get(nodeIdInParent);
            if (event != null) {
                SimulationModel model = (SimulationModel) event.getModel();
                String source = event.getSource();
                TaskCancelEvent cancelEvent = new TaskCancelEvent(model, source, event.getSimulationTimeOfSource(),
                        event.getDesmojObjects(), event.getProcessInstance(), event.getNodeId());
                cancelEvent.schedule(parentProcessInstance, new TimeSpan(0));
                eventsOnHoldMap.remove(nodeIdInParent);
            }
        }
    }

}
