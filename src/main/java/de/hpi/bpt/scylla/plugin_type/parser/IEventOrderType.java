package de.hpi.bpt.scylla.plugin_type.parser;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.simulation.event.ScyllaEvent;

interface IEventOrderType {

    int compare(String resourceId, ScyllaEvent e1, ScyllaEvent e2) throws ScyllaRuntimeException;
}
