package de.hpi.bpt.scylla.plugin.batch;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.plugin_type.simulation.event.BPMNIntermediateEventPluggable;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.event.BPMNIntermediateEvent;

public class BatchBPMNIEPlugin extends BPMNIntermediateEventPluggable {

    @Override
    public String getName() {
        return BatchPluginUtils.PLUGIN_NAME;
    }

    @Override
    public void eventRoutine(BPMNIntermediateEvent event, ProcessInstance processInstance)
            throws ScyllaRuntimeException {

        BatchPluginUtils pluginInstance = BatchPluginUtils.getInstance();
        pluginInstance.logBPMNEventForNonResponsiblePI(event, processInstance);

        //pluginInstance.scheduleNextEventInBatchProcess(event, processInstance);
    }

}
