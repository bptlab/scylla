package de.hpi.bpt.scylla.plugin_type.simulation.event;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;

interface IExternalEventPluggable<T> {

    void eventRoutine(T desmojEvent) throws ScyllaRuntimeException;

}
