package de.hpi.bpt.scylla.plugin_type.simulation.event;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.event.BPMNIntermediateEvent;

public abstract class BPMNIntermediateEventPluggable extends ScyllaEventPluggable<BPMNIntermediateEvent> {

    public static void runPlugins(BPMNIntermediateEvent desmojEvent, ProcessInstance processInstance)
            throws ScyllaRuntimeException {

        runPlugins(BPMNIntermediateEventPluggable.class, desmojEvent, processInstance);
    }
}