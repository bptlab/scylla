package de.hpi.bpt.scylla.plugin_type.simulation.event;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.event.BPMNEndEvent;

public abstract class BPMNEndEventPluggable extends ScyllaEventPluggable<BPMNEndEvent> {

    public static void runPlugins(BPMNEndEvent desmojEvent, ProcessInstance processInstance)
            throws ScyllaRuntimeException {

        runPlugins(BPMNEndEventPluggable.class, desmojEvent, processInstance);
    }
}