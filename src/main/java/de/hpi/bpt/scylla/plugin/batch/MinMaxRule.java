package de.hpi.bpt.scylla.plugin.batch;

import java.time.Duration;

import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.event.TaskBeginEvent;
import de.hpi.bpt.scylla.simulation.event.TaskEnableEvent;

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

    public int getThreshold(TaskBeginEvent desmojEvent, ProcessInstance processInstance) {


        BatchPluginUtils pluginInstance = BatchPluginUtils.getInstance();
        for (Integer key : pluginInstance.runningInstances.keySet()) {
            TaskEnableEvent currentEventOfInst = pluginInstance.runningInstances.get(key);


            if (processInstance.getId() != key && currentEventOfInst.getNodeId() <= desmojEvent.getNodeId()) {


                BatchActivity batchActivity = pluginInstance.getBatchClusters().get(processInstance.getProcessModel().getId()).get(desmojEvent.getNodeId()).get(0).getBatchActivity();

                String dataView = pluginInstance.getDataViewOfInstance(processInstance.getId(), batchActivity);
                String dataViewOfOther = pluginInstance.getDataViewOfInstance(key, batchActivity);


                if (dataView.equals(dataViewOfOther)) {
                    return maxInstances;
                }
            }
        }


        return minInstances;

    }

    public Duration getTimeOut(TaskBeginEvent desmojEvent, ProcessInstance processInstance) {
        BatchPluginUtils pluginInstance = BatchPluginUtils.getInstance();
        for (Integer instance : pluginInstance.runningInstances.keySet()) {
            TaskEnableEvent currentEventOfInst = pluginInstance.runningInstances.get(instance);


            if (processInstance.getId() != instance && currentEventOfInst.getNodeId() <= desmojEvent.getNodeId()) {

                if (!pluginInstance.getBatchClusters().isEmpty()) {
                    BatchActivity batchActivity = pluginInstance.getBatchClusters().get(processInstance.getProcessModel().getId()).get(desmojEvent.getNodeId()).get(0).getBatchActivity();

                    String dataView = pluginInstance.getDataViewOfInstance(processInstance.getId(), batchActivity);
                    String dataViewOfOther = pluginInstance.getDataViewOfInstance(instance, batchActivity);


                    if (dataView.equals(dataViewOfOther)) {
                        return maxTimeout;
                    }
                }

            }
        }


        return minTimeout;
        //TODO if similar instances exists then minInstances
        // TODO implement ExistingEqualPI()
    }

}
