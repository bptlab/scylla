package de.hpi.bpt.scylla.plugin_type.simulation.event;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.event.TaskTerminateEvent;

public abstract class TaskTerminateEventPluggable extends ScyllaEventPluggable<TaskTerminateEvent> {

    public static void runPlugins(TaskTerminateEvent desmojEvent, ProcessInstance processInstance)
            throws ScyllaRuntimeException {

        runPlugins(TaskTerminateEventPluggable.class, desmojEvent, processInstance);
    }
}