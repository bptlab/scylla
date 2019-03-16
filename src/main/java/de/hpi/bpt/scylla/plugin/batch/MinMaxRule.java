package de.hpi.bpt.scylla.plugin.batch;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.event.TaskBeginEvent;
import de.hpi.bpt.scylla.simulation.event.TaskEnableEvent;

/**
 * A MinMaxRule wants to group process instances with similar grouping characteristics.
 * We use data attributes as grouping characteristics.
 * The rule defines two sets of timeout and threshold.
 * When at least one other instance with the same grouping characteristic is running,
 * the set with higher threshold and timeout is used otherwise the lower threshold and timeout are used
 * @author was not Leon Bein
 * @deprecated Not finished
 */
public class MinMaxRule implements ActivationRule{

    private int minInstances;
    private Duration minTimeout;
    private int maxInstances;
    private Duration maxTimeout;

    public MinMaxRule(int minInstances, Duration minTimeout, int maxInstances, Duration maxTimeout) {
        this.minInstances = minInstances;
        this.minTimeout = minTimeout;
        this.maxInstances = maxInstances;
        this.maxTimeout = maxTimeout;
    }
    
    /**
     * TODO
     * @return this should return the information to all currently running process instances, i.o. to be able to identify similar instances that are to reach the batch region
     */
    public List<TaskEnableEvent> getRunningInstances() {
    	return new ArrayList<>();
    }

    public int getThreshold(TaskBeginEvent desmojEvent, ProcessInstance processInstance) {


        BatchPluginUtils pluginInstance = BatchPluginUtils.getInstance();
        for (TaskEnableEvent currentEventOfInst : getRunningInstances()) {

            /*
             * TODO who wrote: currentEventOfInst.getNodeId() <= desmojEvent.getNodeId() ?!?!?
             * if it should do what i think it should do, it does not do what it should do
             * Interpretation: check if nodeA is before nodeB
             * Problem: NodeIds do not follow flow e.g. o->A->B->0 may have NodeIds 4,2,1,3
             */
            if (processInstance.getId() != currentEventOfInst.getProcessInstance().getId() && currentEventOfInst.getNodeId() <= desmojEvent.getNodeId()) {


                BatchActivity batchActivity = pluginInstance.getBatchClusters().get(processInstance.getProcessModel().getId()).get(desmojEvent.getNodeId()).get(0).getBatchActivity();

                if(batchActivity.getGroupingCharacteristic().stream()
                	.allMatch(each -> each.isFulfilledBetween(processInstance, currentEventOfInst.getProcessInstance()))) return maxInstances;
            }
        }


        return minInstances;

    }

    public Duration getTimeOut(TaskBeginEvent desmojEvent, ProcessInstance processInstance) {
        BatchPluginUtils pluginInstance = BatchPluginUtils.getInstance();
        for (TaskEnableEvent currentEventOfInst : getRunningInstances()) {


            if (processInstance.getId() != currentEventOfInst.getProcessInstance().getId() && currentEventOfInst.getNodeId() <= desmojEvent.getNodeId()) {

                if (!pluginInstance.getBatchClusters().isEmpty()) {
                    BatchActivity batchActivity = pluginInstance.getBatchClusters().get(processInstance.getProcessModel().getId()).get(desmojEvent.getNodeId()).get(0).getBatchActivity();

                    if(batchActivity.getGroupingCharacteristic().stream()
                    	.allMatch(each -> each.isFulfilledBetween(processInstance, currentEventOfInst.getProcessInstance()))) return maxTimeout;
                }

            }
        }


        return minTimeout;
        //TODO if similar instances exists then minInstances
        // TODO implement ExistingEqualPI()
    }

}
