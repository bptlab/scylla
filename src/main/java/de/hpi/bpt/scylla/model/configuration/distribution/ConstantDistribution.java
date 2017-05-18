package de.hpi.bpt.scylla.model.configuration.distribution;

public class ConstantDistribution implements Distribution {


    private double constantValue;

    /**
     * Constructor.
     * 
     * @param constantValue
     *            constant value
     */
    public ConstantDistribution(double constantValue) {
        this.constantValue = constantValue;
    }

    public double getConstantValue() {
        return constantValue;
    }
}
