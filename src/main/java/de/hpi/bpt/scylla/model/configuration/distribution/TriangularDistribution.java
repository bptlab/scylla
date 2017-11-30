package de.hpi.bpt.scylla.model.configuration.distribution;

public class TriangularDistribution implements Distribution {

    private double lower;
    private double upper;
    private double peak;

    /**
     * Constructor.
     * 
     * @param lower
     *            minimum value of the triangular distribution
     * @param upper
     *            maximum value of the triangular distribution
     * @param peak
     *            most likely value of the triangular distribution
     */
    public TriangularDistribution(double lower, double upper, double peak) {
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
