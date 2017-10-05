package de.hpi.bpt.scylla.GUI.GlobalConfigurationPane;

import java.awt.AWTKeyStroke;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;

import de.hpi.bpt.scylla.GUI.FormManager;
import de.hpi.bpt.scylla.GUI.InsertRemoveListener;
import de.hpi.bpt.scylla.GUI.ScalingCheckBoxIcon;
import de.hpi.bpt.scylla.GUI.ScyllaGUI;
import de.hpi.bpt.scylla.creation.GlobalConfiguration.GlobalConfigurationCreator.ResourceType.ResourceInstance;

/**
 * Display class for resource instances
 * @author Leon Bein
 *
 */
@SuppressWarnings("serial")
public class InstancePanel extends JPanel{
	
	//Input components
	private JFormattedTextField textfieldCost;
	private JComboBox<TimeUnit> comboboxTimeunit;
	private JComboBox<String> comboboxTimetable;
	private JCheckBox checkboxDefaultTimetable;
	private JCheckBox checkboxDefaultCost;
	
	/**Wrapper for the instance that is displayed with this panel*/
	private ResourceInstance instance;
	
	/**Central object for managing the whole form*/
	private FormManager formManager;

	/**
	 * Constructor
	 * @param fm : The formmanager
	 */
	public InstancePanel(FormManager fm) {
		formManager = fm;
		GridBagLayout gbl_topPanel = new GridBagLayout();
		gbl_topPanel.columnWeights = new double[]{0, 1.0, 0, 1.0, 0.0};
		setLayout(gbl_topPanel);
		
		//Cost label
		JLabel labelCost = new JLabel();
		labelCost.setText("Cost");
		GridBagConstraints gbc_labelCost = new GridBagConstraints();
		gbc_labelCost.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
		gbc_labelCost.fill = GridBagConstraints.HORIZONTAL;
		gbc_labelCost.gridx = 0;
		gbc_labelCost.gridy = 0;
		add(labelCost, gbc_labelCost);
		
		//Cost input field
		textfieldCost = new JFormattedTextField(new NoNegativeDoubleFormat());
		textfieldCost.getDocument().addDocumentListener(new InsertRemoveListener((DocumentEvent e)->{
			if(formManager.isChangeFlag())return;
			try{
				instance.setCost(Double.parseDouble(textfieldCost.getText()));
				formManager.setSaved(false);
			}catch(NumberFormatException exc){}
		}));
		GridBagConstraints gbc_textfieldCost = new GridBagConstraints();
		gbc_textfieldCost.insets = new Insets(ScyllaGUI.STDINSET, 0, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
		gbc_textfieldCost.fill = GridBagConstraints.HORIZONTAL;
		gbc_textfieldCost.gridx = 1;
		gbc_textfieldCost.gridy = 0;
		add(textfieldCost, gbc_textfieldCost);
		textfieldCost.setColumns(10);
		
		//Timeunit label
		JLabel labelTimeunit = new JLabel();
		labelTimeunit.setText("per");
		GridBagConstraints gbc_labelTimeunit = new GridBagConstraints();
		gbc_labelTimeunit.insets = new Insets(0, ScyllaGUI.STDINSET+(int)(10.0*ScyllaGUI.SCALE),ScyllaGUI.STDINSET, ScyllaGUI.STDINSET+(int)(10.0*ScyllaGUI.SCALE));
		gbc_labelTimeunit.fill = GridBagConstraints.HORIZONTAL;
		gbc_labelTimeunit.gridx = 2;
		gbc_labelTimeunit.gridy = 0;
		add(labelTimeunit, gbc_labelTimeunit);
		
		//Timeunit input combobox
		comboboxTimeunit = new JComboBox<TimeUnit>(TimeUnit.values());
		comboboxTimeunit.addItemListener((ItemEvent e)->{
			if(formManager.isChangeFlag())return;
			instance.setTimeUnit((TimeUnit) comboboxTimeunit.getSelectedItem());
			formManager.setSaved(false);
		});
		GridBagConstraints gbc_comboboxTimeunit = new GridBagConstraints();
		gbc_comboboxTimeunit.insets = new Insets(0, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET , ScyllaGUI.STDINSET);
		gbc_comboboxTimeunit.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboboxTimeunit.gridx = 3;
		gbc_comboboxTimeunit.gridy = 0;
		add(comboboxTimeunit, gbc_comboboxTimeunit);
		
		//Checkbox whether to use the resources default cost
		checkboxDefaultCost = new JCheckBox("use default");
		checkboxDefaultCost.addItemListener((ItemEvent e)->{
			if(checkboxDefaultCost.isSelected()){
				defaultsChanged();//Fire event to adapt to the defaults
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
		
		//Timetable label
		JLabel labelTimetable = new JLabel();
		labelTimetable.setText("Timetable");
		GridBagConstraints gbc_labelTimetable = new GridBagConstraints();
		gbc_labelTimetable.insets = new Insets(0, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
		gbc_labelTimetable.fill = GridBagConstraints.HORIZONTAL;
		gbc_labelTimetable.gridx = 0;
		gbc_labelTimetable.gridy = 1;
		add(labelTimetable, gbc_labelTimetable);
		
		//Timetable input combobox
		comboboxTimetable = new JComboBox<String>(formManager.getTimetables().toArray(new String[formManager.getTimetables().size()]));
		comboboxTimetable.addItemListener((ItemEvent e)->{
			if(formManager.isChangeFlag())return;
			instance.setTimetableId((String) comboboxTimetable.getSelectedItem());
			formManager.setSaved(false);
		});
		formManager.getTimetableObserverList().add(comboboxTimetable);
		GridBagConstraints gbc_comboboxTimetable = new GridBagConstraints();
		gbc_comboboxTimetable.gridwidth = 3;
		gbc_comboboxTimetable.insets = new Insets(0, 0, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
		gbc_comboboxTimetable.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboboxTimetable.gridx = 1;
		gbc_comboboxTimetable.gridy = 1;
		add(comboboxTimetable, gbc_comboboxTimetable);
		
		//Checkbox whether to use the resources default timetable
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
		
		//Activate focus traversal by pressing enter
		Set<AWTKeyStroke> forwardKeys = new HashSet<AWTKeyStroke>(getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
		forwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
		setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, forwardKeys);
	}

	/**
	 * Sets the instance:
	 * Imports all attributes into the input fields if given, otherwise activate "use defaults" checkboxes
	 * @param inst : Wrapper of the instance to import
	 */
	public void setInstance(ResourceInstance inst) {
		formManager.setChangeFlag(true);
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
		formManager.setChangeFlag(false);
	}
	
	/**
	 * Adapts the input contents to the resource defaults if the "use defaults" checkboxes are activated
	 */
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
