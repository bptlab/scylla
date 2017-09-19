package de.hpi.bpt.scylla.GUI.GlobalConfigurationPane;

import java.text.DecimalFormat;
import java.text.FieldPosition;

@SuppressWarnings("serial")
public class NoNegativeDoubleFormat extends DecimalFormat{
	
	public NoNegativeDoubleFormat() {
		setMinimumFractionDigits(1);
	}
	
	@Override
	public StringBuffer format(double number, StringBuffer result, FieldPosition fieldPosition){
		return super.format(Math.abs(number), result, fieldPosition);
	}
	
	@Override
	public StringBuffer format(long number, StringBuffer result, FieldPosition fieldPosition){
		return super.format(Math.abs(number), result, fieldPosition);
	}
	
}
