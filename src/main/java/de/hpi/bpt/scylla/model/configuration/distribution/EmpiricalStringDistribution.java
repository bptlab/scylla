package de.hpi.bpt.scylla.model.configuration.distribution;

import java.util.HashMap;
import java.util.Map;

public class EmpiricalStringDistribution implements Distribution {

    private Map<Double, String> names = new HashMap<Double, String>();
    protected Map<Double, Double> entries = new HashMap<Double, Double>();
    
    public void addEntry(String value, double frequency) {
    	int key = entries.size();
        entries.put(Double.valueOf(key), frequency);
        names.put(Double.valueOf(key), value);
    }
    
    public Map<Double, String> getNames() {
    	return names;
    }
    
    public Map<Double, Double> getEntries() {
        return entries;
    }
}
