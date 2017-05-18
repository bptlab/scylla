package de.hpi.bpt.scylla.plugin_type.simulation.event;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.exception.ScyllaValidationException;
import de.hpi.bpt.scylla.plugin_loader.PluginLoader;
import de.hpi.bpt.scylla.plugin_type.IPluggable;
import desmoj.core.simulator.ExternalEvent;

abstract class ExternalEventPluggable<T extends ExternalEvent> implements IPluggable, IExternalEventPluggable<T> {

    @SuppressWarnings("unchecked")
    protected static void runPlugins(Class<? extends ExternalEventPluggable<? extends ExternalEvent>> clazz,
            ExternalEvent desmojEvent) throws ScyllaRuntimeException {

        // TODO: invoke serviceloader only once at start of Scylla

        /*ServiceLoader<? extends ExternalEventPluggable<ExternalEvent>> serviceLoader = (ServiceLoader<? extends ExternalEventPluggable<ExternalEvent>>) ServiceLoader
                .load(clazz);
        Iterator<? extends ExternalEventPluggable<ExternalEvent>> plugins = serviceLoader.iterator();*/
    	Iterator<? extends ExternalEventPluggable<ExternalEvent>> plugins = (Iterator<? extends ExternalEventPluggable<ExternalEvent>>) PluginLoader.dGetPlugins(clazz);
        String eName = clazz.getName().substring(clazz.getName().lastIndexOf(".") + 1);
        Set<String> namesOfExtensions = new HashSet<String>();
        while (plugins.hasNext()) {
            ExternalEventPluggable<ExternalEvent> plugin = plugins.next();
            String name = plugin.getName();
            if (namesOfExtensions.contains(name)) {
                try {
                    // TODO message is not up to date
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
            plugin.eventRoutine(desmojEvent);
        }
    }

}
