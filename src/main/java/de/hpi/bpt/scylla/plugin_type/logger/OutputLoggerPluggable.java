package de.hpi.bpt.scylla.plugin_type.logger;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import de.hpi.bpt.scylla.exception.ScyllaValidationException;
import de.hpi.bpt.scylla.plugin_loader.PluginLoader;
import de.hpi.bpt.scylla.plugin_type.IPluggable;
import de.hpi.bpt.scylla.simulation.SimulationModel;

public abstract class OutputLoggerPluggable implements IPluggable, IOutputLogger {

    public static void runPlugins(SimulationModel model, String outputPathWithoutExtension) throws IOException {
        Class<OutputLoggerPluggable> clazz = OutputLoggerPluggable.class;
        /*ServiceLoader<? extends OutputLoggerPluggable> serviceLoader = (ServiceLoader<? extends OutputLoggerPluggable>) ServiceLoader
                .load(clazz);
        Iterator<? extends OutputLoggerPluggable> plugins = serviceLoader.iterator();*/
        Iterator<? extends OutputLoggerPluggable> plugins = PluginLoader.dGetPlugins(clazz);
        Set<String> namesOfExtensions = new HashSet<String>();
        while (plugins.hasNext()) {
            OutputLoggerPluggable plugin = plugins.next();
            String name = plugin.getName();
            if (namesOfExtensions.contains(name)) {
                try {
                    throw new ScyllaValidationException("Duplicate output logger plugin name: " + name
                            + ". Output logger plugin name must be unique.");
                }
                catch (ScyllaValidationException e) {
                    e.printStackTrace();
                    // TODO REMOVE THIS
                }
            }
            namesOfExtensions.add(name);

            // run
            plugin.writeToLog(model, outputPathWithoutExtension);
        }
    }
}
