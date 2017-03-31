package de.hpi.bpt.scylla.plugin_type.simulation.event;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import desmoj.core.simulator.Entity;
import desmoj.core.simulator.Event;

interface IEventPluggable<T extends Event<U>, U extends Entity> {

    void eventRoutine(T desmojEvent, U desmojEntity) throws ScyllaRuntimeException;
}
