package de.hpi.bpt.scylla.plugin.batch;

import java.util.List;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.plugin_type.simulation.event.TaskCancelEventPluggable;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.event.TaskCancelEvent;
import de.hpi.bpt.scylla.simulation.event.TaskTerminateEvent;

public class BatchTCPlugin extends TaskCancelEventPluggable {

    @Override
    public String getName() {
        return BatchPluginUtils.PLUGIN_NAME;
    }

    @Override
    public void eventRoutine(TaskCancelEvent event, ProcessInstance processInstance) throws ScyllaRuntimeException {
        BatchPluginUtils pluginInstance = BatchPluginUtils.getInstance();
        pluginInstance.logTaskEventForNonResponsiblePI(event, processInstance);
        BatchCluster cluster = pluginInstance.getCluster(processInstance);

        if (cluster != null) {

            List<TaskTerminateEvent> parentalEndEvents = cluster.getParentalEndEvents();
            for (TaskTerminateEvent pee : parentalEndEvents) {
                TaskCancelEvent cancelEvent = new TaskCancelEvent(pee.getModel(), pee.getSource(),
                        pee.getSimulationTimeOfSource(), pee.getSimulationComponents(), pee.getProcessInstance(),
                        pee.getNodeId());
                cancelEvent.schedule(pee.getProcessInstance());
            }

            parentalEndEvents.clear();


            ProcessInstance parentProcessInstance = processInstance.getParent();
            ProcessModel processModel = processInstance.getProcessModel();
            int parentNodeId = processModel.getNodeIdInParent();
            pluginInstance.setClusterToTerminated(parentProcessInstance, parentNodeId);
        }
    }

    
}
