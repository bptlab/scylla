package de.hpi.bpt.scylla.model.configuration.distribution;

import java.util.concurrent.TimeUnit;

public class NormalDistribution extends Distribution {

    private double mean;
    private double standardDeviation;

    /**
     * Constructor.
     * 
     * @param timeUnit
     * @param mean
     *            mean of the normal distribution
     * @param standardDeviation
     *            standard deviation of the normal distribution
     */
    public NormalDistribution(TimeUnit timeUnit, double mean, double standardDeviation) {
        super(timeUnit);
        this.mean = mean;
        this.standardDeviation = standardDeviation;
    }

    public double getMean() {
        return mean;
    }

    public double getStandardDeviation() {
        return standardDeviation;
    }
}
