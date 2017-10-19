package de.hpi.bpt.scylla.plugin.batch;

import java.time.Duration;

import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.event.TaskBeginEvent;

class MinMaxRule implements ActivationRule{

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

    public int getThreshold(TaskBeginEvent desmojEvent, ProcessInstance processInstance) {
        return minInstances;
        //TODO if similar instances exists then maxInstances
        // TODO implement ExistingEqualPI() --> check all Instances which has not yet reached the batch activity, 
        //whether one exists with the same data view, if yes, provide maxInstances!!!! 
        
    }

    public Duration getTimeOut(TaskBeginEvent desmojEvent, ProcessInstance processInstance) {
        return minTimeout;
      //TODO if similar instances exists then minInstances
        // TODO implement ExistingEqualPI()
    }

}
