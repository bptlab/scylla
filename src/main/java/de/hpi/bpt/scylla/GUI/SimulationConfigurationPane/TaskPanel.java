package de.hpi.bpt.scylla.GUI.SimulationConfigurationPane;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;

import de.hpi.bpt.scylla.GUI.FormManager;
import de.hpi.bpt.scylla.GUI.ListChooserPanel.ComponentHolder;
import de.hpi.bpt.scylla.GUI.ScyllaGUI;
import de.hpi.bpt.scylla.GUI.GlobalConfigurationPane.GCFormManager.ResourceObserver;
import de.hpi.bpt.scylla.creation.GlobalConfiguration.GlobalConfigurationCreator;
import de.hpi.bpt.scylla.creation.GlobalConfiguration.GlobalConfigurationCreator.ResourceType;
import de.hpi.bpt.scylla.creation.SimulationConfiguration.Distribution;
import de.hpi.bpt.scylla.creation.SimulationConfiguration.Distribution.DistributionType;
import de.hpi.bpt.scylla.creation.SimulationConfiguration.ResourceAssignment;
import de.hpi.bpt.scylla.creation.SimulationConfiguration.Task;

@SuppressWarnings("serial")
public class TaskPanel extends JPanel implements ComponentHolder,ResourceObserver {

	private JComboBox<TimeUnit> comboboxTimeunit;
	private JComboBox<DistributionType> comboboxDistribution;
	private GridBagConstraints gbc_panelDistribution;
	private Component panelDistribution;
	
	private FormManager fm;
	private GlobalConfigurationCreator gcc;
	private Task task;
	private JPanel panelResources;
	private JComboBox<String> comboboxAssign;
	private JButton buttonAssign;
	private GridBagConstraints gbc_buttonAssign;
	private JLabel labelErrorAssign;
	private Set<AssignerPanel> assignerPanels;

	/**
	 * Create the panel.
	 */
	public TaskPanel(Task t, FormManager f, GlobalConfigurationCreator gc) {
		
		fm = f;
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWeights = new double[] {0,0.5, 0.5,2};
		setLayout(gridBagLayout);
		
		//Duration label
		JLabel labelDuration = new JLabel("Duration");
		labelDuration.setBackground(ScyllaGUI.ColorField0);
		labelDuration.setForeground(ScyllaGUI.TITLEFONT_COLOR);
		labelDuration.setFont(ScyllaGUI.TITLEFONT);
		labelDuration.setOpaque(true);
		GridBagConstraints gbc_labelDuration = new GridBagConstraints();
		gbc_labelDuration.gridwidth = 4;
		gbc_labelDuration.insets = new Insets(ScyllaGUI.STDINSET,0,ScyllaGUI.STDINSET,0);
		gbc_labelDuration.fill = GridBagConstraints.HORIZONTAL;
		gbc_labelDuration.anchor = GridBagConstraints.PAGE_START;
		gbc_labelDuration.gridx = 0;
		gbc_labelDuration.gridy = 0;
		gbc_labelDuration.weightx = 1;
		add(labelDuration, gbc_labelDuration);
		
		//Distribution label
		JLabel labelDistribution = new JLabel("Distribution");
		GridBagConstraints gbc_labelDistribution = new GridBagConstraints();
		gbc_labelDistribution.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
		gbc_labelDistribution.fill = GridBagConstraints.HORIZONTAL;
		gbc_labelDistribution.gridx = 0;
		gbc_labelDistribution.gridy = 1;
		add(labelDistribution, gbc_labelDistribution);
		
		//Distribution Panel layout constraints and empty "panel"
		gbc_panelDistribution = new GridBagConstraints();
		gbc_panelDistribution.gridwidth = 2;
		gbc_panelDistribution.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET*32, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET*2);
		gbc_panelDistribution.fill = GridBagConstraints.BOTH;
		gbc_panelDistribution.anchor = GridBagConstraints.WEST;
		gbc_panelDistribution.gridx = 0;
		gbc_panelDistribution.gridy = 2;
		panelDistribution = new JLabel(" ");
		add(panelDistribution, gbc_panelDistribution);
		
		//Distribution combobox
		comboboxDistribution = new JComboBox<DistributionType>(DistributionType.values());
//		comboboxDistribution = new JComboBox<DistributionType>();
		comboboxDistribution.addItemListener((ItemEvent e)->{
			if(e.getStateChange() != ItemEvent.SELECTED)return;
			if(fm.isChangeFlag())return;
			DistributionType type = (DistributionType) comboboxDistribution.getSelectedItem();
			if(type != null){
				Distribution d = Distribution.create(type);
				setPanelDistribution(d);
				getParent().getParent().revalidate();
				repaint();
				task.setDurationDistribution(d);
				fm.setSaved(false);
			}
		});
		comboboxDistribution.setSelectedIndex(-1);
		GridBagConstraints gbc_comboboxDistribution = new GridBagConstraints();
		gbc_comboboxDistribution.gridwidth = 2;
		gbc_comboboxDistribution.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
		gbc_comboboxDistribution.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboboxDistribution.gridx = 1;
		gbc_comboboxDistribution.gridy = 1;
		add(comboboxDistribution, gbc_comboboxDistribution);
		
		//Empty fill label for more space
		JLabel labelFill = new JLabel(" ");
		GridBagConstraints gbc_labelFill = new GridBagConstraints();
		gbc_labelFill.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, 0);
		gbc_labelFill.fill = GridBagConstraints.HORIZONTAL;
		gbc_labelFill.gridx = 3;
		gbc_labelFill.gridy = 1;
		add(labelFill, gbc_labelFill);
		
		//Time unit label
		JLabel labelTimeunit = new JLabel("Timeunit");
		GridBagConstraints gbc_labelTimeunit = new GridBagConstraints();
		gbc_labelTimeunit.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
		gbc_labelTimeunit.fill = GridBagConstraints.HORIZONTAL;
		gbc_labelTimeunit.gridx = 0;
		gbc_labelTimeunit.gridy = 3;
		add(labelTimeunit, gbc_labelTimeunit);
		
		//Time unit combobox
		comboboxTimeunit = new JComboBox<TimeUnit>(TimeUnit.values());
//		comboboxTimeunit = new JComboBox<TimeUnit>();
		comboboxTimeunit.addItemListener((ItemEvent e)->{
			if(e.getStateChange() != ItemEvent.SELECTED)return;
			if(fm.isChangeFlag())return;
			task.setDurationTimeUnit((TimeUnit) comboboxTimeunit.getSelectedItem());
			fm.setSaved(false);
		});
		GridBagConstraints gbc_comboboxTimeunit = new GridBagConstraints();
		gbc_comboboxTimeunit.gridwidth = 2;
		gbc_comboboxTimeunit.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
		gbc_comboboxTimeunit.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboboxTimeunit.gridx = 1;
		gbc_comboboxTimeunit.gridy = 3;
		add(comboboxTimeunit, gbc_comboboxTimeunit);
		
		//Resource label
		JLabel labelResources = new JLabel("Assigned Resources");
		labelResources.setBackground(ScyllaGUI.ColorField0);
		labelResources.setForeground(ScyllaGUI.TITLEFONT_COLOR);
		labelResources.setFont(ScyllaGUI.TITLEFONT);
		labelResources.setOpaque(true);
		GridBagConstraints gbc_labelResources = new GridBagConstraints();
		gbc_labelResources.gridwidth = 4;
		gbc_labelResources.insets = new Insets(ScyllaGUI.STDINSET,0,ScyllaGUI.STDINSET,0);
		gbc_labelResources.fill = GridBagConstraints.HORIZONTAL;
		gbc_labelResources.anchor = GridBagConstraints.PAGE_START;
		gbc_labelResources.gridx = 0;
		gbc_labelResources.gridy = 4;
		gbc_labelResources.weightx = 1;
		add(labelResources, gbc_labelResources);

		JScrollPane scrollpaneResources = new JScrollPane();
		scrollpaneResources.getVerticalScrollBar().setUnitIncrement(32);
		scrollpaneResources.setPreferredSize(new Dimension(0,getFont().getSize()*10));
		GridBagConstraints gbc_scrollpaneResources = new GridBagConstraints();
		gbc_scrollpaneResources.gridwidth = 4;
		gbc_scrollpaneResources.insets = new Insets(ScyllaGUI.STDINSET,0,ScyllaGUI.STDINSET,0);
		gbc_scrollpaneResources.fill = GridBagConstraints.BOTH;
		gbc_scrollpaneResources.gridx = 0;
		gbc_scrollpaneResources.gridy = 5;
		gbc_scrollpaneResources.weighty = 1;
		add(scrollpaneResources, gbc_scrollpaneResources);
		

		JPanel panelResourcesWrap = new JPanel();
		panelResourcesWrap.setLayout(new GridBagLayout());
		
		panelResources = new JPanel();
		panelResources.setLayout(new BoxLayout(panelResources, BoxLayout.Y_AXIS));
		GridBagConstraints gbc_panelResources = new GridBagConstraints();
		gbc_panelResources.fill = GridBagConstraints.HORIZONTAL;
		gbc_panelResources.weightx = 1;
		gbc_panelResources.weighty = 1;
		gbc_panelResources.anchor = GridBagConstraints.PAGE_START;
		panelResourcesWrap.add(panelResources,gbc_panelResources);
		
		scrollpaneResources.setViewportView(panelResourcesWrap);
		
		JLabel labelAssign = new JLabel("Assign new");
		GridBagConstraints gbc_labelAssign = new GridBagConstraints();
		gbc_labelAssign.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
		gbc_labelAssign.gridx = 0;
		gbc_labelAssign.gridy = 6;
		add(labelAssign, gbc_labelAssign);
		
		comboboxAssign = new JComboBox<String>();
		GridBagConstraints gbc_comboboxAssign = new GridBagConstraints();
		gbc_comboboxAssign.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, 0, ScyllaGUI.STDINSET);
		gbc_comboboxAssign.fill = GridBagConstraints.HORIZONTAL; 
		gbc_comboboxAssign.gridx = 0;
		gbc_comboboxAssign.gridwidth = 2;
		gbc_comboboxAssign.gridy = 7;
		add(comboboxAssign, gbc_comboboxAssign);
		comboboxAssign.setEnabled(false);
		
		buttonAssign = new JButton("assign");
		buttonAssign.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ResourceType type = TaskPanel.this.gcc.getResourceType((String) comboboxAssign.getSelectedItem());
				if(type != null){
					ResourceAssignment res = task.assignResource(type);
					createAssigner(res);
					getParent().getParent().revalidate();
					repaint();
				}
			}
		});
		labelErrorAssign = new JLabel("Cannot assign new resources if no global configuration is specified.");
		labelErrorAssign.setForeground(ScyllaGUI.ERRORFONT_COLOR);
		labelErrorAssign.setFont(ScyllaGUI.DEFAULTFONT);
		
		gbc_buttonAssign = new GridBagConstraints();
		gbc_buttonAssign.anchor = GridBagConstraints.WEST;
		gbc_buttonAssign.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, 0, ScyllaGUI.STDINSET);
		gbc_buttonAssign.gridx = 2;
		gbc_buttonAssign.gridy = 7;
		add(labelErrorAssign, gbc_buttonAssign);
		buttonAssign.setEnabled(false);
		
		assignerPanels = new HashSet<AssignerPanel>();

		if(gc != null)setGcc(gc);
		setTask(t);
		
	}
	
	public void setGcc(GlobalConfigurationCreator gcc) {
		this.gcc = gcc;
		gcc.getResourceObserverList().add(this);
		comboboxAssign.removeAllItems();
		for(ResourceType type : gcc.getResourceTypes()) {
			comboboxAssign.addItem(type.getId());
		}
		buttonAssign.setEnabled(true);
		comboboxAssign.setEnabled(true);
		remove(labelErrorAssign);
		for(AssignerPanel assigner: assignerPanels) {
			String type = assigner.assignment.getId();
			assigner.setErrored(gcc.getResourceType(type) == null);
		}
		add(buttonAssign,gbc_buttonAssign);
	}

	private void setTask(Task t) {
		fm.setChangeFlag(true);
		task = t;
		Distribution d = task.getDurationDistribution();
		if(d != null){
			setPanelDistribution(d);
			comboboxDistribution.setSelectedItem(d.getType());
		}else{
			setPanelDistribution(null);
			comboboxDistribution.setSelectedIndex(-1);
		}
		if(task.getDurationTimeUnit() != null)
			comboboxTimeunit.setSelectedItem(TimeUnit.valueOf(task.getDurationTimeUnit()));
		
		for(String id : t.getResources()) {
			createAssigner(t.getResource(id));
		}

		fm.setChangeFlag(false);
	}

	private void setPanelDistribution(Distribution d) {
		if(panelDistribution != null)remove(panelDistribution);
		if(d != null)panelDistribution = new DistributionPanel(d, fm);
		else panelDistribution = new JLabel(" ");
		add(panelDistribution, gbc_panelDistribution);
	}
	
	private class AssignerPanel extends JPanel{
		
		private ResourceAssignment assignment;
		private JLabel labelError;
		
		private AssignerPanel(ResourceAssignment r) {
			assignment = r;
			GridBagLayout gbl_panel = new GridBagLayout(); 
			gbl_panel.columnWeights = new double[]{1,0,1,1,1,9};
			setLayout(gbl_panel);

			JLabel labelName = new JLabel(r.getName());
			GridBagConstraints gbc_labelName = new GridBagConstraints();
			gbc_labelName.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
			gbc_labelName.gridx = 0;
			gbc_labelName.gridy = 0;
			add(labelName, gbc_labelName);
			
			JLabel labelNumber = new JLabel("Amount:");
			GridBagConstraints gbc_labelNumber = new GridBagConstraints();
			gbc_labelNumber.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
			gbc_labelNumber.gridx = 1;
			gbc_labelNumber.gridy = 0;
			add(labelNumber, gbc_labelNumber);
			
			Integer max = null;
			if(r.getType() != null)max = Integer.valueOf(r.getType().getDefaultQuantity());
			JSpinner spinner = new JSpinner(new SpinnerNumberModel(new Integer(0), new Integer(0), max, new Integer(1)));
			//TODO set maximum to Resource maximum [done] and add resource change listener for that
			spinner.setValue(Integer.parseInt(r.getAmount()));
			spinner.addChangeListener((ChangeEvent e)->{
				if(fm.isChangeFlag())return;
				r.setAmount((Integer)spinner.getValue());
				fm.setSaved(false);
			});
			GridBagConstraints gbc_spinner = new GridBagConstraints();
			gbc_spinner.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
			gbc_spinner.gridx = 2;
			gbc_spinner.gridy = 0;
			gbc_spinner.fill = GridBagConstraints.HORIZONTAL;
			add(spinner, gbc_spinner);
			
			JButton buttonDeassign = new JButton("deassign");
			buttonDeassign.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					task.deassignResource(r.getId());
					panelResources.remove(AssignerPanel.this);
					assignerPanels.remove(AssignerPanel.this);
					TaskPanel.this.getParent().getParent().revalidate();
					TaskPanel.this.repaint();
				}
			});
			GridBagConstraints gbc_buttonDeassign = new GridBagConstraints();
			gbc_buttonDeassign.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
			gbc_buttonDeassign.gridx = 3;
			gbc_buttonDeassign.gridy = 0;
			add(buttonDeassign, gbc_buttonDeassign);
			
			labelError = new JLabel(" ");
			labelError.setForeground(ScyllaGUI.ERRORFONT_COLOR);
			labelError.setFont(ScyllaGUI.DEFAULTFONT);
			GridBagConstraints gbc_labelError = new GridBagConstraints();
			gbc_labelError.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
			gbc_labelError.gridx = 4;
			gbc_labelError.gridy = 0;
			add(labelError, gbc_labelError);
			
			JLabel labelFill = new JLabel(" ");
			GridBagConstraints gbc_labelFill = new GridBagConstraints();
			gbc_labelFill.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
			gbc_labelFill.gridx = 5;
			gbc_labelFill.gridy = 0;
			add(labelFill, gbc_labelFill);
		}
		
		private void setErrored(boolean b) {
			if(!b) labelError.setText(" ");
			else labelError.setText("Warning: The resource type of this assignment does not appear in the current global configuration file.");
		}
	}
	
	private void createAssigner(ResourceAssignment r){
		AssignerPanel assigner = new AssignerPanel(r);
		panelResources.add(assigner);
		assignerPanels.add(assigner);
	}


	@Override
	public Component getComponent() {
		return this;
	}
	@Override
	public void setName(String s){}
	
	@Override
	public String toString(){
		return task.getName();
	}

	@Override
	public void notifyResourceCreation(String id) {
		comboboxAssign.addItem(id);
	}

	@Override
	public void notifyResourceDeletion(String id) {
		comboboxAssign.removeItem(id);
	}

	@Override
	public void notifyResourceRenaming(String id, String newid) {
		comboboxAssign.removeItem(id);
		comboboxAssign.addItem(newid);
	}

}
