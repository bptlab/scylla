package de.hpi.bpt.scylla.model.configuration.distribution;

import java.util.concurrent.TimeUnit;

public class ConstantDistribution extends Distribution {

    private double constantValue;

    /**
     * Constructor.
     * 
     * @param timeUnit
     * @param constantValue
     *            constant value
     */
    public ConstantDistribution(TimeUnit timeUnit, double constantValue) {
        super(timeUnit);
        this.constantValue = constantValue;
    }

    public double getConstantValue() {
        return constantValue;
    }
}
