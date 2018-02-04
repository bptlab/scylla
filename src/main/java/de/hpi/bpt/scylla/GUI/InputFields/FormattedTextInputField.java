package de.hpi.bpt.scylla.GUI.InputFields;

import javax.swing.JFormattedTextField;

import de.hpi.bpt.scylla.GUI.FormManager;

public abstract class FormattedTextInputField<T> extends InputField<T, JFormattedTextField> {

	public FormattedTextInputField(FormManager fm) {
		super(fm);
	}

	@Override
	protected JFormattedTextField createComponent() {
		return new JFormattedTextField();
	}

	@SuppressWarnings("unchecked")
	@Override
	public T getValue() {
		return (T) getComponent().getValue();
	}

	@Override
	public void setValue(T v) {
		getComponent().setValue(v);
	}


	@Override
	public void reset() {
		getComponent().setValue(defaultValue());
	}

	@Override
	public void clear() {
		getComponent().setValue(null);
	}
	
	protected abstract Object defaultValue();

}
