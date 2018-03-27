package de.hpi.bpt.scylla.plugin.batch;

import java.util.List;

import de.hpi.bpt.scylla.model.process.ProcessModel;

public class BatchActivity {

    private ProcessModel processModel;
    private int nodeId;
    private Integer maxBatchSize;
    private BatchClusterExecutionType executionType;
    private ActivationRule activationRule;
    private List<String> groupingCharacteristic;

    public BatchActivity(ProcessModel processModel, int nodeId, Integer maxBatchSize, BatchClusterExecutionType executionType, ActivationRule activationRule,
            List<String> groupingCharacteristic) {
        this.processModel = processModel;
        this.nodeId = nodeId;
        this.maxBatchSize = maxBatchSize;
        this.executionType = executionType;
        this.activationRule = activationRule;
        this.groupingCharacteristic = groupingCharacteristic;
    }

    public BatchActivity(int nodeId, Integer maxBatchSize, BatchClusterExecutionType executionType, ActivationRule activationRule,
                         List<String> groupingCharacteristic) {
        this.nodeId = nodeId;
        this.maxBatchSize = maxBatchSize;

        this.executionType = executionType;
        this.activationRule = activationRule;
        this.groupingCharacteristic = groupingCharacteristic;
    }

    public void setProcessModel(ProcessModel processModel){
        this.processModel = processModel;
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
