package de.hpi.bpt.scylla.GUI.SimulationConfigurationPane;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.NumberFormat;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.hpi.bpt.scylla.GUI.FormManager;
import de.hpi.bpt.scylla.GUI.ListChooserPanel.ComponentHolder;
import de.hpi.bpt.scylla.GUI.ScyllaGUI;
import de.hpi.bpt.scylla.GUI.InputFields.NumberField;
import de.hpi.bpt.scylla.creation.ElementLink;
import de.hpi.bpt.scylla.creation.SimulationConfiguration.ExclusiveGateway;
import de.hpi.bpt.scylla.creation.SimulationConfiguration.SimulationConfigurationCreator;

/**
 * Display class for X-Gateway configurations
 * TODO this is plugin content and might be moved to ui plugins in the future 
 * @author Leon Bein
 *
 */
@SuppressWarnings("serial")
public class ExclusiveGatewayPanel extends JPanel implements ComponentHolder{

	/**Manager for changes*/
	private FormManager formManager;
	/**Wrapper for gateway to be configured*/
	private ExclusiveGateway gateway;
	/**Parent sc creator, to get branching targets*/
	private SimulationConfigurationCreator scCreator;
	/**Branch subpanel; must be internally visible for branch adding*/
	private JPanel panelBranches;
	

	public ExclusiveGatewayPanel(ExclusiveGateway g, FormManager fm, SimulationConfigurationCreator scc) {
		
		formManager = fm;
		gateway = g;
		scCreator = scc;
		
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		setLayout(gridBagLayout);
		
		panelBranches = new JPanel();
		GridBagConstraints gbc_panelBranches = new GridBagConstraints();
		gbc_panelBranches.fill = GridBagConstraints.HORIZONTAL;
		gbc_panelBranches.weightx = 1;
		gbc_panelBranches.weighty = 1;
		gbc_panelBranches.anchor = GridBagConstraints.PAGE_START;
		gbc_panelBranches.gridx = 0;
		gbc_panelBranches.gridy = 0;
		add(panelBranches, gbc_panelBranches);
		panelBranches.setLayout(new BoxLayout(panelBranches, BoxLayout.Y_AXIS));

	}
	
	public void initBranches() {
		for(String branch : gateway.getBranches()){
			ElementLink target = scCreator.getFlowTarget(branch);
			panelBranches.add(createBranchPanel(branch, target.getName()));
		}
	}

	@Override
	public Component getComponent() {
		return this;
	}
	
	@Override
	public String toString(){
		return gateway.getId();
	}
	
	private JPanel createBranchPanel(String branch, String target){
		JPanel panel = new JPanel();
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWeights = new double[]{1.0, 1.0, 1.0, 0.0, 1.0};
		panel.setLayout(gridBagLayout);
		
		JLabel labelTarget = new JLabel(target);
		GridBagConstraints gbc_labelTarget = new GridBagConstraints();
		gbc_labelTarget.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
		gbc_labelTarget.gridx = 0;
		gbc_labelTarget.gridy = 0;
		panel.add(labelTarget, gbc_labelTarget);
		
		JLabel labelProbability = new JLabel("Probability:");
		GridBagConstraints gbc_labelProbability = new GridBagConstraints();
		gbc_labelProbability.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
		gbc_labelProbability.anchor = GridBagConstraints.EAST;
		gbc_labelProbability.gridx = 1;
		gbc_labelProbability.gridy = 0;
		panel.add(labelProbability, gbc_labelProbability);
		
		
		NumberField<Double> textfieldProbability = new NumberField<Double>(formManager) {
			@Override
			protected NumberFormat getFormat() {
				return DECIMALFORMAT;
			}

			@Override
			protected Double getSavedValue() {
				String prob = gateway.getBranchingProbability(branch);
				return prob.isEmpty() ? null : Double.valueOf(prob)*100.0;
			}

			@Override
			protected void setSavedValue(Double v) {
				gateway.setBranchingProbability(branch, v/100.0);
			}
		};
		textfieldProbability.setMinimum(0.0);
		textfieldProbability.setMaximum(100.0);
		
		GridBagConstraints gbc_textfieldProbability = new GridBagConstraints();
		gbc_textfieldProbability.insets = new Insets(ScyllaGUI.STDINSET, 0, ScyllaGUI.STDINSET, 0);
		gbc_textfieldProbability.fill = GridBagConstraints.HORIZONTAL;
		gbc_textfieldProbability.gridx = 2;
		gbc_textfieldProbability.gridy = 0;
		panel.add(textfieldProbability.getComponent(), gbc_textfieldProbability);

		
		JLabel labelPercent = new JLabel("%");
		GridBagConstraints gbc_labelPercent = new GridBagConstraints();
		gbc_labelPercent.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
		gbc_labelPercent.gridx = 3;
		gbc_labelPercent.gridy = 0;
		panel.add(labelPercent, gbc_labelPercent);
		
		JLabel labelFill = new JLabel(" ");
		GridBagConstraints gbc_labelFill = new GridBagConstraints();
		gbc_labelFill.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
		gbc_labelFill.gridx = 4;
		gbc_labelFill.gridy = 0;
		gbc_labelFill.fill = GridBagConstraints.HORIZONTAL;
		panel.add(labelFill, gbc_labelFill);
		
		return panel;
	}

}
