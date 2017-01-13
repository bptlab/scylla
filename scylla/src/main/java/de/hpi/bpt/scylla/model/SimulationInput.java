package de.hpi.bpt.scylla.model;

import java.util.HashMap;
import java.util.Map;

import de.hpi.bpt.scylla.plugin_type.PluginUtils;

/**
 * Abstract class for all types of simulation input.
 * 
 * @author Tsun Yin Wong
 *
 */
public abstract class SimulationInput {

    protected String id;
    protected Map<String, Object> extensionAttributes;

    protected SimulationInput(String id) {
        this.id = id;
        this.extensionAttributes = new HashMap<String, Object>();
    }

    public String getId() {
        return id;
    }

    /**
     * In plugins, use {@link #getExtensionValue(String, String)}.
     * 
     * @return map of extension attributes
     */
    public Map<String, Object> getExtensionAttributes() {
        return extensionAttributes;
    }

    /**
     * Returns extension value which has been stored by a plug-in.
     * 
     * @param pluginName
     *            name of plugin
     * @param attributeName
     *            name of attribute
     * @return extension value (to be cast by plug-in)
     */
    public Object getExtensionValue(String pluginName, String attributeName) {
        return extensionAttributes.get(pluginName + PluginUtils.getPluginAttributeNameDelimiter() + attributeName);
    }
}
