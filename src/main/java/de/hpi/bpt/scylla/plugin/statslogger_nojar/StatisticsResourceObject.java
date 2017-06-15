package de.hpi.bpt.scylla.plugin.statslogger_nojar;

class StatisticsResourceObject {

    long durationInUse;
    long durationInUseIdle;
    long durationAvailable;
    double costs;

    public long getDurationInUse() {
        return durationInUse;
    }

    public void setDurationInUse(long durationInUse) {
        this.durationInUse = durationInUse;
    }

    public long getDurationInUseIdle() {
        return durationInUseIdle;
    }

    public void setDurationInUseIdle(long durationInUseIdle) {
        this.durationInUseIdle = durationInUseIdle;
    }

    public long getDurationAvailable() {
        return durationAvailable;
    }

    public void setDurationAvailable(long durationAvailable) {
        this.durationAvailable = durationAvailable;
    }

    public double getCosts() {
        return costs;
    }

    public void setCosts(double costs) {
        this.costs = costs;
    }
}
