package de.hpi.bpt.scylla.GUI.InputFields;

import javax.swing.JTextField;

import de.hpi.bpt.scylla.GUI.FormManager;

public abstract class StringField extends InputField<String, JTextField>{

	public StringField(FormManager fm) {
		super(fm);
	}

	@Override
	protected JTextField createComponent() {
		return new JTextField();
	}

	@Override
	public String getValue() {
		return getComponent().getText();
	}

	@Override
	public void setValue(String v) {
		getComponent().setText(v);
	}

	@Override
	public void reset() {
		getComponent().setText("");
	}

	@Override
	public void clear() {
		getComponent().setText(null);
	}
	
	@Override
	protected Class<String> getDataTypeClass() {
		return String.class;
	}

}
