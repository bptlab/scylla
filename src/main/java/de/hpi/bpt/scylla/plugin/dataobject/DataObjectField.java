package de.hpi.bpt.scylla.plugin.dataobject;

public class DataObjectField {

	private DataDistributionWrapper distributionWrapper;
	private String fieldName;
	private Integer NodeId; //DataObject it contains to
	
	public DataObjectField(DataDistributionWrapper wrapper, Integer NodeId, String fieldName) {
		this.distributionWrapper = wrapper;
		this.NodeId = NodeId;
		this.fieldName = fieldName;
	}

	public DataDistributionWrapper getDataDistributionWrapper() {
		return distributionWrapper;
	}
	
	public Integer getNodeId() {
		return NodeId;
	}
	
	public String getFieldName() {
		return this.fieldName;
	}
}
