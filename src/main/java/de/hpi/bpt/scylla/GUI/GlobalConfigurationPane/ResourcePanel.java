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

import de.hpi.bpt.scylla.GUI.InsertRemoveListener;
import de.hpi.bpt.scylla.GUI.NoNegativeDoubleFormat;
import de.hpi.bpt.scylla.GUI.ExtendedListChooserPanel;
import de.hpi.bpt.scylla.GUI.ScyllaGUI;
import de.hpi.bpt.scylla.GUI.ListChooserPanel.ComponentHolder;
import de.hpi.bpt.scylla.creation.GlobalConfiguration.GlobalConfigurationCreator.ResourceType;
import de.hpi.bpt.scylla.creation.GlobalConfiguration.GlobalConfigurationCreator.ResourceType.ResourceInstance;

/**
 * Display class for resources
 * @author Leon Bein
 *
 */
@SuppressWarnings("serial")
public class ResourcePanel extends JSplitPane{
	//Inputs
	private JSpinner spinnerQuantity;
	private JFormattedTextField textfieldCost;
	private JComboBox<TimeUnit> comboboxTimeunit;
	private JComboBox<String> comboboxTimetable;
	
	/**Panel containing the panels corresponding to this resource*/
	private ExtendedListChooserPanel listpanelInstances;
	/**Additional list of all instances in order to retain access*/
	private List<InstancePanel> instances;
	
	/**Wrapper for this resource*/
	private ResourceType resourceType;
	
	/**Central object for managing the whole form*/
	private GCFormManager formManager;

	/**
	 * Constructor
	 * @param fm : The objects formmanager
	 */
	public ResourcePanel(GCFormManager fm) {
		setEnabled(false);
		setOrientation(JSplitPane.VERTICAL_SPLIT);
		formManager = fm;
		instances = new ArrayList<InstancePanel>();
		
		//--- Resource Information Panel ---
		JPanel topPanel = new JPanel();
		setLeftComponent(topPanel);
		GridBagLayout gbl_topPanel = new GridBagLayout();
		gbl_topPanel.columnWeights = new double[]{0, 1.0, 0, 1.0};
		topPanel.setLayout(gbl_topPanel);
		
		//Label for quantity
		JLabel labelQuantity = new JLabel();
		labelQuantity.setText("Quantity");
		GridBagConstraints gbc_labelQuantity = new GridBagConstraints();
		gbc_labelQuantity.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
		gbc_labelQuantity.fill = GridBagConstraints.HORIZONTAL;
		gbc_labelQuantity.gridx = 0;
		gbc_labelQuantity.gridy = 0;
		topPanel.add(labelQuantity, gbc_labelQuantity);
		
		//Spinner for quantity
		spinnerQuantity = new JSpinner();
		spinnerQuantity.setModel(new SpinnerNumberModel(new Integer(0), new Integer(0), null, new Integer(1)));
		spinnerQuantity.addChangeListener((ChangeEvent e)->{
			if(formManager.isChangeFlag())return;
			resourceType.setDefaultQuantity((Integer)spinnerQuantity.getValue());
			formManager.setSaved(false);
		});
		GridBagConstraints gbc_spinnerQuantity = new GridBagConstraints();
		gbc_spinnerQuantity.gridwidth = 1;
		gbc_spinnerQuantity.fill = GridBagConstraints.BOTH;
		gbc_spinnerQuantity.insets = new Insets(ScyllaGUI.STDINSET, 0, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
		gbc_spinnerQuantity.gridx = 1;
		gbc_spinnerQuantity.gridy = 0;
		topPanel.add(spinnerQuantity, gbc_spinnerQuantity);
		
		//Cost label
		JLabel labelCost = new JLabel();
		labelCost.setText("Cost");
		GridBagConstraints gbc_labelCost = new GridBagConstraints();
		gbc_labelCost.insets = new Insets(0, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
		gbc_labelCost.fill = GridBagConstraints.HORIZONTAL;
		gbc_labelCost.gridx = 0;
		gbc_labelCost.gridy = 1;
		topPanel.add(labelCost, gbc_labelCost);
		
		//Input field for cost
		textfieldCost = new JFormattedTextField(new NoNegativeDoubleFormat());
		textfieldCost.getDocument().addDocumentListener(new InsertRemoveListener((DocumentEvent e)->{
			if(formManager.isChangeFlag())return;
			try{
				resourceType.setDefaultCost(Double.parseDouble(textfieldCost.getText()));
				notifyDefaultChanges();
				formManager.setSaved(false);
			}catch(NumberFormatException exc){}
		}));
		GridBagConstraints gbc_textfieldCost = new GridBagConstraints();
		gbc_textfieldCost.insets = new Insets(0, 0, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
		gbc_textfieldCost.fill = GridBagConstraints.HORIZONTAL;
		gbc_textfieldCost.gridx = 1;
		gbc_textfieldCost.gridy = 1;
		topPanel.add(textfieldCost, gbc_textfieldCost);
		textfieldCost.setColumns(10);
		
		//Timeunit label
		JLabel labelTimeunit = new JLabel();
		labelTimeunit.setText("per");
		GridBagConstraints gbc_labelTimeunit = new GridBagConstraints();
		gbc_labelTimeunit.insets = new Insets(0, ScyllaGUI.STDINSET+(int)(10.0*ScyllaGUI.SCALE),ScyllaGUI.STDINSET, ScyllaGUI.STDINSET+(int)(10.0*ScyllaGUI.SCALE));
		gbc_labelTimeunit.fill = GridBagConstraints.HORIZONTAL;
		gbc_labelTimeunit.gridx = 2;
		gbc_labelTimeunit.gridy = 1;
		topPanel.add(labelTimeunit, gbc_labelTimeunit);
		
		//Timeunit input combobox
		comboboxTimeunit = new JComboBox<TimeUnit>(TimeUnit.values());
		comboboxTimeunit.addItemListener((ItemEvent e)->{
			if(e.getStateChange() != ItemEvent.SELECTED)return;
			if(formManager.isChangeFlag())return;
			resourceType.setDefaultTimeUnit((TimeUnit) comboboxTimeunit.getSelectedItem());
			notifyDefaultChanges();
			formManager.setSaved(false);
		});
		GridBagConstraints gbc_comboboxTimeunit = new GridBagConstraints();
		gbc_comboboxTimeunit.insets = new Insets(0, ScyllaGUI.STDINSET,ScyllaGUI.STDINSET,ScyllaGUI.STDINSET);
		gbc_comboboxTimeunit.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboboxTimeunit.gridx = 3;
		gbc_comboboxTimeunit.gridy = 1;
		topPanel.add(comboboxTimeunit, gbc_comboboxTimeunit);
		
		//Timetable label
		JLabel labelTimetable = new JLabel();
		labelTimetable.setText("Default Timetable");
		GridBagConstraints gbc_textfieldTimetable = new GridBagConstraints();
		gbc_textfieldTimetable.insets = new Insets(0, ScyllaGUI.STDINSET,ScyllaGUI.STDINSET,ScyllaGUI.STDINSET);
		gbc_textfieldTimetable.fill = GridBagConstraints.HORIZONTAL;
		gbc_textfieldTimetable.gridx = 0;
		gbc_textfieldTimetable.gridy = 2;
		topPanel.add(labelTimetable, gbc_textfieldTimetable);
		
		//Timetable input combobox
		comboboxTimetable = new JComboBox<String>(formManager.getTimetables().toArray(new String[formManager.getTimetables().size()]));
		fm.getTimetableObserverList().add(comboboxTimetable);
		comboboxTimetable.addItemListener((ItemEvent e)->{
			if(e.getStateChange() != ItemEvent.SELECTED)return;
			if(formManager.isChangeFlag())return;
			resourceType.setDefaultTimetableId((String) comboboxTimetable.getSelectedItem());
			notifyDefaultChanges();
			formManager.setSaved(false);
		});
		GridBagConstraints gbc_comboboxTimetable = new GridBagConstraints();
		gbc_comboboxTimetable.gridwidth = 3;
		gbc_comboboxTimetable.insets = new Insets(0, 0, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
		gbc_comboboxTimetable.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboboxTimetable.gridx = 1;
		gbc_comboboxTimetable.gridy = 2;
		topPanel.add(comboboxTimetable, gbc_comboboxTimetable);
		
		//--- General instance panel ---
		JPanel bottomPanel = new JPanel();
		setRightComponent(bottomPanel);
		GridBagLayout gbl_bottomPanel = new GridBagLayout();
		bottomPanel.setLayout(gbl_bottomPanel);
		
		//Instance label
		JLabel labelInstancesTitle = new JLabel();
		labelInstancesTitle.setText("Instances");
		labelInstancesTitle.setBackground(ScyllaGUI.ColorField0);
		labelInstancesTitle.setForeground(ScyllaGUI.TITLEFONT_COLOR);
		labelInstancesTitle.setFont(ScyllaGUI.TITLEFONT);
		labelInstancesTitle.setOpaque(true);
		GridBagConstraints gbc_textfieldInstancesTitle = new GridBagConstraints();
		gbc_textfieldInstancesTitle.insets = new Insets(0, 0, ScyllaGUI.STDINSET, 0);
		gbc_textfieldInstancesTitle.gridx = 0;
		gbc_textfieldInstancesTitle.gridy = 0;
		gbc_textfieldInstancesTitle.weighty = 0;
		gbc_textfieldInstancesTitle.weightx = 1;
		gbc_textfieldInstancesTitle.fill = GridBagConstraints.HORIZONTAL;
		bottomPanel.add(labelInstancesTitle, gbc_textfieldInstancesTitle);
		
		//Instance list panel
		listpanelInstances = new ExtendedListChooserPanel(){

			@Override
			public void onDelete(ComponentHolder toDel) {
				resourceType.removeInstance(toDel.toString());
				instances.remove(toDel.getComponent());
				formManager.setSaved(false);
			}

			@Override
			public ComponentHolder onCreate() {
				formManager.setSaved(false);
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
		
		//Activate focus traversal by enter
		Set<AWTKeyStroke> forwardKeys = new HashSet<AWTKeyStroke>(getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
		forwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
		setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, forwardKeys);
		
	}
	

	/**
	 * Sets the resources type:
	 * Imports all attributes into the input fields and all the instances
	 * @param res : Wrapper for the resource to import
	 */
	public void setResourceType(ResourceType res){
		formManager.setChangeFlag(true);
		resourceType = res;
		spinnerQuantity.setValue(Integer.parseInt(res.getDefaultQuantity()));
		textfieldCost.setText(res.getDefaultCost());
		comboboxTimeunit.setSelectedItem(TimeUnit.valueOf(res.getDefaultTimeUnit()));
		comboboxTimetable.setSelectedItem(res.getDefaultTimetableId());
		formManager.setChangeFlag(false);
		importInstances(res);
	}
	
	/**
	 * Imports all instances of a given resource
	 * @param res : Wrapper of the resource whose instances shall be imported
	 */
	private void importInstances(ResourceType res){
		for(ResourceInstance inst : res.getResourceInstances()){
			listpanelInstances.add(newInstance(inst));
		}
	}
	
	/**
	 * Creates a panel for a given instance
	 * @param res : Wrapper of the given instance
	 * @return : A new componentholder for an InstancePanel object
	 * @see {@link InstancePanel}
	 */
	private ComponentHolder newInstance(ResourceInstance inst) {
		return new ComponentHolder() {
			InstancePanel p = new InstancePanel(formManager);
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
				//If the name has changed, but is already given,
				//it will be serially numbered, and the first free number is chosen
				int i = 2;
				if(!inst.getName().equals(s)){
					while(resourceType.getInstance(t) != null){
						t = s+"("+i+")";
						i++;
					}
				}
				inst.setName(t);
				formManager.setSaved(false);
			}
		};
	}
	
	/**
	 * Notify all instance panels, that the defaults have changed and they have to update their values
	 */
	private void notifyDefaultChanges(){
		for(InstancePanel i : instances){
			i.defaultsChanged();
		}
	}

}
