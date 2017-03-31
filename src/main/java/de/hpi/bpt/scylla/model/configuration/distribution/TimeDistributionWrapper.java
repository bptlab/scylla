package de.hpi.bpt.scylla.model.configuration.distribution;

import java.util.concurrent.TimeUnit;

public class TimeDistributionWrapper extends DistributionWrapper {
	
	protected TimeUnit timeUnit;

    public TimeDistributionWrapper(TimeUnit timeUnit) {
    	this.timeUnit = timeUnit;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

}
