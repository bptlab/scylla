package de.hpi.bpt.scylla.plugin.batch;

import java.util.List;

import de.hpi.bpt.scylla.model.process.ProcessModel;

class BatchRegion {

    private ProcessModel processModel;
    private int nodeId;
    private Integer maxBatchSize;
    private ActivationRule activationRule;
    private List<String> groupingCharacteristic;

    BatchRegion(ProcessModel processModel, int nodeId, Integer maxBatchSize, ActivationRule activationRule,
            List<String> groupingCharacteristic) {
        this.processModel = processModel;
        this.nodeId = nodeId;
        this.maxBatchSize = maxBatchSize;
        this.activationRule = activationRule;
        this.groupingCharacteristic = groupingCharacteristic;
    }

    ProcessModel getProcessModel() {
        return processModel;
    }

    int getNodeId() {
        return nodeId;
    }

    Integer getMaxBatchSize() {
        return maxBatchSize;
    }

   ActivationRule getActivationRule() {
        return activationRule;
    }

    List<String> getGroupingCharacteristic() {
        return groupingCharacteristic;
    }

}
