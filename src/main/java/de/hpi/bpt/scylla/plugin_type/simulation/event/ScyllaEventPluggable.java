package de.hpi.bpt.scylla.plugin_type.simulation.event;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.exception.ScyllaValidationException;
import de.hpi.bpt.scylla.plugin_loader.PluginLoader;
import de.hpi.bpt.scylla.plugin_type.IPluggable;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.event.ScyllaEvent;

abstract class ScyllaEventPluggable<T extends ScyllaEvent> implements IPluggable, IEventPluggable<T, ProcessInstance> {

    @SuppressWarnings("unchecked")
    protected static void runPlugins(Class<? extends ScyllaEventPluggable<? extends ScyllaEvent>> clazz,
            ScyllaEvent desmojEvent, ProcessInstance processInstance) throws ScyllaRuntimeException {

        // TODO: invoke serviceloader only once at start of Scylla

        /*ServiceLoader<? extends ScyllaEventPluggable<ScyllaEvent>> serviceLoader = (ServiceLoader<? extends ScyllaEventPluggable<ScyllaEvent>>) ServiceLoader
                .load(clazz);
        Iterator<? extends ScyllaEventPluggable<ScyllaEvent>> plugins = serviceLoader.iterator();*/
    	Iterator<? extends ScyllaEventPluggable<ScyllaEvent>> plugins = (Iterator<? extends ScyllaEventPluggable<ScyllaEvent>>) PluginLoader.dGetPlugins(clazz);
        String eName = clazz.getName().substring(clazz.getName().lastIndexOf(".") + 1);
        Set<String> namesOfExtensions = new HashSet<String>();
        while (plugins.hasNext()) {
            ScyllaEventPluggable<ScyllaEvent> plugin = plugins.next();
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
