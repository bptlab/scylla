package de.hpi.bpt.scylla.GUI.GlobalConfigurationPane;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;

import de.hpi.bpt.scylla.GUI.FormulaManager;
import de.hpi.bpt.scylla.GUI.InsertRemoveListener;
import de.hpi.bpt.scylla.GUI.ScalingCheckBoxIcon;
import de.hpi.bpt.scylla.GUI.ScyllaGUI;
import de.hpi.bpt.scylla.creation.GlobalConfiguration.GlobalConfigurationCreator.ResourceType.ResourceInstance;

@SuppressWarnings("serial")
public class InstancePanel extends JPanel{
	
	private JFormattedTextField textfieldCost;
	private JComboBox<TimeUnit> comboboxTimeunit;
	private JComboBox<String> comboboxTimetable;
	private JCheckBox checkboxDefaultTimetable;
	private JCheckBox checkboxDefaultCost;
	private ResourceInstance instance;
	
	private FormulaManager formulaManager;

	public InstancePanel(FormulaManager fm) {
		formulaManager = fm;
		GridBagLayout gbl_topPanel = new GridBagLayout();
		gbl_topPanel.columnWeights = new double[]{0, 1.0, 0, 1.0, 0.0};
		setLayout(gbl_topPanel);
		
		JLabel labelCost = new JLabel();
		labelCost.setText("Cost");
		GridBagConstraints gbc_textfieldCost = new GridBagConstraints();
		gbc_textfieldCost.insets = new Insets(0, 0, 5, 5);
		gbc_textfieldCost.fill = GridBagConstraints.HORIZONTAL;
		gbc_textfieldCost.gridx = 0;
		gbc_textfieldCost.gridy = 0;
		add(labelCost, gbc_textfieldCost);
		
		textfieldCost = new JFormattedTextField(DecimalFormat.getInstance(Locale.ENGLISH));
		textfieldCost.getDocument().addDocumentListener(new InsertRemoveListener((DocumentEvent e)->{
			if(formulaManager.isChangeFlag())return;
			try{
				instance.setCost(Double.parseDouble(textfieldCost.getText()));
				formulaManager.setSaved(false);
			}catch(NumberFormatException exc){}
		}));
		GridBagConstraints gbc_textfieldCostEdit = new GridBagConstraints();
		gbc_textfieldCostEdit.insets = new Insets(0, 0, 5, 5);
		gbc_textfieldCostEdit.fill = GridBagConstraints.HORIZONTAL;
		gbc_textfieldCostEdit.gridx = 1;
		gbc_textfieldCostEdit.gridy = 0;
		add(textfieldCost, gbc_textfieldCostEdit);
		textfieldCost.setColumns(10);
		
		JLabel labelTimeunit = new JLabel();
		labelTimeunit.setText("per");
		GridBagConstraints gbc_textFieldTimeunit = new GridBagConstraints();
		gbc_textFieldTimeunit.insets = new Insets(0, 5+20, 5, 5+25);
		gbc_textFieldTimeunit.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldTimeunit.gridx = 2;
		gbc_textFieldTimeunit.gridy = 0;
		add(labelTimeunit, gbc_textFieldTimeunit);
		
		comboboxTimeunit = new JComboBox<TimeUnit>(TimeUnit.values());
		comboboxTimeunit.addItemListener((ItemEvent e)->{
			if(formulaManager.isChangeFlag())return;
			instance.setTimeUnit((TimeUnit) comboboxTimeunit.getSelectedItem());
			formulaManager.setSaved(false);
		});
		GridBagConstraints gbc_comboboxTimeunit = new GridBagConstraints();
		gbc_comboboxTimeunit.insets = new Insets(0, 0, 5, 5);
		gbc_comboboxTimeunit.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboboxTimeunit.gridx = 3;
		gbc_comboboxTimeunit.gridy = 0;
		add(comboboxTimeunit, gbc_comboboxTimeunit);
		
		checkboxDefaultCost = new JCheckBox("use default");
		checkboxDefaultCost.addItemListener((ItemEvent e)->{
			if(checkboxDefaultCost.isSelected()){
				defaultsChanged();
				labelCost.setEnabled(false);
				textfieldCost.setEnabled(false);
				labelTimeunit.setEnabled(false);
				comboboxTimeunit.setEnabled(false);
			}else{
				labelCost.setEnabled(true);
				textfieldCost.setEnabled(true);
				labelTimeunit.setEnabled(true);
				comboboxTimeunit.setEnabled(true);
			}
		});
		checkboxDefaultCost.setIcon(new ScalingCheckBoxIcon(ScyllaGUI.DEFAULTFONT.getSize()));
		GridBagConstraints gbc_checkboxDefaultCost = new GridBagConstraints();
		gbc_checkboxDefaultCost.gridx = 4;
		gbc_checkboxDefaultCost.gridy = 0;
		add(checkboxDefaultCost, gbc_checkboxDefaultCost);
		
		JLabel labelTimetable = new JLabel();
		labelTimetable.setText("Timetable");
		GridBagConstraints gbc_textfieldTimetable = new GridBagConstraints();
		gbc_textfieldTimetable.insets = new Insets(0, 0, 0, 5);
		gbc_textfieldTimetable.fill = GridBagConstraints.HORIZONTAL;
		gbc_textfieldTimetable.gridx = 0;
		gbc_textfieldTimetable.gridy = 1;
		add(labelTimetable, gbc_textfieldTimetable);
		
		comboboxTimetable = new JComboBox<String>();
		comboboxTimetable.addItemListener((ItemEvent e)->{
			if(formulaManager.isChangeFlag())return;
			instance.setTimetableId((String) comboboxTimetable.getSelectedItem());
			formulaManager.setSaved(false);
		});
		formulaManager.getTimetableObserverList().add(comboboxTimetable.getModel());
		GridBagConstraints gbc_comboboxTimetable = new GridBagConstraints();
		gbc_comboboxTimetable.gridwidth = 3;
		gbc_comboboxTimetable.insets = new Insets(0, 0, 0, 5);
		gbc_comboboxTimetable.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboboxTimetable.gridx = 1;
		gbc_comboboxTimetable.gridy = 1;
		add(comboboxTimetable, gbc_comboboxTimetable);
		
		checkboxDefaultTimetable = new JCheckBox("use default");
		checkboxDefaultTimetable.addItemListener((ItemEvent e)->{
			if(checkboxDefaultTimetable.isSelected()){
				defaultsChanged();
				labelTimetable.setEnabled(false);
				comboboxTimetable.setEnabled(false);
			}else{
				labelTimetable.setEnabled(true);
				comboboxTimetable.setEnabled(true);
			}
		});
		checkboxDefaultTimetable.setIcon(new ScalingCheckBoxIcon(ScyllaGUI.DEFAULTFONT.getSize()));
		GridBagConstraints gbc_checkboxDefaultTimetable = new GridBagConstraints();
		gbc_checkboxDefaultTimetable.gridx = 4;
		gbc_checkboxDefaultTimetable.gridy = 1;
		add(checkboxDefaultTimetable, gbc_checkboxDefaultTimetable);
	}

	public void setInstance(ResourceInstance inst) {
		formulaManager.setChangeFlag(true);
		instance = inst;
		if(inst.getCost() != null){
			checkboxDefaultCost.setSelected(false);
			textfieldCost.setValue(Double.parseDouble(inst.getCost()));
		}
		else checkboxDefaultCost.setSelected(true);
		
		if(inst.getTimeUnit() != null)comboboxTimeunit.setSelectedItem(TimeUnit.valueOf(inst.getTimeUnit()));
		else comboboxTimeunit.setSelectedItem(TimeUnit.valueOf(inst.resourceType.getDefaultTimeUnit()));
		
		if(inst.getTimetableId() != null){
			comboboxTimetable.setSelectedItem(inst.getTimetableId());
			checkboxDefaultTimetable.setSelected(false);
		}
		else checkboxDefaultTimetable.setSelected(true);
		formulaManager.setChangeFlag(false);
	}
	
	public void defaultsChanged(){
		if(checkboxDefaultCost.isSelected()){
			textfieldCost.setText(instance.resourceType.getDefaultCost());
			comboboxTimeunit.setSelectedItem(TimeUnit.valueOf(instance.resourceType.getDefaultTimeUnit()));
			instance.removeCost();
			instance.removeTimetUnit();
		}
		if(checkboxDefaultTimetable.isSelected()){
			instance.removeTimetableId();
			comboboxTimetable.setSelectedItem(instance.resourceType.getDefaultTimetableId());
		}
	}

}
