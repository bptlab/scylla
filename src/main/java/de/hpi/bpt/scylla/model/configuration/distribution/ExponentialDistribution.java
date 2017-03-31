package de.hpi.bpt.scylla.model.configuration.distribution;

public class ExponentialDistribution implements Distribution {

    private double mean;

    /**
     * Constructor.
     * 
     * @param mean
     *            mean value of the negative-exponential distribution
     */
    public ExponentialDistribution(double mean) {
        this.mean = mean;
    }

    public double getMean() {
        return mean;
    }
}
