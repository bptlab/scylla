package de.hpi.bpt.scylla.plugin.dataobject;

public class DataObjectPluginUtils {
	
    public static final String PLUGIN_NAME = "dataobject";
    private static DataObjectPluginUtils singleton;
    
    static DataObjectPluginUtils getInstance() {
        if (singleton == null) {
            singleton = new DataObjectPluginUtils();
        }
        return singleton;
    }
}
