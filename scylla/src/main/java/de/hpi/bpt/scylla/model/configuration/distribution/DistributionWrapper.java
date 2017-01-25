package de.hpi.bpt.scylla.model.configuration.distribution;

public abstract class DistributionWrapper {

    protected Distribution distribution;

	public Distribution getDistribution() {
		return distribution;
	}
	
	public void setDistribution(Distribution distribution) {
		this.distribution = distribution;
	}
}
