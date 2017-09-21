package de.hpi.bpt.scylla.GUI.GlobalConfigurationPane;

import java.awt.AWTKeyStroke;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;

import de.hpi.bpt.scylla.GUI.FormManager;
import de.hpi.bpt.scylla.GUI.InsertRemoveListener;
import de.hpi.bpt.scylla.GUI.ListChooserPanel;
import de.hpi.bpt.scylla.GUI.ScyllaGUI;
import de.hpi.bpt.scylla.GUI.ListChooserPanel.ComponentHolder;
import de.hpi.bpt.scylla.creation.GlobalConfiguration.GlobalConfigurationCreator.ResourceType;
import de.hpi.bpt.scylla.creation.GlobalConfiguration.GlobalConfigurationCreator.ResourceType.ResourceInstance;

@SuppressWarnings("serial")
public class ResourcePanel extends JSplitPane{
	private JSpinner spinnerQuantity;
	private JFormattedTextField textfieldCost;
	private JComboBox<TimeUnit> comboboxTimeunit;
	private JComboBox<String> comboboxTimetable;
	
	private ListChooserPanel listpanelInstances;
	private ResourceType resourceType;
	
	private FormManager formulaManager;
	private List<InstancePanel> instances;

	public ResourcePanel(FormManager fm) {
		setEnabled(false);
		setOrientation(JSplitPane.VERTICAL_SPLIT);
		formulaManager = fm;
		instances = new ArrayList<InstancePanel>();
		
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
		
		spinnerQuantity = new JSpinner();
		spinnerQuantity.setModel(new SpinnerNumberModel(new Integer(0), new Integer(0), null, new Integer(1)));
		//((JSpinner.DefaultEditor) spinnerQuantity.getEditor()).getTextField().setColumns(2);
		spinnerQuantity.addChangeListener((ChangeEvent e)->{
			if(formulaManager.isChangeFlag())return;
			resourceType.setDefaultQuantity((Integer)spinnerQuantity.getValue());
			formulaManager.setSaved(false);
		});
		GridBagConstraints gbc_spinnerQuantity = new GridBagConstraints();
		gbc_spinnerQuantity.gridwidth = 1;
		gbc_spinnerQuantity.fill = GridBagConstraints.BOTH;
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
		
		textfieldCost = new JFormattedTextField(new NoNegativeDoubleFormat());
		textfieldCost.getDocument().addDocumentListener(new InsertRemoveListener((DocumentEvent e)->{
			if(formulaManager.isChangeFlag())return;
			try{
				resourceType.setDefaultCost(Double.parseDouble(textfieldCost.getText()));
				notifyDefaultChanges();
				formulaManager.setSaved(false);
			}catch(NumberFormatException exc){}
		}));
		GridBagConstraints gbc_textfieldCostEdit = new GridBagConstraints();
		gbc_textfieldCostEdit.insets = new Insets(0, 0, 5, 5);
		gbc_textfieldCostEdit.fill = GridBagConstraints.HORIZONTAL;
		gbc_textfieldCostEdit.gridx = 1;
		gbc_textfieldCostEdit.gridy = 1;
		topPanel.add(textfieldCost, gbc_textfieldCostEdit);
		textfieldCost.setColumns(10);
		
		JLabel textFieldTimeunit = new JLabel();
		textFieldTimeunit.setText("per");
		GridBagConstraints gbc_textFieldTimeunit = new GridBagConstraints();
		gbc_textFieldTimeunit.insets = new Insets(0, 5+20, 5, 5+25);
		gbc_textFieldTimeunit.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldTimeunit.gridx = 2;
		gbc_textFieldTimeunit.gridy = 1;
		topPanel.add(textFieldTimeunit, gbc_textFieldTimeunit);
		
		comboboxTimeunit = new JComboBox<TimeUnit>(TimeUnit.values());
		comboboxTimeunit.addItemListener((ItemEvent e)->{
			if(e.getStateChange() != ItemEvent.SELECTED)return;
			if(formulaManager.isChangeFlag())return;
			resourceType.setDefaultTimeUnit((TimeUnit) comboboxTimeunit.getSelectedItem());
			notifyDefaultChanges();
			formulaManager.setSaved(false);
		});
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
		
		comboboxTimetable = new JComboBox<String>(formulaManager.getTimetables().toArray(new String[formulaManager.getTimetables().size()]));
		fm.getTimetableObserverList().add(comboboxTimetable);
		comboboxTimetable.addItemListener((ItemEvent e)->{
			if(e.getStateChange() != ItemEvent.SELECTED)return;
			if(formulaManager.isChangeFlag())return;
			resourceType.setDefaultTimetableId((String) comboboxTimetable.getSelectedItem());
			notifyDefaultChanges();
			formulaManager.setSaved(false);
		});
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
		labelInstancesTitle.setBackground(ScyllaGUI.ColorField0);
		labelInstancesTitle.setForeground(ScyllaGUI.TITLEFONT_COLOR);
		labelInstancesTitle.setFont(ScyllaGUI.TITLEFONT);
		labelInstancesTitle.setOpaque(true);
		GridBagConstraints gbc_textfieldInstancesTitle = new GridBagConstraints();
		gbc_textfieldInstancesTitle.insets = new Insets(0, 0, 5, 0);
		gbc_textfieldInstancesTitle.gridx = 0;
		gbc_textfieldInstancesTitle.gridy = 0;
		gbc_textfieldInstancesTitle.weighty = 0;
		gbc_textfieldInstancesTitle.weightx = 1;
		gbc_textfieldInstancesTitle.fill = GridBagConstraints.HORIZONTAL;
		bottomPanel.add(labelInstancesTitle, gbc_textfieldInstancesTitle);
		
		listpanelInstances = new ListChooserPanel(){

			@Override
			public void onDelete(ComponentHolder toDel) {
				resourceType.removeInstance(toDel.toString());
				instances.remove(toDel.getComponent());
				formulaManager.setSaved(false);
			}

			@Override
			public ComponentHolder onCreate() {
				formulaManager.setSaved(false);
				return newInstance(
						resourceType.addInstance("<enter name>")
				);
			}
			
		};
		GridBagConstraints gbc_listpanelInstances = new GridBagConstraints();
		gbc_listpanelInstances.anchor = GridBagConstraints.PAGE_START;
		gbc_listpanelInstances.gridx = 0;
		gbc_listpanelInstances.gridy = 1;
		gbc_listpanelInstances.fill = GridBagConstraints.HORIZONTAL;
		gbc_listpanelInstances.weighty = 1;
		gbc_listpanelInstances.weightx = 1;
		bottomPanel.add(listpanelInstances, gbc_listpanelInstances);
		
		Set<AWTKeyStroke> forwardKeys = new HashSet<AWTKeyStroke>(getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
		forwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
		setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, forwardKeys);
		
	}
	


	public void setResourceType(ResourceType res){
		formulaManager.setChangeFlag(true);
		resourceType = res;
		spinnerQuantity.setValue(Integer.parseInt(res.getDefaultQuantity()));
		textfieldCost.setText(res.getDefaultCost());
		comboboxTimeunit.setSelectedItem(TimeUnit.valueOf(res.getDefaultTimeUnit()));
		comboboxTimetable.setSelectedItem(res.getDefaultTimetableId());
		formulaManager.setChangeFlag(false);
		importInstances(res);
	}
	
	private void importInstances(ResourceType res){
		for(ResourceInstance inst : res.getResourceInstances()){
			listpanelInstances.add(newInstance(inst));
		}
	}
	
	private ComponentHolder newInstance(ResourceInstance inst) {
		return new ComponentHolder() {
			InstancePanel p = new InstancePanel(formulaManager);
			{
				p.setInstance(inst);
				instances.add(p);
			}
			@Override
			public Component getComponent() {
				return p;
			}
			@Override
			public String toString(){
				return inst.getName();
			}
			@Override
			public void setName(String s){
				String t = s;
				int i = 2;
				if(!inst.getName().equals(s)){
					while(resourceType.getInstance(t) != null){
						t = s+"("+i+")";
						i++;
					}
				}
				inst.setName(t);
				formulaManager.setSaved(false);
			}
		};
	}
	
	private void notifyDefaultChanges(){
		for(InstancePanel i : instances){
			i.defaultsChanged();
		}
	}

}