package de.hpi.bpt.scylla.GUI.InputFields;

import java.text.Format;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

import javax.swing.JFormattedTextField;

import de.hpi.bpt.scylla.GUI.FormManager;

public abstract class TimeField extends FormattedTextInputField<LocalTime>{
	
	public static final Format TIMEFORMAT = DateTimeFormatter.ISO_LOCAL_TIME.toFormat();
	protected boolean nanoSecondsAllowed;

	public TimeField(FormManager fm) {
		super(fm);
		setNanoSecondsAllowed(false);
	}
	
	@Override
	protected JFormattedTextField createComponent() {
		JFormattedTextField field = new JFormattedTextField(TIMEFORMAT);
		field.setValue(defaultValue());
		return field;
	}
	
	@Override
	public void setValue(LocalTime v) {
		if(!isNanoSecondsAllowed()) v = v.withNano(0);
		super.setValue(v);
	}
	
	@Override
	public LocalTime getValue() {
		TemporalAccessor temp = super.getValue();
		if(temp == null)return null;
		return LocalTime.from(temp);
	}
	
	
	@Override
	protected Object defaultValue() {
		return LocalTime.of(0, 0, 0);
	}
	
	@Override
	public Class<LocalTime> getDataTypeClass(){
		return LocalTime.class;
	}
	
	public boolean isNanoSecondsAllowed() {
		return nanoSecondsAllowed;
	}

	public void setNanoSecondsAllowed(boolean nanoSecondsAllowed) {
		this.nanoSecondsAllowed = nanoSecondsAllowed;
	}


	

}
