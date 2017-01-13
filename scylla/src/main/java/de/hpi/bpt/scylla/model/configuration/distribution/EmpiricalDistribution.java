package de.hpi.bpt.scylla.model.configuration.distribution;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class EmpiricalDistribution extends Distribution {

    private Map<Double, Double> entries = new HashMap<Double, Double>();

    /**
     * Constructor.
     * 
     * @param timeUnit
     */
    public EmpiricalDistribution(TimeUnit timeUnit) {
        super(timeUnit);
    }

    public void addEntry(double value, double frequency) {
        entries.put(value, frequency);
    }

    public Map<Double, Double> getEntries() {
        return entries;
    }
}
