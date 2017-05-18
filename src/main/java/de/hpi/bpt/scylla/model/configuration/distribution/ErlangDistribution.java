package de.hpi.bpt.scylla.model.configuration.distribution;

public class ErlangDistribution implements Distribution {

    private long order;
    private double mean;

    /**
     * Constructor.
     * 
     * @param order
     *            order of the Erlang distribution
     * @param mean
     *            mean value of this Erlang distribution
     */
    public ErlangDistribution(long order, double mean) {
        this.order = order;
        this.mean = mean;
    }

    public long getOrder() {
        return order;
    }

    public double getMean() {
        return mean;
    }
}
