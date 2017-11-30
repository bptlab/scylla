package de.hpi.bpt.scylla.model.configuration.distribution;

public class NormalDistribution implements Distribution {


    private double mean;
    private double standardDeviation;

    /**
     * Constructor.
     * 
     * @param mean
     *            mean of the normal distribution
     * @param standardDeviation
     *            standard deviation of the normal distribution
     */
    public NormalDistribution(double mean, double standardDeviation) {
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
