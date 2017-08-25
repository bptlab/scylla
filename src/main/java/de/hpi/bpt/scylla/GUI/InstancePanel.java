package de.hpi.bpt.scylla.GUI;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.NumberFormat;

import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JCheckBox;

@SuppressWarnings("serial")
public class InstancePanel extends JPanel{
	
	public InstancePanel() {
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
		
		JTextField testfieldCost = new JFormattedTextField(NumberFormat.getNumberInstance());
		GridBagConstraints gbc_textfieldCostEdit = new GridBagConstraints();
		gbc_textfieldCostEdit.insets = new Insets(0, 0, 5, 5);
		gbc_textfieldCostEdit.fill = GridBagConstraints.HORIZONTAL;
		gbc_textfieldCostEdit.gridx = 1;
		gbc_textfieldCostEdit.gridy = 0;
		add(testfieldCost, gbc_textfieldCostEdit);
		testfieldCost.setColumns(10);
		
		JTextField textFieldTimeunit = new JTextField();
		textFieldTimeunit.setText("per");
		textFieldTimeunit.setEditable(false);
		GridBagConstraints gbc_textFieldTimeunit = new GridBagConstraints();
		gbc_textFieldTimeunit.insets = new Insets(0, 0, 5, 5);
		gbc_textFieldTimeunit.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldTimeunit.gridx = 2;
		gbc_textFieldTimeunit.gridy = 0;
		add(textFieldTimeunit, gbc_textFieldTimeunit);
		textFieldTimeunit.setColumns(10);
		
		JComboBox comboboxTimeunit = new JComboBox();
		GridBagConstraints gbc_comboboxTimeunit = new GridBagConstraints();
		gbc_comboboxTimeunit.insets = new Insets(0, 0, 5, 5);
		gbc_comboboxTimeunit.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboboxTimeunit.gridx = 3;
		gbc_comboboxTimeunit.gridy = 0;
		add(comboboxTimeunit, gbc_comboboxTimeunit);
		
		JCheckBox checkboxDefaultCost = new JCheckBox("use default");
		checkboxDefaultCost.setIcon(new ScalingCheckBoxIcon(ScyllaGUI.DEFAULTFONT.getSize()));
		GridBagConstraints gbc_checkboxDefaultCost = new GridBagConstraints();
		gbc_checkboxDefaultCost.gridx = 4;
		gbc_checkboxDefaultCost.gridy = 0;
		add(checkboxDefaultCost, gbc_checkboxDefaultCost);
		
		JLabel labelTimetable = new JLabel();
		labelTimetable.setText("Default Timetable");
		GridBagConstraints gbc_textfieldTimetable = new GridBagConstraints();
		gbc_textfieldTimetable.insets = new Insets(0, 0, 0, 5);
		gbc_textfieldTimetable.fill = GridBagConstraints.HORIZONTAL;
		gbc_textfieldTimetable.gridx = 0;
		gbc_textfieldTimetable.gridy = 1;
		add(labelTimetable, gbc_textfieldTimetable);
		
		JComboBox comboboxTimetable = new JComboBox();
		GridBagConstraints gbc_comboboxTimetable = new GridBagConstraints();
		gbc_comboboxTimetable.gridwidth = 3;
		gbc_comboboxTimetable.insets = new Insets(0, 0, 0, 5);
		gbc_comboboxTimetable.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboboxTimetable.gridx = 1;
		gbc_comboboxTimetable.gridy = 1;
		add(comboboxTimetable, gbc_comboboxTimetable);
		
		JCheckBox checkboxDefaultTimetable = new JCheckBox("use default");
		checkboxDefaultTimetable.setIcon(new ScalingCheckBoxIcon(ScyllaGUI.DEFAULTFONT.getSize()));
		GridBagConstraints gbc_checkboxDefaultTimetable = new GridBagConstraints();
		gbc_checkboxDefaultTimetable.gridx = 4;
		gbc_checkboxDefaultTimetable.gridy = 1;
		add(checkboxDefaultTimetable, gbc_checkboxDefaultTimetable);
	}

}
