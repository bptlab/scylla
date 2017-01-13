package de.hpi.bpt.scylla.model.configuration.distribution;

import java.util.concurrent.TimeUnit;

public class UniformDistribution extends Distribution {

    private double lower;
    private double upper;

    /**
     * Constructor.
     * 
     * @param timeUnit
     * @param lower
     *            minimum value of the triangular distribution
     * @param upper
     *            maximum value of the triangular distribution
     */
    public UniformDistribution(TimeUnit timeUnit, double lower, double upper) {
        super(timeUnit);
        this.lower = lower;
        this.upper = upper;
    }

    public double getLower() {
        return lower;
    }

    public double getUpper() {
        return upper;
    }
}
