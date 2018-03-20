package de.hpi.bpt.scylla.plugin.batch;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.plugin_type.simulation.event.TaskTerminateEventPluggable;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import de.hpi.bpt.scylla.simulation.event.TaskBeginEvent;
import de.hpi.bpt.scylla.simulation.event.TaskTerminateEvent;

public class BatchTTPlugin extends TaskTerminateEventPluggable {

    @Override
    public String getName() {
        return BatchPluginUtils.PLUGIN_NAME;
    }

    @Override
    public void eventRoutine(TaskTerminateEvent event, ProcessInstance processInstance) throws ScyllaRuntimeException {
        BatchPluginUtils pluginInstance = BatchPluginUtils.getInstance();
        pluginInstance.logTaskEventForNonResponsiblePI(event, processInstance);
        // schedule other tasks of batch here
    }

}
