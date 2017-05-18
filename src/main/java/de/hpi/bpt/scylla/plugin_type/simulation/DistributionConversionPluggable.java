package de.hpi.bpt.scylla.plugin_type.simulation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import de.hpi.bpt.scylla.exception.ScyllaValidationException;
import de.hpi.bpt.scylla.plugin_loader.PluginLoader;
import de.hpi.bpt.scylla.plugin_type.IPluggable;
import de.hpi.bpt.scylla.simulation.ProcessSimulationComponents;

public abstract class DistributionConversionPluggable implements IPluggable, IDistributionConversionPluggable {

    public static Map<String, Map<Integer, Object>> runPlugins(ProcessSimulationComponents pSimComponents) {
        Class<DistributionConversionPluggable> clazz = DistributionConversionPluggable.class;
        /*ServiceLoader<? extends DistributionConversionPluggable> serviceLoader = (ServiceLoader<? extends DistributionConversionPluggable>) ServiceLoader
                .load(clazz);
        Iterator<? extends DistributionConversionPluggable> plugins = serviceLoader.iterator();*/
        Iterator<? extends DistributionConversionPluggable> plugins = PluginLoader.dGetPlugins(clazz);
        String eName = clazz.getName().substring(clazz.getName().lastIndexOf(".") + 1);
        Set<String> namesOfExtensions = new HashSet<String>();
        Map<String, Map<Integer, Object>> distributionsExtensional = new HashMap<String, Map<Integer, Object>>();
        while (plugins.hasNext()) {
            DistributionConversionPluggable plugin = plugins.next();
            String name = plugin.getName();
            if (namesOfExtensions.contains(name)) {
                try {
                    throw new ScyllaValidationException(
                            "Duplicate distribution conversion extension name for parser type " + eName + ": " + name
                                    + ". Distribution conversion extension name must be unique per model type.");
                }
                catch (ScyllaValidationException e) {
                    e.printStackTrace();
                    // TODO REMOVE THIS
                }
            }
            namesOfExtensions.add(name);

            // run the routine
            distributionsExtensional.put(name, plugin.convertToDesmoJDistributions(pSimComponents));
        }
        return distributionsExtensional;
    }

}
