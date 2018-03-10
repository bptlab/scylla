package de.hpi.bpt.scylla.plugin.statslogger_nojar;

class StatisticsTaskInstanceObject {

    /*
	String taskName;
    double avgWaitingTime = 0;
    int updatesAvgWaitingTime = 1;
    double avgDuration = 0;
    int updatesAvgDuration = 1;
    */
    // ToDo: Change this class to an instanceObject like StatisticsProcessInstanceObject
    
	// den Namen nehmen wir noch mit rein, ansonsten alles, was wir unten weiterverarbeiten wollen, um es auszugeben
	String taskName;
    long durationEffective = 0;
    long durationResourcesIdle = 0;
    long durationWaiting = 0;
    double cost = 0.0;
    
    public StatisticsTaskInstanceObject(String taskName) {
        this.taskName = taskName;
    }
    
    public String getTaskName() {
        return taskName;
    }
    
    public long getDurationEffective() {
        return durationEffective;
    }
    
    public long getDurationResourcesIdle() {
        return durationResourcesIdle;
    }
    
    public long getDurationWaiting() {
        return durationWaiting;
    }
    
    public double getCost() {
        return cost;
    }
    
    public void setDurationEffective(long durationEffective) {
        this.durationEffective = durationEffective;
    }
    
    public void setDurationResourcesIdle(long durationResourcesIdle) {
        this.durationResourcesIdle = durationResourcesIdle;
    }
    
    public void setDurationWaiting(long durationWaiting) {
        this.durationWaiting = durationWaiting;
    }
    
    public void setCost(double cost) {
        this.cost = cost;
    }
    
    /*
    public StatisticsTaskInstanceObject(String taskName) {
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
	*/

}
