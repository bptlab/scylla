package de.hpi.bpt.scylla.plugin.dataobject;

public class DataObjectField {

	private DataDistributionWrapper distributionWrapper;
	private String fieldName;
	private Integer NodeId; //DataObject it contains to
	private String fieldType;
	
	public DataObjectField(DataDistributionWrapper wrapper, Integer NodeId, String fieldName, String fieldtype) {
		this.distributionWrapper = wrapper;
		this.NodeId = NodeId;
		this.fieldName = fieldName;
		this.fieldType = fieldtype;
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
	
	public String getFieldType() {
		return this.fieldType;
	}
}
