package de.hpi.bpt.scylla.plugin_type.simulation.event;

import java.util.HashSet;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.Set;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.exception.ScyllaValidationException;
import de.hpi.bpt.scylla.plugin_type.IPluggable;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.event.ProcessInstanceGenerationEvent;

public abstract class ProcessInstanceGenerationEventPluggable
        implements
            IPluggable,
            IEventPluggable<ProcessInstanceGenerationEvent, ProcessInstance> {

    public static void runPlugins(ProcessInstanceGenerationEvent desmojEvent, ProcessInstance processInstance)
            throws ScyllaRuntimeException {

        // TODO: invoke serviceloader only once at start of Scylla
        Class<ProcessInstanceGenerationEventPluggable> clazz = ProcessInstanceGenerationEventPluggable.class;
        ServiceLoader<ProcessInstanceGenerationEventPluggable> serviceLoader = (ServiceLoader<ProcessInstanceGenerationEventPluggable>) ServiceLoader
                .load(clazz);
        Iterator<ProcessInstanceGenerationEventPluggable> plugins = serviceLoader.iterator();
        String eName = clazz.getName().substring(clazz.getName().lastIndexOf(".") + 1);
        Set<String> namesOfExtensions = new HashSet<String>();
        while (plugins.hasNext()) {
            ProcessInstanceGenerationEventPluggable plugin = plugins.next();
            String name = plugin.getName();
            if (namesOfExtensions.contains(name)) {
                try {
                    throw new ScyllaValidationException("Duplicate event routine extension name for parser type "
                            + eName + ": " + name + ". Event routine name must be unique per model type.");
                }
                catch (ScyllaValidationException e) {
                    e.printStackTrace();
                    // TODO REMOVE THIS
                }
            }
            namesOfExtensions.add(name);

            // run the routine
            plugin.eventRoutine(desmojEvent, processInstance);
        }
    }
}