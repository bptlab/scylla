package de.hpi.bpt.scylla.model.configuration.distribution;

import java.util.concurrent.TimeUnit;

public class BinomialDistribution extends Distribution {

    private double probability;
    private int amount;

    /**
     * Constructor.
     * 
     * @param timeUnit
     * @param probability
     *            probability of success in each separate Bernoulli experiment
     * @param amount
     *            amount of separate Bernoulli experiments that lead to the result
     */
    public BinomialDistribution(TimeUnit timeUnit, double probability, int amount) {
        super(timeUnit);
        this.probability = probability;
        this.amount = amount;
    }

    public double getProbability() {
        return probability;
    }

    public int getAmount() {
        return amount;
    }
}
