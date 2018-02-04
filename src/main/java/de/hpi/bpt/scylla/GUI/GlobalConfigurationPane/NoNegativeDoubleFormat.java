package de.hpi.bpt.scylla.GUI.GlobalConfigurationPane;

import java.text.DecimalFormat;
import java.text.FieldPosition;

/**
 * Utility class for decimal format, that automatically converts negative numbers to their absolute values
 * @author Leon Bein
 *
 */
@SuppressWarnings("serial")
public class NoNegativeDoubleFormat extends DecimalFormat{
	
	/**
	 * Constructor
	 */
	public NoNegativeDoubleFormat() {
		//Always display that the content is a floating point number
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
