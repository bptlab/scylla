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

@SuppressWarnings("serial")
public class DistributionPanel extends JPanel {
	
	private Distribution distribution;
	private FormManager fm;

	/**
	 * Create the panel.
	 */
	public DistributionPanel(Distribution d,FormManager f) {
		distribution = d;
		fm = f;
		GridBagLayout gridBagLayout = new GridBagLayout();
		setLayout(gridBagLayout);
		
		DistributionType type = d.getType();
		switch(type){
		case descrete :
			JComponent table = getTypeInputComponent(null,AttributeType.ENTRYSET);
			GridBagConstraints gbc_table = new GridBagConstraints();
			gbc_table.gridx = 0;
			gbc_table.gridy = 0;
			gbc_table.weightx = 2;
			gbc_table.fill = GridBagConstraints.HORIZONTAL;
			gbc_table.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
			add(table, gbc_table);
		break;
		default :
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
		}
	}
	
	private JComponent getTypeInputComponent(String name, Distribution.AttributeType type){
		String value = distribution.getAttribute(name);
		switch(type){
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
				if(fm.isChangeFlag())return;
				try{
					long s = Long.parseLong(distribution.getAttribute(name));
					long n = Long.parseLong(intfield.getText());
					if(s != n){
						distribution.setAttribute(name, n);
						fm.setSaved(false);
					}
				}catch(Exception exc){}
			}));
			intfield.setColumns(10);
			return intfield;
			
		case DOUBLE : 
			JFormattedTextField doublefield = new JFormattedTextField(new NumberFormatter(new NoNegativeDoubleFormat()));
			if(value != null && !value.isEmpty())doublefield.setText(value);
			else doublefield.setText("0.0");
			doublefield.getDocument().addDocumentListener(new InsertRemoveListener((DocumentEvent e)->{
				if(fm.isChangeFlag())return;
				try{
					distribution.setAttribute(name, Double.parseDouble(doublefield.getText()));
					fm.setSaved(false);
				}catch(NumberFormatException exc){}
			}));
			doublefield.setColumns(10);
			return doublefield;
		case ENTRYSET : 
			if(distribution instanceof DiscreteDistribution){
				DiscreteDistribution dist = (DiscreteDistribution) distribution;
				return new DiscreteDistributionPanel(dist,fm);
			}else return null;
			
		default : return null;
		}
	}
	
	@Override
	public void setEnabled(boolean b) {
		for(Component c : getComponents()){
			c.setEnabled(b);
		}
	}

}
