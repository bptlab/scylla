package de.hpi.bpt.scylla.GUI.InputFields;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JFormattedTextField;
import javax.swing.text.NumberFormatter;

import de.hpi.bpt.scylla.GUI.FormManager;

public abstract class NumberField<T extends Number> extends InputField<T,JFormattedTextField> {

	protected NumberFormatter formatter;

	public static NumberFormat NUMBERFORMAT = NumberFormat.getNumberInstance();static {NUMBERFORMAT.setGroupingUsed(false);}
	public static NumberFormat DECIMALFORMAT = new DecimalFormat(); static{DECIMALFORMAT.setMinimumFractionDigits(1);DECIMALFORMAT.setMaximumFractionDigits(Integer.MAX_VALUE);}

	public NumberField(FormManager fm) {
		super(fm);
	}
	
	/**
	 * @return The format that is used for my component. 
	 * Can be overridden to change/add properties.
	 */
	protected NumberFormat getFormat() {
		return NUMBERFORMAT;
	}
	
	/**
	 * @return The formatter that is used for my component. 
	 * Can be overridden to change/add properties.
	 */
	protected NumberFormatter getFormatter() {
		formatter = new NumberFormatter(getFormat());
		formatter.setValueClass(getDataTypeClass());
	    return formatter;
	}

	@Override
	protected JFormattedTextField createComponent() {
	    JFormattedTextField field = new JFormattedTextField(getFormatter());
	    field.setValue(0);
	    return field;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected T getValue() {
		return (T)getComponent().getValue();
	}

	@Override
	protected void setValue(T v) {
		getComponent().setValue(v);
	}
	
	public void setMaximum(T max) {
		formatter.setMaximum((Comparable<?>) max);
	}
	
	public void setMinimum(T min) {
		formatter.setMinimum((Comparable<?>) min);
	}


}
