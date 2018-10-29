package de.hpi.bpt.scylla.plugin.batch;

import java.util.List;

public class BatchActivity {

    //Not needed, as Batch activities are stored with their process models as keys private ProcessModel processModel;
    private int nodeId;
    private Integer maxBatchSize;
    private BatchClusterExecutionType executionType;
    private ActivationRule activationRule;
    private List<String> groupingCharacteristic;

    /*Not used public BatchActivity(ProcessModel processModel, int nodeId, Integer maxBatchSize, BatchClusterExecutionType executionType, ActivationRule activationRule,
            List<String> groupingCharacteristic) {
        this.processModel = processModel;
        this.nodeId = nodeId;
        this.maxBatchSize = maxBatchSize;
        this.executionType = executionType;
        this.activationRule = activationRule;
        this.groupingCharacteristic = groupingCharacteristic;
    }*/

    public BatchActivity(int nodeId, Integer maxBatchSize, BatchClusterExecutionType executionType, ActivationRule activationRule,
                         List<String> groupingCharacteristic) {
        this.nodeId = nodeId;
        this.maxBatchSize = maxBatchSize;
        this.executionType = executionType;
        this.activationRule = activationRule;
        this.groupingCharacteristic = groupingCharacteristic;
    }

    /*public void setProcessModel(ProcessModel processModel){
        this.processModel = processModel;
    }*/

    int getNodeId() {
        return nodeId;
    }

    Integer getMaxBatchSize() {
        return maxBatchSize;
    }

    public BatchClusterExecutionType getExecutionType() {
        return executionType;
    }

    ActivationRule getActivationRule() {
        return activationRule;
    }

    List<String> getGroupingCharacteristic() {
        return groupingCharacteristic;
    }

}
