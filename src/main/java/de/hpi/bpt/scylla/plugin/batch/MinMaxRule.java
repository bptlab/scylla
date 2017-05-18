package de.hpi.bpt.scylla.plugin.batch;

import java.time.Duration;

class MinMaxRule {

    private int minInstances;
    private Duration minTimeout;
    private int maxInstances;
    private Duration maxTimeout;

    MinMaxRule(int minInstances, Duration minTimeout, int maxInstances, Duration maxTimeout) {
        this.minInstances = minInstances;
        this.minTimeout = minTimeout;
        this.maxInstances = maxInstances;
        this.maxTimeout = maxTimeout;
    }

    public int getMinInstances() {
        return minInstances;
    }

    public Duration getMinTimeout() {
        return minTimeout;
    }

    public int getMaxInstances() {
        return maxInstances;
    }

    public Duration getMaxTimeout() {
        return maxTimeout;
    }
}
