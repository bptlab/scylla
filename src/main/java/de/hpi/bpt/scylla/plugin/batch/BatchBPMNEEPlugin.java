package de.hpi.bpt.scylla.plugin.batch;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.plugin_type.simulation.event.BPMNEndEventPluggable;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.event.BPMNEndEvent;

public class BatchBPMNEEPlugin extends BPMNEndEventPluggable {

    @Override
    public String getName() {
        return BatchPluginUtils.PLUGIN_NAME;
    }

    @Override
    public void eventRoutine(BPMNEndEvent event, ProcessInstance processInstance) throws ScyllaRuntimeException {

        BatchPluginUtils pluginInstance = BatchPluginUtils.getInstance();

        // Schedule parental end events
        BatchCluster cluster = pluginInstance.getCluster(processInstance);

        if (cluster != null) {
        	cluster.endEvent(event);
        }

    }

}
