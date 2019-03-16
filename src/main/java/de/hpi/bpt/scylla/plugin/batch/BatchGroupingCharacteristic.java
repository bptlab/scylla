package de.hpi.bpt.scylla.plugin.batch;

import java.util.Objects;

import de.hpi.bpt.scylla.plugin.dataobject.DataObjectField;
import de.hpi.bpt.scylla.simulation.ProcessInstance;

public class BatchGroupingCharacteristic {
	
	private String dataViewElement;
	
	public BatchGroupingCharacteristic(String dataViewElement) {
		setDataViewElement(dataViewElement);
	}

	public String getDataViewElement() {
		return dataViewElement;
	}

	public void setDataViewElement(String dataViewElement) {
		this.dataViewElement = dataViewElement;
	}
	
	public boolean isFulfilledBetween(ProcessInstance pi1, ProcessInstance pi2) {
		return Objects.equals(valueIn(pi1), valueIn(pi2));
	}
	
	private Object valueIn(ProcessInstance processInstance) {
		return DataObjectField.getDataObjectValue(processInstance.getId(), getDataViewElement());
	}
	
	@Override
	public boolean equals(Object other) {
		return this == other || 
				(Objects.nonNull(other) && other.getClass().isAssignableFrom(BatchGroupingCharacteristic.class) 
				&& ((BatchGroupingCharacteristic)other).getDataViewElement().equals(getDataViewElement()));
	}
	
	@Override
	public int hashCode() {
		return getDataViewElement().hashCode();
	}

}
