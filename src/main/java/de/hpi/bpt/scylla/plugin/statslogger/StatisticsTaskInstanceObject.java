package de.hpi.bpt.scylla.plugin.statslogger;

class StatisticsTaskInstanceObject {

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
}
