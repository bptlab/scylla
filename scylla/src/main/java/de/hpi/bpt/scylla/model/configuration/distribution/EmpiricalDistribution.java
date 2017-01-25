package de.hpi.bpt.scylla.model.configuration.distribution;

import java.util.HashMap;
import java.util.Map;

public class EmpiricalDistribution implements Distribution {

    protected Map<Double, Double> entries = new HashMap<Double, Double>();

    public void addEntry(double value, double frequency) {
        entries.put(value, frequency);
    }

    public Map<Double, Double> getEntries() {
        return entries;
    }
}
