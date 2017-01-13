package de.hpi.bpt.scylla.model.configuration.distribution;

import java.util.concurrent.TimeUnit;

public class ExponentialDistribution extends Distribution {

    private double mean;

    /**
     * Constructor.
     * 
     * @param timeUnit
     * @param mean
     *            mean value of the negative-exponential distribution
     */
    public ExponentialDistribution(TimeUnit timeUnit, double mean) {
        super(timeUnit);
        this.mean = mean;
    }

    public double getMean() {
        return mean;
    }
}
