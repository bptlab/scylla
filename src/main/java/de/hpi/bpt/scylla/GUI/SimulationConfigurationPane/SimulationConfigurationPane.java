package de.hpi.bpt.scylla.GUI.SimulationConfigurationPane;


import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;

import org.jdom2.JDOMException;

import de.hpi.bpt.scylla.GUI.EditorPanel;
import de.hpi.bpt.scylla.GUI.ExpandPanel;
import de.hpi.bpt.scylla.GUI.InsertRemoveListener;
import de.hpi.bpt.scylla.GUI.ScyllaGUI;
import de.hpi.bpt.scylla.creation.SimulationConfiguration.SimulationConfigurationCreator;

@SuppressWarnings("serial")
public class SimulationConfigurationPane extends EditorPanel {
	
	private SimulationConfigurationCreator creator;
	
	private JTextField textfieldId;

	private JFormattedTextField textfieldSeed;

	/**
	 * Create the panel.
	 */
	public SimulationConfigurationPane() {
		
		setBounds(100, 100, 800, 450);

		int inset_b = 25;//(int)(25.0*ScyllaGUI.SCALE);
		
		// -- Reference Panel --
		JPanel panelReference = new JPanel();
		panelReference.setFocusable(true);
		GridBagConstraints gbc_panelReference = new GridBagConstraints();
		gbc_panelReference.anchor = GridBagConstraints.PAGE_START;
		gbc_panelReference.insets = new Insets(inset_b,inset_b,inset_b,inset_b);
		gbc_panelReference.gridx = 0;
		gbc_panelReference.gridy = 0;
		gbc_panelReference.fill = GridBagConstraints.HORIZONTAL;
		gbc_panelReference.weightx = 1;
		panelMain.add(panelReference, gbc_panelReference);
		GridBagLayout gbl_panelReference = new GridBagLayout();
		gbl_panelReference.columnWeights = new double[]{0.1,4.0,0};
		panelReference.setLayout(gbl_panelReference);
		
		//Referenced files title label
		JLabel labelReferenceTitle = new JLabel("Referenced files");
		labelReferenceTitle.setBackground(ScyllaGUI.ColorField0);
		labelReferenceTitle.setForeground(ScyllaGUI.TITLEFONT_COLOR);
		labelReferenceTitle.setFont(ScyllaGUI.TITLEFONT);
		labelReferenceTitle.setOpaque(true);
		GridBagConstraints gbc_labelReferenceTitle = new GridBagConstraints();
		gbc_labelReferenceTitle.fill = GridBagConstraints.HORIZONTAL;
		gbc_labelReferenceTitle.gridx = 0;
		gbc_labelReferenceTitle.gridy = 0;
		gbc_labelReferenceTitle.gridwidth = 3;
		panelReference.add(labelReferenceTitle, gbc_labelReferenceTitle);
		
		//Label referenced Process Model
		JLabel labelRefPM = new JLabel("Referenced Process Model file");
		GridBagConstraints gbc_labelRefPM = new GridBagConstraints();
		gbc_labelRefPM.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
		gbc_labelRefPM.fill = GridBagConstraints.HORIZONTAL;
		gbc_labelRefPM.gridx = 0;
		gbc_labelRefPM.gridy = 1;
		panelReference.add(labelRefPM, gbc_labelRefPM);
		
		//Label to show ref PM
		JLabel labelrefPMshow = new JLabel(" ");
		labelrefPMshow.setBackground(ScyllaGUI.ColorField2);
		labelrefPMshow.setOpaque(true);
		GridBagConstraints gbc_labelrefPMshow = new GridBagConstraints();
		gbc_labelrefPMshow.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
		gbc_labelrefPMshow.fill = GridBagConstraints.HORIZONTAL;
		gbc_labelrefPMshow.gridx = 1;
		gbc_labelrefPMshow.gridy = 1;
		panelReference.add(labelrefPMshow, gbc_labelrefPMshow);
		
		JButton button_openPM = new JButton();
		button_openPM.setToolTipText("Open Process Model File");
		button_openPM.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//TODO
			}
		});
		button_openPM.setIcon(ScyllaGUI.ICON_OPEN);
		GridBagConstraints gbc_button_openPM = new GridBagConstraints();
		gbc_button_openPM.fill = GridBagConstraints.NONE;
		gbc_button_openPM.gridx = 2;
		gbc_button_openPM.gridy = 1;
		gbc_button_openPM.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
		panelReference.add(button_openPM, gbc_button_openPM);
		
		//Label referenced global configuration
		JLabel labelRefGC = new JLabel("Referenced global configuration file");
		GridBagConstraints gbc_labelRefGC = new GridBagConstraints();
		gbc_labelRefGC.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
		gbc_labelRefGC.fill = GridBagConstraints.HORIZONTAL;
		gbc_labelRefGC.gridx = 0;
		gbc_labelRefGC.gridy = 2;
		panelReference.add(labelRefGC, gbc_labelRefGC);
		
		//Label to show ref gc
		JLabel labelrefGCshow = new JLabel(" ");
		labelrefGCshow.setBackground(ScyllaGUI.ColorField2);
		labelrefGCshow.setOpaque(true);
		GridBagConstraints gbc_labelrefGCshow = new GridBagConstraints();
		gbc_labelrefGCshow.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
		gbc_labelrefGCshow.fill = GridBagConstraints.HORIZONTAL;
		gbc_labelrefGCshow.gridx = 1;
		gbc_labelrefGCshow.gridy = 2;
		panelReference.add(labelrefGCshow, gbc_labelrefGCshow);
		
		JButton button_openGC = new JButton();
		button_openGC.setToolTipText("Open Global Configuration File");
		button_openGC.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//TODO
			}
		});
		button_openGC.setIcon(ScyllaGUI.ICON_OPEN);
		GridBagConstraints gbc_button_openGC = new GridBagConstraints();
		gbc_button_openGC.fill = GridBagConstraints.NONE;
		gbc_button_openGC.gridx = 2;
		gbc_button_openGC.gridy = 2;
		gbc_button_openGC.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
		panelReference.add(button_openGC, gbc_button_openGC);
		
		// -- General Information Panel --
		JPanel panelGeneral = new JPanel();
		panelGeneral.setFocusable(true);
		GridBagConstraints gbc_panelGeneral = new GridBagConstraints();
		gbc_panelGeneral.anchor = GridBagConstraints.PAGE_START;
		gbc_panelGeneral.insets = new Insets(0 ,inset_b,inset_b,inset_b);
		gbc_panelGeneral.gridx = 0;
		gbc_panelGeneral.gridy = 1;
		gbc_panelGeneral.fill = GridBagConstraints.HORIZONTAL;
		gbc_panelGeneral.weightx = 1;
		panelMain.add(panelGeneral, gbc_panelGeneral);
		GridBagLayout gbl_panelGeneral = new GridBagLayout();
		gbl_panelGeneral.columnWeights = new double[]{1.0,1.0,0.0,3.0};
		panelGeneral.setLayout(gbl_panelGeneral);
		
		//General panel title label
		JLabel labelGeneralTitle = new JLabel("General");
		labelGeneralTitle.setBackground(ScyllaGUI.ColorField0);
		labelGeneralTitle.setForeground(ScyllaGUI.TITLEFONT_COLOR);
		labelGeneralTitle.setFont(ScyllaGUI.TITLEFONT);
		labelGeneralTitle.setOpaque(true);
		GridBagConstraints gbc_labelGeneralTitle = new GridBagConstraints();
		gbc_labelGeneralTitle.insets = new Insets(0, 0, 5, 0);
		gbc_labelGeneralTitle.fill = GridBagConstraints.HORIZONTAL;
		gbc_labelGeneralTitle.gridx = 0;
		gbc_labelGeneralTitle.gridy = 0;
		gbc_labelGeneralTitle.gridwidth = 4;
		panelGeneral.add(labelGeneralTitle, gbc_labelGeneralTitle);
		
		//Id label
		JLabel labelId = new JLabel("Id");
		GridBagConstraints gbc_labelId = new GridBagConstraints();
		gbc_labelId.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
		gbc_labelId.fill = GridBagConstraints.HORIZONTAL;
		gbc_labelId.gridx = 0;
		gbc_labelId.gridy = 1;
		panelGeneral.add(labelId, gbc_labelId);
		
		//Id input field
		textfieldId = new JTextField();
		textfieldId.getDocument().addDocumentListener(new InsertRemoveListener((DocumentEvent e)->{
			if(isChangeFlag())return;
			creator.setId(textfieldId.getText());
			setSaved(false);
		}));
		GridBagConstraints gbc_textfieldId = new GridBagConstraints();
		gbc_textfieldId.gridwidth = 3;
		gbc_textfieldId.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, inset_b);
		gbc_textfieldId.fill = GridBagConstraints.HORIZONTAL;
		gbc_textfieldId.gridx = 1;
		gbc_textfieldId.gridy = 1;
		panelGeneral.add(textfieldId, gbc_textfieldId);
		
		//Seed label
		JLabel labelSeed = new JLabel("Seed");
		GridBagConstraints gbc_labelSeed = new GridBagConstraints();
		gbc_labelSeed.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
		gbc_labelSeed.fill = GridBagConstraints.HORIZONTAL;
		gbc_labelSeed.gridx = 0;
		gbc_labelSeed.gridy = 1;
		panelGeneral.add(labelSeed, gbc_labelSeed);
		
		//Seed input field
		NumberFormat format = NumberFormat.getInstance();
		format.setGroupingUsed(false);
		textfieldSeed = new JFormattedTextField(format);
		textfieldSeed.getDocument().addDocumentListener(new InsertRemoveListener((DocumentEvent e)->{
			if(isChangeFlag())return;
			try{
				long s = creator.getRandomSeed();
				long n = Long.parseLong(textfieldSeed.getText());
				if(s != n){
					creator.setRandomSeed(n);
					setSaved(false);
				}
			}catch(Exception exc){}
		}));
		textfieldSeed.getDocument().addDocumentListener(new InsertRemoveListener((DocumentEvent e)->{
			if(isChangeFlag())return;
			creator.setId(textfieldSeed.getText());
			setSaved(false);
		}));
		GridBagConstraints gbc_textfieldSeed = new GridBagConstraints();
		gbc_textfieldSeed.gridwidth = 3;
		gbc_textfieldSeed.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, inset_b);
		gbc_textfieldSeed.fill = GridBagConstraints.HORIZONTAL;
		gbc_textfieldSeed.gridx = 1;
		gbc_textfieldSeed.gridy = 1;
		panelGeneral.add(textfieldSeed, gbc_textfieldSeed);
		
		
		
		//------
		GridBagConstraints gbc_panelStartevent = new GridBagConstraints();
		gbc_panelStartevent.anchor = GridBagConstraints.PAGE_START;
		gbc_panelStartevent.fill = GridBagConstraints.HORIZONTAL;
		gbc_panelStartevent.weightx = 1.0;
		gbc_panelStartevent.weighty = 1.0;
		gbc_panelStartevent.insets = new Insets(0, inset_b, inset_b, inset_b);
		gbc_panelStartevent.gridx = 0;
		gbc_panelStartevent.gridy = 2;
		JLabel starteventLabel = new JLabel("Start Arrival");
		starteventLabel.setBackground(ScyllaGUI.ColorField0);
		starteventLabel.setForeground(ScyllaGUI.TITLEFONT_COLOR);
		starteventLabel.setFont(ScyllaGUI.TITLEFONT);
		starteventLabel.setOpaque(true);
		ExpandPanel panelStarteventExpand = new ExpandPanel(starteventLabel, new JPanel());
		panelStarteventExpand.expand();
		panelMain.add(panelStarteventExpand, gbc_panelStartevent);
		
		GridBagConstraints gbc_panelTasks = new GridBagConstraints();
		gbc_panelTasks.anchor = GridBagConstraints.PAGE_START;
		gbc_panelTasks.fill = GridBagConstraints.HORIZONTAL;
		gbc_panelTasks.weightx = 1.0;
		gbc_panelTasks.weighty = 1.0;
		gbc_panelTasks.insets = new Insets(0, inset_b, inset_b, inset_b);
		gbc_panelTasks.gridx = 0;
		gbc_panelTasks.gridy = 3;
		JLabel tasksLabel = new JLabel("Tasks");
		tasksLabel.setBackground(ScyllaGUI.ColorField0);
		tasksLabel.setForeground(ScyllaGUI.TITLEFONT_COLOR);
		tasksLabel.setFont(ScyllaGUI.TITLEFONT);
		tasksLabel.setOpaque(true);
		ExpandPanel panelTasksExpand = new ExpandPanel(tasksLabel, new JPanel());
		panelTasksExpand.expand();
		panelMain.add(panelTasksExpand, gbc_panelTasks);
		
		GridBagConstraints gbc_panelGateways = new GridBagConstraints();
		gbc_panelGateways.anchor = GridBagConstraints.PAGE_START;
		gbc_panelGateways.fill = GridBagConstraints.HORIZONTAL;
		gbc_panelGateways.weightx = 1.0;
		gbc_panelGateways.weighty = 1.0;
		gbc_panelGateways.insets = new Insets(0, inset_b, inset_b, inset_b);
		gbc_panelGateways.gridx = 0;
		gbc_panelGateways.gridy = 4;
		JLabel gatewaysLabel = new JLabel("Gateways");
		gatewaysLabel.setBackground(ScyllaGUI.ColorField0);
		gatewaysLabel.setForeground(ScyllaGUI.TITLEFONT_COLOR);
		gatewaysLabel.setFont(ScyllaGUI.TITLEFONT);
		gatewaysLabel.setOpaque(true);
		ExpandPanel panelGatewaysExpand = new ExpandPanel(gatewaysLabel, new JPanel());
		panelGatewaysExpand.expand();
		panelMain.add(panelGatewaysExpand, gbc_panelGateways);
		
		//Layout fixing empty buffer panel
		JPanel panelBuffer = new JPanel();
		panelBuffer.setFocusable(true);
		panelBuffer.setBackground(panelMain.getBackground());
		GridBagConstraints gbc_panelBuffer = new GridBagConstraints();
		gbc_panelBuffer.anchor = GridBagConstraints.PAGE_START;
		gbc_panelBuffer.fill = GridBagConstraints.BOTH;
		gbc_panelBuffer.weighty = 100;
		gbc_panelBuffer.weightx = 1;
		gbc_panelBuffer.gridx = 0;
		gbc_panelBuffer.gridy = 5;
		panelMain.add(panelBuffer,gbc_panelBuffer);
		

	}

	@Override
	protected void create() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void save() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void open() throws JDOMException, IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void close() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected String getId() {
		return creator.getId();
	}
	
	/**
	 * Enables or disables all User input components
	 */
	@Override
	public void setEnabled(boolean b){
		//TODO
		super.setEnabled(b);
	}

}
