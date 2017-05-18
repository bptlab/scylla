package de.hpi.bpt.scylla.plugin_type.parser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jdom2.Element;

import de.hpi.bpt.scylla.SimulationManager;
import de.hpi.bpt.scylla.exception.ScyllaValidationException;
import de.hpi.bpt.scylla.model.SimulationInput;
import de.hpi.bpt.scylla.plugin_loader.PluginLoader;
import de.hpi.bpt.scylla.plugin_type.IPluggable;
import de.hpi.bpt.scylla.plugin_type.PluginUtils;

abstract class ParserPluggable<T extends SimulationInput> implements IPluggable, IDOMParserPluggable<T> {

    protected SimulationManager simulationEnvironment;

    @SuppressWarnings("unchecked")
    protected static void run(SimulationManager simEnvironment,
            Class<? extends ParserPluggable<? extends SimulationInput>> clazz, SimulationInput smo, Element sim)
                    throws ScyllaValidationException {

        Set<String> namesOfExtensions = new HashSet<String>();

        /*Iterator<? extends ParserPluggable<SimulationInput>> parserExtensions = (Iterator<? extends ParserPluggable<SimulationInput>>) ServiceLoader
                .load(clazz).iterator();*/
        Iterator<? extends ParserPluggable<SimulationInput>> parserExtensions = (Iterator<? extends ParserPluggable<SimulationInput>>)PluginLoader.dGetPlugins(clazz);
        String smoName = clazz.getName().substring(clazz.getName().lastIndexOf(".") + 1);
        Map<String, Object> smoExtensionAttributes = new HashMap<String, Object>();
        while (parserExtensions.hasNext()) {
            ParserPluggable<SimulationInput> parserExtension = parserExtensions.next();
            String name = parserExtension.getName();
            if (namesOfExtensions.contains(name)) {
                throw new ScyllaValidationException("Duplicate parser extension name for parser type " + smoName + ": "
                        + name + ". Parser extension name must be unique per model type.");
            }
            namesOfExtensions.add(name);

            parserExtension.setSimulationEnvironment(simEnvironment);
            Map<String, Object> extensionAttributes = parserExtension.parse(smo, sim);

            for (String attributeName : extensionAttributes.keySet()) {
                Object value = extensionAttributes.get(attributeName);
                smoExtensionAttributes.put(name + PluginUtils.getPluginAttributeNameDelimiter() + attributeName, value);
            }
        }
        smo.getExtensionAttributes().putAll(smoExtensionAttributes);
    }

    public void setSimulationEnvironment(SimulationManager simEnvironment) {
        this.simulationEnvironment = simEnvironment;
    }

}