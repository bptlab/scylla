package de.hpi.bpt.scylla.GUI.InputFields;


import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import de.hpi.bpt.scylla.GUI.FormManager;

public abstract class NumberSpinner<DataType extends Number & Comparable<DataType>> extends InputField<DataType, JSpinner>{
	
	private SpinnerNumberModel model;

	protected NumberSpinner(FormManager fm, DataType value, DataType minimum, DataType maximum, DataType stepSize) {
		super(fm, new SpinnerNumberModel(value, minimum, maximum, stepSize));
	}
	
	@Override
	protected JSpinner createComponent(Object o) {
		model = (SpinnerNumberModel) o;
		return super.createComponent(o);
	}

	@Override
	protected JSpinner createComponent() {
		return new JSpinner(model);
	}

	@SuppressWarnings("unchecked")
	@Override
	public DataType getValue() {
		return (DataType) getComponent().getValue();
	}

	@Override
	public void setValue(DataType v) {
		getComponent().setValue(v);
	}

	@Override
	public void reset() {
		getComponent().setValue(model.getMinimum());
	}

	@Override
	public void clear() {
		getComponent().setValue(null);
	}

}
