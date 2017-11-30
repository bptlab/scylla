package de.hpi.bpt.scylla.plugin_type.simulation;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.exception.ScyllaValidationException;
import de.hpi.bpt.scylla.plugin_loader.PluginLoader;
import de.hpi.bpt.scylla.plugin_type.IPluggable;
import de.hpi.bpt.scylla.simulation.event.ScyllaEvent;
import desmoj.core.simulator.TimeSpan;

public abstract class EventSchedulingPluggable implements IPluggable, IEventSchedulingPluggable {

    public static boolean runPlugins(ScyllaEvent event, TimeSpan timeSpan) throws ScyllaRuntimeException {
        Class<EventSchedulingPluggable> clazz = EventSchedulingPluggable.class;
        /*ServiceLoader<? extends EventSchedulingPluggable> serviceLoader = (ServiceLoader<? extends EventSchedulingPluggable>) ServiceLoader
                .load(clazz);
        Iterator<? extends EventSchedulingPluggable> plugins = serviceLoader.iterator();*/
        Iterator<? extends EventSchedulingPluggable> plugins = PluginLoader.dGetPlugins(clazz);
        String eName = clazz.getName().substring(clazz.getName().lastIndexOf(".") + 1);
        Set<String> namesOfExtensions = new HashSet<String>();
        boolean normalScheduleBehavior = true;
        while (plugins.hasNext()) {
            EventSchedulingPluggable plugin = plugins.next();
            String name = plugin.getName();
            if (namesOfExtensions.contains(name)) {
                try {
                    throw new ScyllaValidationException("Duplicate event scheduling extension name for parser type "
                            + eName + ": " + name + ". Event scheduling extension name must be unique per model type.");
                }
                catch (ScyllaValidationException e) {
                    e.printStackTrace();
                    // TODO REMOVE THIS
                }
            }
            namesOfExtensions.add(name);

            // run the routine
            if (!plugin.scheduleEvent(event, timeSpan) && normalScheduleBehavior) {
                normalScheduleBehavior = false;
            };
        }
        return normalScheduleBehavior;
    }

}
