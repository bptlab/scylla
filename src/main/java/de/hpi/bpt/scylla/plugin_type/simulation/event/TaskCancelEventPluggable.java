package de.hpi.bpt.scylla.plugin_type.simulation.event;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.event.TaskCancelEvent;

public abstract class TaskCancelEventPluggable extends ScyllaEventPluggable<TaskCancelEvent> {

    public static void runPlugins(TaskCancelEvent desmojEvent, ProcessInstance processInstance)
            throws ScyllaRuntimeException {

        runPlugins(TaskCancelEventPluggable.class, desmojEvent, processInstance);
    }
}