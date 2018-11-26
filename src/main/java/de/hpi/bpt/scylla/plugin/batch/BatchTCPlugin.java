package de.hpi.bpt.scylla.plugin.batch;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.plugin_type.simulation.event.TaskCancelEventPluggable;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.event.TaskCancelEvent;

public class BatchTCPlugin extends TaskCancelEventPluggable {

    @Override
    public String getName() {
        return BatchPluginUtils.PLUGIN_NAME;
    }

    @Override
    public void eventRoutine(TaskCancelEvent event, ProcessInstance processInstance) throws ScyllaRuntimeException {
        BatchPluginUtils pluginInstance = BatchPluginUtils.getInstance();
        BatchCluster cluster = pluginInstance.getCluster(processInstance);
        if(cluster == null)cluster = pluginInstance.getCluster(event);
        if (cluster != null) {
        	cluster.taskCancelEvent(event);
        }
    }

    
}
