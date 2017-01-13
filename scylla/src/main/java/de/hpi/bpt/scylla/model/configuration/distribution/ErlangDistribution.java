package de.hpi.bpt.scylla.model.configuration.distribution;

import java.util.concurrent.TimeUnit;

public class ErlangDistribution extends Distribution {

    private long order;
    private double mean;

    /**
     * Constructor.
     * 
     * @param timeUnit
     * @param order
     *            order of the Erlang distribution
     * @param mean
     *            mean value of this Erlang distribution
     */
    public ErlangDistribution(TimeUnit timeUnit, long order, double mean) {
        super(timeUnit);
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
