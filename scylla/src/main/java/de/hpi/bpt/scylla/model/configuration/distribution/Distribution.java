package de.hpi.bpt.scylla.model.configuration.distribution;

import java.util.concurrent.TimeUnit;

/**
 * Abstract class for distributions.
 * 
 * @author Tsun Yin Wong
 */
public abstract class Distribution {

    protected TimeUnit timeUnit;

    public Distribution(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }
}
