package de.hpi.bpt.scylla.model.configuration.distribution;

import java.util.concurrent.TimeUnit;

public class TriangularDistribution extends Distribution {

    private double lower;
    private double upper;
    private double peak;

    /**
     * Constructor.
     * 
     * @param timeUnit
     * @param lower
     *            minimum value of the triangular distribution
     * @param upper
     *            maximum value of the triangular distribution
     * @param peak
     *            most likely value of the triangular distribution
     */
    public TriangularDistribution(TimeUnit timeUnit, double lower, double upper, double peak) {
        super(timeUnit);
        this.lower = lower;
        this.upper = upper;
        this.peak = peak;
    }

    public double getLower() {
        return lower;
    }

    public double getUpper() {
        return upper;
    }

    public double getPeak() {
        return peak;
    }
}
