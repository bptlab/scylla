package de.hpi.bpt.scylla.plugin_type.simulation.event;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.simulation.event.ResourceAvailabilityEvent;

public abstract class ResourceAvailabilityEventPluggable extends ExternalEventPluggable<ResourceAvailabilityEvent> {

    public static void runPlugins(ResourceAvailabilityEvent desmojEvent) throws ScyllaRuntimeException {

        runPlugins(ResourceAvailabilityEventPluggable.class, desmojEvent);
    }
}