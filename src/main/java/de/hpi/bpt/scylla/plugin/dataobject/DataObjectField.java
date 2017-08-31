package de.hpi.bpt.scylla.plugin.dataobject;

public class DataObjectField {

	private DataDistributionWrapper distributionWrapper;
	
	private Integer NodeId; //DataObject it contains to
	
	public DataObjectField(DataDistributionWrapper wrapper, Integer NodeId) {
		this.distributionWrapper = wrapper;
		this.NodeId = NodeId;
	}

	public DataDistributionWrapper getDataDistributionWrapper() {
		return distributionWrapper;
	}
	
	public Integer getNodeId() {
		return NodeId;
	}
}
