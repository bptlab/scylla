package de.hpi.bpt.scylla.plugin.statslogger_nojar;

class StatisticsProcessInstanceObject {

    long durationTotal;
    long durationInactive;
    long durationResourcesIdle;
    long durationWaiting;
    double costs;

    public long getDurationTotal() {
        return durationTotal;
    }

    public void setDurationTotal(long durationTotal) {
        this.durationTotal = durationTotal;
    }

    public long getDurationInactive() {
        return durationInactive;
    }

    public void setDurationInactive(long durationInactive) {
        this.durationInactive = durationInactive;
    }

    public long getDurationResourcesIdle() {
        return durationResourcesIdle;
    }

    public void setDurationResourcesIdle(long durationResourcesIdle) {
        this.durationResourcesIdle = durationResourcesIdle;
    }

    public long getDurationWaiting() {
        return durationWaiting;
    }

    public void setDurationWaiting(long durationWaiting) {
        this.durationWaiting = durationWaiting;
    }

    public double getCosts() {
        return costs;
    }

    public void setCosts(double costs) {
        this.costs = costs;
    }
}
