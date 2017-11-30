package de.hpi.bpt.scylla.plugin_type.simulation.event;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.event.GatewayEvent;

public abstract class GatewayEventPluggable extends ScyllaEventPluggable<GatewayEvent> {

    public static void runPlugins(GatewayEvent desmojEvent, ProcessInstance processInstance)
            throws ScyllaRuntimeException {

        runPlugins(GatewayEventPluggable.class, desmojEvent, processInstance);
    }
}