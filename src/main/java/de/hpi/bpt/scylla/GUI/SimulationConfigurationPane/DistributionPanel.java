package de.hpi.bpt.scylla.GUI.SimulationConfigurationPane;

import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.text.NumberFormatter;

import de.hpi.bpt.scylla.GUI.FormManager;
import de.hpi.bpt.scylla.GUI.InsertRemoveListener;
import de.hpi.bpt.scylla.GUI.ScyllaGUI;
import de.hpi.bpt.scylla.GUI.GlobalConfigurationPane.NoNegativeDoubleFormat;
import de.hpi.bpt.scylla.creation.SimulationConfiguration.Distribution;
import de.hpi.bpt.scylla.creation.SimulationConfiguration.Distribution.AttributeType;
import de.hpi.bpt.scylla.creation.SimulationConfiguration.Distribution.DiscreteDistribution;
import de.hpi.bpt.scylla.creation.SimulationConfiguration.Distribution.DistributionType;

import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.NumberFormat;

import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;

import java.awt.Component;
import java.awt.GridBagConstraints;

/**
 * Display class for distributions
 * @author Leon Bein
 *
 */
@SuppressWarnings("serial")
public class DistributionPanel extends JPanel {
	
	/**Distribution wrapper object to be displayed by this panel.*/
	private Distribution distribution;
	/**Central object for managing the whole simulation configuration form.*/
	private FormManager formManager;

	/**
	 * Create the panel.
	 */
	public DistributionPanel(Distribution d,FormManager fm) {
		distribution = d;
		formManager = fm;
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		setLayout(gridBagLayout);
		
		//Switch distribution type
		DistributionType type = d.getType();
		switch(type){
		//Default behavior, if not specified differently
		default :
			//For each attribute: create a fitting input field
			for(int i = 0; i < type.attributes.length; i++){
				JLabel label = new JLabel(type.attributes[i]);
				GridBagConstraints gbc_label = new GridBagConstraints();
				gbc_label.gridx = 0;
				gbc_label.gridy = i;
				gbc_label.weightx = 0.1;
				gbc_label.fill = GridBagConstraints.HORIZONTAL;
				gbc_label.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
				add(label, gbc_label);
				JComponent input = getTypeInputComponent(type.attributes[i], type.types[i]);
				GridBagConstraints gbc_input = new GridBagConstraints();
				gbc_input.gridx = 1;
				gbc_input.gridy = i;
				gbc_input.weightx = 2;
				gbc_input.fill = GridBagConstraints.HORIZONTAL;
				gbc_input.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
				add(input, gbc_input);
			}
		break;
		//Behavior for descrete distribution type (a DiscreteDistributionPanel is created)
		case DISCRETE :
			JComponent table = getTypeInputComponent(null,AttributeType.ENTRYSET);
			GridBagConstraints gbc_table = new GridBagConstraints();
			gbc_table.gridx = 0;
			gbc_table.gridy = 0;
			gbc_table.weightx = 2;
			gbc_table.fill = GridBagConstraints.HORIZONTAL;
			gbc_table.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
			add(table, gbc_table);
		break;
		}
	}
	
	/**
	 * Creates input fields for given attribute types
	 * @param name : Name of the attribute, in order to display a label with that name
	 * @param type : Type of that attribute, to determine the input components behavior
	 * @return A component managing inputs for the given attribute
	 */
	private JComponent getTypeInputComponent(String name, Distribution.AttributeType type){
		String value = distribution.getAttribute(name);
		switch(type){
		//Type INT: Positive Integers, default value 0, no invalid characters allowed
		case INT : 
			NumberFormatter formatter = new NumberFormatter(NumberFormat.getInstance());
		    formatter.setValueClass(Integer.class);
		    formatter.setMinimum(0);
		    formatter.setMaximum(Integer.MAX_VALUE);
		    formatter.setAllowsInvalid(false);
			JFormattedTextField intfield = new JFormattedTextField(formatter);
			if(value != null && !value.isEmpty())intfield.setText(value);
			else intfield.setText("0");
			intfield.getDocument().addDocumentListener(new InsertRemoveListener((DocumentEvent e)->{
				if(formManager.isChangeFlag())return;
				try{
					long s = Long.parseLong(distribution.getAttribute(name));
					long n = Long.parseLong(intfield.getText());
					if(s != n){
						distribution.setAttribute(name, n);
						formManager.setSaved(false);
					}
				}catch(Exception exc){}
			}));
			intfield.setColumns(10);
			return intfield;
		
		//Type DOUBLE: No negative double, default value 0
		case DOUBLE : 
			JFormattedTextField doublefield = new JFormattedTextField(new NumberFormatter(new NoNegativeDoubleFormat()));
			if(value != null && !value.isEmpty())doublefield.setText(value);
			else doublefield.setText("0.0");
			doublefield.getDocument().addDocumentListener(new InsertRemoveListener((DocumentEvent e)->{
				if(formManager.isChangeFlag())return;
				try{
					distribution.setAttribute(name, Double.parseDouble(doublefield.getText()));
					formManager.setSaved(false);
				}catch(NumberFormatException exc){}
			}));
			doublefield.setColumns(10);
			return doublefield;
			
		//Type ENTRYSET: (Like discrete distribution) create a special panel for that
		case ENTRYSET : 
			if(distribution instanceof DiscreteDistribution){
				DiscreteDistribution dist = (DiscreteDistribution) distribution;
				return new DiscreteDistributionPanel(dist,formManager);
			}else return null;
			
		default : return null;
		}
	}
	
	/**
	 * En-/disables all input
	 */
	@Override
	public void setEnabled(boolean b) {
		for(Component c : getComponents()){
			c.setEnabled(b);
		}
	}

}
