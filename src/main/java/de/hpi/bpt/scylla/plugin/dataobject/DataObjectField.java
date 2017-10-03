package de.hpi.bpt.scylla.plugin.dataobject;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class DataObjectField {

	private DataDistributionWrapper distributionWrapper;
	private String fieldName;
	private Integer NodeId; //DataObject it contains to
	private String fieldType;

	private static HashMap<String, Object> dataObjectValues = new HashMap<String, Object>();

	DataObjectField(DataDistributionWrapper wrapper, Integer NodeId, String fieldName, String fieldtype) {
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

	static void addDataObjectValue(Integer processId, String fieldName, Object value) {
		dataObjectValues.put(fieldName + "_" + String.valueOf(processId), value);
	}

	public static Object getDataObjectValue(Integer processId, String fieldName) {
		return dataObjectValues.get(fieldName + "_" + String.valueOf(processId));
	}
}