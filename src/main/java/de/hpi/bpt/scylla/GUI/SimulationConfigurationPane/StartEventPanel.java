package de.hpi.bpt.scylla.GUI.SimulationConfigurationPane;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.util.concurrent.TimeUnit;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.hpi.bpt.scylla.GUI.FormManager;
import de.hpi.bpt.scylla.GUI.ScyllaGUI;
import de.hpi.bpt.scylla.creation.SimulationConfiguration.Distribution;
import de.hpi.bpt.scylla.creation.SimulationConfiguration.Distribution.DistributionType;
import de.hpi.bpt.scylla.creation.SimulationConfiguration.StartEvent;

@SuppressWarnings("serial")
public class StartEventPanel extends JPanel {

	private FormManager fm;
	private JComponent panelDistribution;
	
	private StartEvent startEvent;
	private GridBagConstraints gbc_panelDistribution;
	private JComboBox<DistributionType> comboboxDistribution;
	private JComboBox<TimeUnit> comboboxTimeunit;

	public StartEventPanel(FormManager f) {
		fm = f;
		
		//Layout
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWeights = new double[] {0.2,4,8};
		gridBagLayout.rowWeights = new double[] {0,1,0};
		setLayout(gridBagLayout);
		
		//Distribution label
		JLabel labelDistribution = new JLabel("Distribution");
		GridBagConstraints gbc_labelDistribution = new GridBagConstraints();
		gbc_labelDistribution.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
		gbc_labelDistribution.fill = GridBagConstraints.HORIZONTAL;
		gbc_labelDistribution.gridx = 0;
		gbc_labelDistribution.gridy = 0;
		add(labelDistribution, gbc_labelDistribution);
		
		//Distribution Panel layout constraints and empty "panel"
		gbc_panelDistribution = new GridBagConstraints();
		gbc_panelDistribution.gridwidth = 2;
		gbc_panelDistribution.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET*32, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET*2);
		gbc_panelDistribution.fill = GridBagConstraints.BOTH;
		gbc_panelDistribution.anchor = GridBagConstraints.WEST;
		gbc_panelDistribution.gridx = 0;
		gbc_panelDistribution.gridy = 1;
		panelDistribution = new JLabel(" ");
		add(panelDistribution, gbc_panelDistribution);
		
		//Distribution combobox
		comboboxDistribution = new JComboBox<DistributionType>(DistributionType.values());
		//JComboBox comboboxDistribution = new JComboBox();
		comboboxDistribution.addItemListener((ItemEvent e)->{
			if(e.getStateChange() != ItemEvent.SELECTED)return;
			if(fm.isChangeFlag())return;
			DistributionType type = (DistributionType) comboboxDistribution.getSelectedItem();
			if(type != null){
				Distribution d = Distribution.create(type);
				setPanelDistribution(d);
				startEvent.setArrivalRateDistribution(d);
				fm.setSaved(false);
			}
		});
		comboboxDistribution.setSelectedIndex(-1);
		GridBagConstraints gbc_comboboxDistribution = new GridBagConstraints();
		gbc_comboboxDistribution.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
		gbc_comboboxDistribution.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboboxDistribution.gridx = 1;
		gbc_comboboxDistribution.gridy = 0;
		add(comboboxDistribution, gbc_comboboxDistribution);
		
		//Empty fill label for more space
		JLabel labelFill = new JLabel(" ");
		GridBagConstraints gbc_labelFill = new GridBagConstraints();
		gbc_labelFill.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
		gbc_labelFill.fill = GridBagConstraints.HORIZONTAL;
		gbc_labelFill.gridx = 2;
		gbc_labelFill.gridy = 0;
		add(labelFill, gbc_labelFill);
		
		//Time unit label
		JLabel labelTimeunit = new JLabel("Timeunit");
		GridBagConstraints gbc_labelTimeunit = new GridBagConstraints();
		gbc_labelTimeunit.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
		gbc_labelTimeunit.fill = GridBagConstraints.HORIZONTAL;
		gbc_labelTimeunit.gridx = 0;
		gbc_labelTimeunit.gridy = 2;
		add(labelTimeunit, gbc_labelTimeunit);
		
		//Time unit combobox
		comboboxTimeunit = new JComboBox<TimeUnit>(TimeUnit.values());
		comboboxTimeunit.addItemListener((ItemEvent e)->{
			if(e.getStateChange() != ItemEvent.SELECTED)return;
			if(fm.isChangeFlag())return;
			startEvent.setArrivalTimeUnit((TimeUnit) comboboxTimeunit.getSelectedItem());
			fm.setSaved(false);
		});
		GridBagConstraints gbc_comboboxTimeunit = new GridBagConstraints();
		gbc_comboboxTimeunit.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
		gbc_comboboxTimeunit.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboboxTimeunit.gridx = 1;
		gbc_comboboxTimeunit.gridy = 2;
		add(comboboxTimeunit, gbc_comboboxTimeunit);
	}
	
	public void setStartEvent(StartEvent startEvent) {
		fm.setChangeFlag(true);
		
		this.startEvent = startEvent;
		Distribution d = startEvent.getArrivalRateDistribution();
		comboboxDistribution.setSelectedItem(d.getType());
		if(d != null){
			setPanelDistribution(d);
		}
		comboboxTimeunit.setSelectedItem(TimeUnit.valueOf(startEvent.getArritvalTimeUnit()));
				
		fm.setChangeFlag(false);
	}
	
	/**
	 * Sets the distribution that is displayed, but no the comboboxes content
	 * @param d
	 */
	private void setPanelDistribution(Distribution d){
		if(panelDistribution != null)remove(panelDistribution);
		panelDistribution = new DistributionPanel(d, fm);
		add(panelDistribution, gbc_panelDistribution);
		revalidate();
		repaint();
	}

}
