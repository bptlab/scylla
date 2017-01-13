package de.hpi.bpt.scylla.plugin_type.simulation.event;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.event.TaskBeginEvent;

public abstract class TaskBeginEventPluggable extends ScyllaEventPluggable<TaskBeginEvent> {

    public static void runPlugins(TaskBeginEvent desmojEvent, ProcessInstance processInstance)
            throws ScyllaRuntimeException {

        runPlugins(TaskBeginEventPluggable.class, desmojEvent, processInstance);
    }
}