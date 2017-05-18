package de.hpi.bpt.scylla.plugin_type.simulation;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.simulation.event.ScyllaEvent;
import desmoj.core.simulator.TimeSpan;

interface IEventSchedulingPluggable {

    boolean scheduleEvent(ScyllaEvent event, TimeSpan timeSpan) throws ScyllaRuntimeException;
}
