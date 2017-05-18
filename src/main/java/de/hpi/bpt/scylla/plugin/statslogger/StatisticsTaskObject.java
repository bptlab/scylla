package de.hpi.bpt.scylla.plugin.statslogger;

class StatisticsTaskObject {

    String taskName;
    double avgWaitingTime = 0;
    int updatesAvgWaitingTime = 1;
    double avgDuration = 0;
    int updatesAvgDuration = 1;

    public StatisticsTaskObject(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskName() {
        return taskName;
    }

    public double getAvgWaitingTime() {
        return avgWaitingTime;
    }

    public void updateAvgWaitingTime(long waitingTime) {
        this.avgWaitingTime += (waitingTime - this.avgWaitingTime) / updatesAvgWaitingTime;
        ++updatesAvgWaitingTime;
    }

    public double getAvgDuration() {
        return avgDuration;
    }

    public void updateAvgDuration(long duration) {
        this.avgDuration += (duration - this.avgDuration) / updatesAvgDuration;
        ++updatesAvgDuration;
    }

}
