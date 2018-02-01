package de.hpi.bpt.scylla.GUI.InputFields;

import java.text.Format;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

import javax.swing.JFormattedTextField;

import de.hpi.bpt.scylla.GUI.FormManager;

public abstract class DateField extends FormattedTextInputField<LocalDate>{
	
	public static final Format DATEFORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy").toFormat();

	public DateField(FormManager fm) {
		super(fm);
	}



	@Override
	protected JFormattedTextField createComponent() {
		JFormattedTextField field = new JFormattedTextField(DATEFORMAT);
		field.setValue(defaultValue());
		return field;
	}
	
	
	@Override
	public LocalDate getValue() {
		TemporalAccessor temp = super.getValue();
		if(temp == null)return null;
		return LocalDate.from(temp);
	}
	
	@Override
	protected Object defaultValue() {
		return LocalDate.now();
	}
	
	@Override
	public Class<LocalDate> getDataTypeClass(){
		return LocalDate.class;
	}

}
