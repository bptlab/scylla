package de.hpi.bpt.scylla.plugin_type.simulation.event;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.event.TaskEnableEvent;

public abstract class TaskEnableEventPluggable extends ScyllaEventPluggable<TaskEnableEvent> {

    public static void runPlugins(TaskEnableEvent desmojEvent, ProcessInstance processInstance)
            throws ScyllaRuntimeException {

        runPlugins(TaskEnableEventPluggable.class, desmojEvent, processInstance);
    }
}