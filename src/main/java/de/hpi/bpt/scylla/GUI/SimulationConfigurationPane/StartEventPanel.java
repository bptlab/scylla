package de.hpi.bpt.scylla.GUI.SimulationConfigurationPane;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.concurrent.TimeUnit;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.hpi.bpt.scylla.GUI.EditorPane;
import de.hpi.bpt.scylla.GUI.FormManager;
import de.hpi.bpt.scylla.GUI.ScyllaGUI;
import de.hpi.bpt.scylla.GUI.InputFields.SelectionField;
import de.hpi.bpt.scylla.creation.SimulationConfiguration.Distribution;
import de.hpi.bpt.scylla.creation.SimulationConfiguration.Distribution.DistributionType;
import de.hpi.bpt.scylla.creation.SimulationConfiguration.StartEvent;

@SuppressWarnings("serial")
public class StartEventPanel extends JPanel {

	private FormManager fm;
	private JComponent panelDistribution;
	
	private StartEvent startEvent;
	private GridBagConstraints gbc_panelDistribution;
	private SelectionField<DistributionType> comboboxDistribution;
	private SelectionField<TimeUnit> comboboxTimeunit;

	public StartEventPanel(FormManager f) {
		fm = f;
		
		//Layout
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWeights = new double[] {0.2,4,8};
		gridBagLayout.rowWeights = new double[] {0,1,0};
		setLayout(gridBagLayout);
		
		//Distribution label
		JLabel labelDistribution = new JLabel("Distribution");
		GridBagConstraints gbc_labelDistribution = EditorPane.createInputLabelConstraints(0);
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
		comboboxDistribution = new SelectionField<DistributionType>(fm, DistributionType.values()) {

			@Override
			protected DistributionType getSavedValue() {
				if(startEvent == null || startEvent.getArrivalRateDistribution() == null)return null;
				return startEvent.getArrivalRateDistribution().getType();
			}

			@Override
			protected void setSavedValue(DistributionType type) {
				Distribution d = Distribution.create(type);
				setPanelDistribution(d);
				startEvent.setArrivalRateDistribution(d);
			}
			
		};
		//JComboBox comboboxDistribution = new JComboBox();

		comboboxDistribution.clear();
		GridBagConstraints gbc_comboboxDistribution = new GridBagConstraints();
		gbc_comboboxDistribution.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
		gbc_comboboxDistribution.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboboxDistribution.gridx = 1;
		gbc_comboboxDistribution.gridy = 0;
		add(comboboxDistribution.getComponent(), gbc_comboboxDistribution);
		
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
		GridBagConstraints gbc_labelTimeunit = EditorPane.createInputLabelConstraints(2);
		add(labelTimeunit, gbc_labelTimeunit);
		
		//Time unit combobox
		comboboxTimeunit = new SelectionField<TimeUnit>(fm, TimeUnit.values()) {

			@Override
			protected TimeUnit getSavedValue() {
				if(startEvent == null)return null;
				return TimeUnit.valueOf(startEvent.getArrivalTimeUnit());
			}

			@Override
			protected void setSavedValue(TimeUnit v) {
				startEvent.setArrivalTimeUnit(v);
			}
			
		};
		comboboxTimeunit.clear();
		GridBagConstraints gbc_comboboxTimeunit = new GridBagConstraints();
		gbc_comboboxTimeunit.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
		gbc_comboboxTimeunit.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboboxTimeunit.gridx = 1;
		gbc_comboboxTimeunit.gridy = 2;
		add(comboboxTimeunit.getComponent(), gbc_comboboxTimeunit);
	}
	
	public void setStartEvent(StartEvent startEvent) {
		fm.setChangeFlag(true);
		
		this.startEvent = startEvent;
		Distribution d = startEvent.getArrivalRateDistribution();
		if(d != null){
			setPanelDistribution(d);
			comboboxDistribution.loadSavedValue();
		}else{
			setPanelDistribution(null);
			comboboxDistribution.clear();
		}
		comboboxTimeunit.loadSavedValue();
				
		fm.setChangeFlag(false);
	}
	
	/**
	 * Sets the distribution that is displayed, but no the comboboxes content
	 * @param d
	 */
	private void setPanelDistribution(Distribution d){
		if(panelDistribution != null)remove(panelDistribution);
		if(d != null)panelDistribution = new DistributionPanel(d, fm);
		else panelDistribution = new JLabel(" ");
		add(panelDistribution, gbc_panelDistribution);
		revalidate();
		repaint();
	}
	
	@Override
	public void setEnabled(boolean b) {
		comboboxDistribution.getComponent().setEnabled(b);
		comboboxTimeunit.getComponent().setEnabled(b);
		panelDistribution.setEnabled(b);
	}

	public void clear() {
		comboboxDistribution.clear();
		if(panelDistribution != null)remove(panelDistribution);
		panelDistribution = new JLabel(" ");
		add(panelDistribution, gbc_panelDistribution);
		comboboxTimeunit.clear();
	}

}
