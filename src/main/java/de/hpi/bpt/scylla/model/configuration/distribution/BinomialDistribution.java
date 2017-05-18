package de.hpi.bpt.scylla.model.configuration.distribution;

public class BinomialDistribution implements Distribution {

    private double probability;
    private int amount;

    /**
     * Constructor.
     * 
     * @param probability
     *            probability of success in each separate Bernoulli experiment
     * @param amount
     *            amount of separate Bernoulli experiments that lead to the result
     */
    public BinomialDistribution(double probability, int amount) {
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
