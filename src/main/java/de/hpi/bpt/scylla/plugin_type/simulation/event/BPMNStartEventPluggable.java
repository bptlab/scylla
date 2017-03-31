package de.hpi.bpt.scylla.plugin_type.simulation.event;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.event.BPMNStartEvent;

public abstract class BPMNStartEventPluggable extends ScyllaEventPluggable<BPMNStartEvent> {

    public static void runPlugins(BPMNStartEvent desmojEvent, ProcessInstance processInstance)
            throws ScyllaRuntimeException {

        runPlugins(BPMNStartEventPluggable.class, desmojEvent, processInstance);
    }
}