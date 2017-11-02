package de.hpi.bpt.scylla.GUI.SimulationConfigurationPane;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
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
import de.hpi.bpt.scylla.creation.GlobalConfiguration.GlobalConfigurationCreator;
import de.hpi.bpt.scylla.creation.GlobalConfiguration.GlobalConfigurationCreator.ResourceType;
import de.hpi.bpt.scylla.creation.SimulationConfiguration.Distribution;
import de.hpi.bpt.scylla.creation.SimulationConfiguration.Distribution.DistributionType;
import de.hpi.bpt.scylla.creation.SimulationConfiguration.Resource;
import de.hpi.bpt.scylla.creation.SimulationConfiguration.Task;

@SuppressWarnings("serial")
public class TaskPanel extends JPanel implements ComponentHolder {

	private JComboBox<TimeUnit> comboboxTimeunit;
	private JComboBox<DistributionType> comboboxDistribution;
	private GridBagConstraints gbc_panelDistribution;
	private Component panelDistribution;
	
	private FormManager fm;
	private Task task;
	private JPanel panelResources;

	/**
	 * Create the panel.
	 */
	public TaskPanel(Task t, FormManager f) {
		
		fm = f;
		task = t;
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		setLayout(gridBagLayout);
		
		//Duration label
		JLabel labelDuration = new JLabel("Duration");
		labelDuration.setBackground(ScyllaGUI.ColorField0);
		labelDuration.setForeground(ScyllaGUI.TITLEFONT_COLOR);
		labelDuration.setFont(ScyllaGUI.TITLEFONT);
		labelDuration.setOpaque(true);
		GridBagConstraints gbc_labelDuration = new GridBagConstraints();
		gbc_labelDuration.gridwidth = 3;
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
				task.setDurationDistribution(d);
				fm.setSaved(false);
			}
		});
		comboboxDistribution.setSelectedIndex(-1);
		GridBagConstraints gbc_comboboxDistribution = new GridBagConstraints();
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
		gbc_labelFill.gridx = 2;
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
		gbc_labelResources.gridwidth = 3;
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
		gbc_scrollpaneResources.gridwidth = 3;
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
		
		JComboBox<ResourceType> comboboxAssign = new JComboBox<ResourceType>();//TODO add values
		GridBagConstraints gbc_comboboxAssign = new GridBagConstraints();
		gbc_comboboxAssign.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
		gbc_comboboxAssign.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboboxAssign.gridx = 0;
		gbc_comboboxAssign.gridy = 7;
		add(comboboxAssign, gbc_comboboxAssign);
		
		JButton buttonAssign = new JButton("assign");
		buttonAssign.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ResourceType type = new GlobalConfigurationCreator().addResourceType("Test");//TODO
				type.setName("Delete me");
				if(type != null){
					Resource res = task.assignResource(type);
					panelResources.add(createAssigner(res, 3));
					getParent().getParent().revalidate();
					repaint();
				}
			}
		});
		GridBagConstraints gbc_buttonAssign = new GridBagConstraints();
		gbc_buttonAssign.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
		gbc_buttonAssign.gridx = 1;
		gbc_buttonAssign.gridy = 7;
		add(buttonAssign, gbc_buttonAssign);
		
	}

	private void setPanelDistribution(Distribution d) {
		if(panelDistribution != null)remove(panelDistribution);
		panelDistribution = new DistributionPanel(d, fm);
		add(panelDistribution, gbc_panelDistribution);
		revalidate();
		repaint();
	}
	
	private JPanel createAssigner(Resource r, int amount){
		JPanel panel = new JPanel();
		GridBagLayout gbl_panel = new GridBagLayout(); 
		gbl_panel.columnWeights = new double[]{1,0,1,1,10};
		panel.setLayout(gbl_panel);

		JLabel labelName = new JLabel(r.getName());
		GridBagConstraints gbc_labelName = new GridBagConstraints();
		gbc_labelName.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
		gbc_labelName.gridx = 0;
		gbc_labelName.gridy = 0;
		panel.add(labelName, gbc_labelName);
		
		JLabel labelNumber = new JLabel("Amount:");
		GridBagConstraints gbc_labelNumber = new GridBagConstraints();
		gbc_labelNumber.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
		gbc_labelNumber.gridx = 1;
		gbc_labelNumber.gridy = 0;
		panel.add(labelNumber, gbc_labelNumber);
		
		JSpinner spinner = new JSpinner(new SpinnerNumberModel(new Integer(0), new Integer(0), null, new Integer(1)));
		//TODO set maximum to Resource maximum and add resource change listener for that
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
		panel.add(spinner, gbc_spinner);
		
		JButton buttonDeassign = new JButton("deassign");
		buttonDeassign.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				task.deassignResource(r.getId());
				panelResources.remove(panel);
				getParent().getParent().revalidate();
				repaint();
			}
		});
		GridBagConstraints gbc_buttonDeassign = new GridBagConstraints();
		gbc_buttonDeassign.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
		gbc_buttonDeassign.gridx = 3;
		gbc_buttonDeassign.gridy = 0;
		panel.add(buttonDeassign, gbc_buttonDeassign);
		
		JLabel labelFill = new JLabel(" ");
		GridBagConstraints gbc_labelFill = new GridBagConstraints();
		gbc_labelFill.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
		gbc_labelFill.gridx = 4;
		gbc_labelFill.gridy = 0;
		panel.add(labelFill, gbc_labelFill);
		
		return panel;
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

}
