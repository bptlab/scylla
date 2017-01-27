package de.hpi.bpt.scylla.model.configuration.distribution;

public class UniformDistribution implements Distribution {

    private double lower;
    private double upper;

    /**
     * Constructor.
     * 
     * @param lower
     *            minimum value of the triangular distribution
     * @param upper
     *            maximum value of the triangular distribution
     */
    public UniformDistribution(double lower, double upper) {
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
