package de.hpi.bpt.scylla.plugin.batch;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.plugin_type.simulation.event.BPMNStartEventPluggable;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.event.BPMNStartEvent;

public class BatchBPMNSEPlugin extends BPMNStartEventPluggable {

    @Override
    public String getName() {
        return BatchPluginUtils.PLUGIN_NAME;
    }

    @Override
    public void eventRoutine(BPMNStartEvent event, ProcessInstance processInstance) throws ScyllaRuntimeException {

        BatchPluginUtils pluginInstance = BatchPluginUtils.getInstance();
        pluginInstance.logBPMNEventForNonResponsiblePI(event, processInstance);
        
        BatchCluster cluster = pluginInstance.getCluster(processInstance);
        if (cluster != null && cluster.hasExecutionType(BatchClusterExecutionType.SEQUENTIAL_TASKBASED)) {
            cluster.scheduleNextEventInBatchProcess(event);
        }
    }

}
