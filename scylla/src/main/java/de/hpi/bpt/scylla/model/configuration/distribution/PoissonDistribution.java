package de.hpi.bpt.scylla.model.configuration.distribution;

public class PoissonDistribution implements Distribution {

    private double mean;

    /**
     * Constructor.
     * 
     * @param mean
     *            mean value of the negative-exponential distribution
     */
    public PoissonDistribution(double mean) {
        this.mean = mean;
    }

    public double getMean() {
        return mean;
    }
}
