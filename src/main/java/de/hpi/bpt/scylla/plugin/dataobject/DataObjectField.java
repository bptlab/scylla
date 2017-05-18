package de.hpi.bpt.scylla.plugin.dataobject;

public class DataObjectField {

	private DataDistributionWrapper distributionWrapper;
	
	public DataObjectField(DataDistributionWrapper wrapper) {
		this.distributionWrapper = wrapper;
	}

	public DataDistributionWrapper getDataDistributionWrapper() {
		return distributionWrapper;
	}
}
