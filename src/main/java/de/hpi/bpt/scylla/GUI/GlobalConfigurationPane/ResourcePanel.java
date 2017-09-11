package de.hpi.bpt.scylla.GUI.GlobalConfigurationPane;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.NumberFormat;

import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import de.hpi.bpt.scylla.GUI.ListChooserPanel;
import de.hpi.bpt.scylla.GUI.ListChooserPanel.ComponentHolder;

@SuppressWarnings("serial")
public class ResourcePanel extends JSplitPane{
	public ResourcePanel() {
		setEnabled(false);
		setOrientation(JSplitPane.VERTICAL_SPLIT);
		
		JPanel topPanel = new JPanel();
		setLeftComponent(topPanel);
		GridBagLayout gbl_topPanel = new GridBagLayout();
		gbl_topPanel.columnWeights = new double[]{0, 1.0, 0, 1.0};
		topPanel.setLayout(gbl_topPanel);
		
		JLabel labelQuantity = new JLabel();
		labelQuantity.setText("Quantity");
		GridBagConstraints gbc_textfieldQuantity = new GridBagConstraints();
		gbc_textfieldQuantity.insets = new Insets(0, 0, 5, 5);
		gbc_textfieldQuantity.fill = GridBagConstraints.HORIZONTAL;
		gbc_textfieldQuantity.gridx = 0;
		gbc_textfieldQuantity.gridy = 0;
		topPanel.add(labelQuantity, gbc_textfieldQuantity);
		
		JSpinner spinnerQuantity = new JSpinner();
		spinnerQuantity.setModel(new SpinnerNumberModel(new Integer(0), new Integer(0), null, new Integer(1)));
		((JSpinner.DefaultEditor) spinnerQuantity.getEditor()).getTextField().setColumns(2);
		GridBagConstraints gbc_spinnerQuantity = new GridBagConstraints();
		gbc_spinnerQuantity.gridwidth = 3;
		gbc_spinnerQuantity.insets = new Insets(0, 0, 5, 5);
		gbc_spinnerQuantity.gridx = 1;
		gbc_spinnerQuantity.gridy = 0;
		topPanel.add(spinnerQuantity, gbc_spinnerQuantity);
		
		JLabel labelCost = new JLabel();
		labelCost.setText("Cost");
		GridBagConstraints gbc_textfieldCost = new GridBagConstraints();
		gbc_textfieldCost.insets = new Insets(0, 0, 5, 5);
		gbc_textfieldCost.fill = GridBagConstraints.HORIZONTAL;
		gbc_textfieldCost.gridx = 0;
		gbc_textfieldCost.gridy = 1;
		topPanel.add(labelCost, gbc_textfieldCost);
		
		JFormattedTextField textfieldCostEdit = new JFormattedTextField(NumberFormat.getNumberInstance());
		GridBagConstraints gbc_textfieldCostEdit = new GridBagConstraints();
		gbc_textfieldCostEdit.insets = new Insets(0, 0, 5, 5);
		gbc_textfieldCostEdit.fill = GridBagConstraints.HORIZONTAL;
		gbc_textfieldCostEdit.gridx = 1;
		gbc_textfieldCostEdit.gridy = 1;
		topPanel.add(textfieldCostEdit, gbc_textfieldCostEdit);
		textfieldCostEdit.setColumns(10);
		
		JTextField textFieldTimeunit = new JTextField();
		textFieldTimeunit.setText("per");
		textFieldTimeunit.setEditable(false);
		GridBagConstraints gbc_textFieldTimeunit = new GridBagConstraints();
		gbc_textFieldTimeunit.insets = new Insets(0, 0, 5, 5);
		gbc_textFieldTimeunit.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldTimeunit.gridx = 2;
		gbc_textFieldTimeunit.gridy = 1;
		topPanel.add(textFieldTimeunit, gbc_textFieldTimeunit);
		textFieldTimeunit.setColumns(10);
		
		JComboBox comboboxTimeunit = new JComboBox();
		GridBagConstraints gbc_comboboxTimeunit = new GridBagConstraints();
		gbc_comboboxTimeunit.insets = new Insets(0, 0, 5, 5);
		gbc_comboboxTimeunit.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboboxTimeunit.gridx = 3;
		gbc_comboboxTimeunit.gridy = 1;
		topPanel.add(comboboxTimeunit, gbc_comboboxTimeunit);
		
		JLabel labelTimetable = new JLabel();
		labelTimetable.setText("Default Timetable");
		GridBagConstraints gbc_textfieldTimetable = new GridBagConstraints();
		gbc_textfieldTimetable.insets = new Insets(0, 0, 5, 5);
		gbc_textfieldTimetable.fill = GridBagConstraints.HORIZONTAL;
		gbc_textfieldTimetable.gridx = 0;
		gbc_textfieldTimetable.gridy = 2;
		topPanel.add(labelTimetable, gbc_textfieldTimetable);
		
		JComboBox comboboxTimetable = new JComboBox();
		GridBagConstraints gbc_comboboxTimetable = new GridBagConstraints();
		gbc_comboboxTimetable.gridwidth = 3;
		gbc_comboboxTimetable.insets = new Insets(0, 0, 5, 5);
		gbc_comboboxTimetable.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboboxTimetable.gridx = 1;
		gbc_comboboxTimetable.gridy = 2;
		topPanel.add(comboboxTimetable, gbc_comboboxTimetable);
		
		JPanel bottomPanel = new JPanel();
		setRightComponent(bottomPanel);
		GridBagLayout gbl_bottomPanel = new GridBagLayout();
		bottomPanel.setLayout(gbl_bottomPanel);
		
		JLabel labelInstancesTitle = new JLabel();
		labelInstancesTitle.setText("Instances");
		GridBagConstraints gbc_textfieldInstancesTitle = new GridBagConstraints();
		gbc_textfieldInstancesTitle.insets = new Insets(0, 0, 5, 0);
		gbc_textfieldInstancesTitle.gridx = 0;
		gbc_textfieldInstancesTitle.gridy = 0;
		gbc_textfieldInstancesTitle.weighty = 0;
		gbc_textfieldInstancesTitle.weightx = 1;
		gbc_textfieldInstancesTitle.fill = GridBagConstraints.HORIZONTAL;
		bottomPanel.add(labelInstancesTitle, gbc_textfieldInstancesTitle);
		
		ListChooserPanel listpanelInstances = new ListChooserPanel(){

			@Override
			public void onDelete(ComponentHolder toDel) {
				// TODO Auto-generated method stub
				System.out.println("Deleted "+toDel);
			}

			@Override
			public ComponentHolder onCreate() {
				// TODO Auto-generated method stub
				return null;
			}
			
		};
		GridBagConstraints gbc_listpanelInstances = new GridBagConstraints();
		gbc_listpanelInstances.anchor = GridBagConstraints.PAGE_START;
		gbc_listpanelInstances.gridx = 0;
		gbc_listpanelInstances.gridy = 1;
		gbc_listpanelInstances.fill = GridBagConstraints.BOTH;
		gbc_listpanelInstances.weighty = 1;
		gbc_listpanelInstances.weightx = 1;
		bottomPanel.add(listpanelInstances, gbc_listpanelInstances);
		
		listpanelInstances.add(new ComponentHolder() {
			InstancePanel p = new InstancePanel();
			@Override
			public Component getComponent() {
				return p;
			}
			@Override
			public String toString(){
				return "An Instance";
			}
		});
		
	}

}
