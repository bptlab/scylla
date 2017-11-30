package de.hpi.bpt.scylla.plugin_type.simulation.event;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.simulation.event.ProcessSimulationStopEvent;

public abstract class ProcessSimulationStopEventPluggable extends ExternalEventPluggable<ProcessSimulationStopEvent> {

    public static void runPlugins(ProcessSimulationStopEvent desmojEvent) throws ScyllaRuntimeException {

        runPlugins(ProcessSimulationStopEventPluggable.class, desmojEvent);
    }
}