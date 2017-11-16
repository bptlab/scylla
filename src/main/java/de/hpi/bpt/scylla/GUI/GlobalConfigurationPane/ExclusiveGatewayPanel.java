package de.hpi.bpt.scylla.GUI.GlobalConfigurationPane;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BoxLayout;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.text.NumberFormatter;

import de.hpi.bpt.scylla.GUI.FormManager;
import de.hpi.bpt.scylla.GUI.InsertRemoveListener;
import de.hpi.bpt.scylla.GUI.ListChooserPanel.ComponentHolder;
import de.hpi.bpt.scylla.GUI.ScyllaGUI;
import de.hpi.bpt.scylla.creation.ElementLink;
import de.hpi.bpt.scylla.creation.SimulationConfiguration.ExclusiveGateway;
import de.hpi.bpt.scylla.creation.SimulationConfiguration.SimulationConfigurationCreator;

@SuppressWarnings("serial")
public class ExclusiveGatewayPanel extends JPanel implements ComponentHolder{

	
	private FormManager formManager;
	private ExclusiveGateway gateway;
	private SimulationConfigurationCreator scCreator;
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
		
		NumberFormatter formatter = new NumberFormatter(new NoNegativeDoubleFormat());
		formatter.setValueClass(Double.class);
		formatter.setMinimum(0.0);
		formatter.setMaximum(100.0);
		formatter.setAllowsInvalid(false);
		JFormattedTextField textfieldProbability = new JFormattedTextField(formatter);
		textfieldProbability.getDocument().addDocumentListener(new InsertRemoveListener((DocumentEvent e)->{
			if(formManager.isChangeFlag())return;
			try{
				gateway.setBranchingProbability(branch, Double.parseDouble(textfieldProbability.getText()));
				formManager.setSaved(false);
			}catch(NumberFormatException exc){}
		}));
		GridBagConstraints gbc_textfieldProbability = new GridBagConstraints();
		gbc_textfieldProbability.insets = new Insets(ScyllaGUI.STDINSET, 0, ScyllaGUI.STDINSET, 0);
		gbc_textfieldProbability.fill = GridBagConstraints.HORIZONTAL;
		gbc_textfieldProbability.gridx = 2;
		gbc_textfieldProbability.gridy = 0;
		panel.add(textfieldProbability, gbc_textfieldProbability);
		String prob = gateway.getBranchingProbability(branch);
		if(prob != null && !prob.isEmpty()){
			textfieldProbability.setValue((Double.parseDouble(prob)*100.0));
		}else{
			textfieldProbability.setText(0.0+"");
		}
		
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
