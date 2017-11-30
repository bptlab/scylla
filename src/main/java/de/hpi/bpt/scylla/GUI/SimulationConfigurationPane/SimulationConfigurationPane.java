package de.hpi.bpt.scylla.GUI.SimulationConfigurationPane;


import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import de.hpi.bpt.scylla.GUI.EditorPane;
import de.hpi.bpt.scylla.GUI.ExpandPanel;
import de.hpi.bpt.scylla.GUI.InsertRemoveListener;
import de.hpi.bpt.scylla.GUI.ListChooserPanel;
import de.hpi.bpt.scylla.GUI.ListChooserPanel.ComponentHolder;
import de.hpi.bpt.scylla.GUI.ScalingCheckBoxIcon;
import de.hpi.bpt.scylla.GUI.ScalingFileChooser;
import de.hpi.bpt.scylla.GUI.ScyllaGUI;
import de.hpi.bpt.scylla.creation.ElementLink;
import de.hpi.bpt.scylla.creation.GlobalConfiguration.GlobalConfigurationCreator;
import de.hpi.bpt.scylla.creation.SimulationConfiguration.ExclusiveGateway;
import de.hpi.bpt.scylla.creation.SimulationConfiguration.SimulationConfigurationCreator;
import de.hpi.bpt.scylla.creation.SimulationConfiguration.SimulationConfigurationCreator.NoProcessSpecifiedException;
import de.hpi.bpt.scylla.creation.SimulationConfiguration.SimulationConfigurationCreator.NotAValidFileException;
import de.hpi.bpt.scylla.creation.SimulationConfiguration.SimulationConfigurationCreator.NotAuthorizedToOverrideException;
import de.hpi.bpt.scylla.creation.SimulationConfiguration.Task;

@SuppressWarnings("serial")
public class SimulationConfigurationPane extends EditorPane {
	
	private SimulationConfigurationCreator creator;
	private String bpmnPath;
	private String globalPath;
	
	private GlobalConfigurationCreator gcc;
	
	private JTextField textfieldId;
	private JFormattedTextField textfieldSeed;
	private JSpinner spinnerNOI;
	private JFormattedTextField textfieldStartDate;
	private ZonedDateTime startDateTime;
	private JFormattedTextField textfieldStartTime;
	private JFormattedTextField textfieldEndTime;
	private ZonedDateTime endDateTime;
	private JFormattedTextField textfieldEndDate;
	private JCheckBox checkboxUnlimited;
	private StartEventPanel startEventPanel;
	private ListChooserPanel gatewayPanel;
	private ListChooserPanel taskPanel;
	private DateTimeFormatter dateFormatter;
	private JLabel labelRefPMshow;
	private JLabel labelRefGCshow;
	private JButton button_openPM;
	private JButton button_openGC;
	private ExpandPanel panelTasksExpand;
	private ExpandPanel panelGatewaysExpand;
	private ExpandPanel panelStarteventExpand;

	/**
	 * Create the panel.
	 */
	public SimulationConfigurationPane() {

		int inset_b = 25;//TODO (int)(25.0*ScyllaGUI.SCALE);
		
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
		JLabel labelRefPM = new JLabel("Referenced process model file");
		GridBagConstraints gbc_labelRefPM = new GridBagConstraints();
		gbc_labelRefPM.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
		gbc_labelRefPM.fill = GridBagConstraints.HORIZONTAL;
		gbc_labelRefPM.gridx = 0;
		gbc_labelRefPM.gridy = 1;
		panelReference.add(labelRefPM, gbc_labelRefPM);
		
		//Label to show ref PM
		labelRefPMshow = new JLabel(" ");
		labelRefPMshow.setBackground(ScyllaGUI.ColorField2);
		labelRefPMshow.setOpaque(true);
		GridBagConstraints gbc_labelrefPMshow = new GridBagConstraints();
		gbc_labelrefPMshow.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
		gbc_labelrefPMshow.fill = GridBagConstraints.HORIZONTAL;
		gbc_labelrefPMshow.gridx = 1;
		gbc_labelrefPMshow.gridy = 1;
		panelReference.add(labelRefPMshow, gbc_labelrefPMshow);
		
		button_openPM = new JButton();
		button_openPM.setToolTipText("Open process model File");
		button_openPM.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				be_openPM();
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
		labelRefGCshow = new JLabel(" ");
		labelRefGCshow.setBackground(ScyllaGUI.ColorField2);
		labelRefGCshow.setOpaque(true);
		GridBagConstraints gbc_labelrefGCshow = new GridBagConstraints();
		gbc_labelrefGCshow.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
		gbc_labelrefGCshow.fill = GridBagConstraints.HORIZONTAL;
		gbc_labelrefGCshow.gridx = 1;
		gbc_labelrefGCshow.gridy = 2;
		panelReference.add(labelRefGCshow, gbc_labelrefGCshow);
		
		button_openGC = new JButton();
		button_openGC.setToolTipText("Open Global Configuration File");
		button_openGC.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				be_openGC();
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
		gbl_panelGeneral.columnWeights = new double[]{1.0,1.0,0.0,1.0, 0.0};
		panelGeneral.setLayout(gbl_panelGeneral);
		
		//General panel title label
		JLabel labelGeneralTitle = new JLabel("General");
		labelGeneralTitle.setBackground(ScyllaGUI.ColorField0);
		labelGeneralTitle.setForeground(ScyllaGUI.TITLEFONT_COLOR);
		labelGeneralTitle.setFont(ScyllaGUI.TITLEFONT);
		labelGeneralTitle.setOpaque(true);
		GridBagConstraints gbc_labelGeneralTitle = new GridBagConstraints();
		gbc_labelGeneralTitle.insets = new Insets(0, 0, 0, 0);
		gbc_labelGeneralTitle.fill = GridBagConstraints.HORIZONTAL;
		gbc_labelGeneralTitle.gridx = 0;
		gbc_labelGeneralTitle.gridy = 0;
		gbc_labelGeneralTitle.gridwidth = 5;
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
		gbc_textfieldId.gridwidth = 4;
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
		gbc_labelSeed.gridy = 2;
		panelGeneral.add(labelSeed, gbc_labelSeed);
		
		//Seed input field
		NumberFormat format = NumberFormat.getInstance();
		format.setGroupingUsed(false);
		textfieldSeed = new JFormattedTextField(format);
		textfieldSeed.getDocument().addDocumentListener(new InsertRemoveListener((DocumentEvent e)->{
			if(isChangeFlag())return;
			try{
				Long s = creator.getRandomSeed();
				Long n = Long.parseLong(textfieldSeed.getText());
				if(!n.equals(s)){
					creator.setRandomSeed(n);
					setSaved(false);
				}
			}catch(Exception exc){}
		}));
		GridBagConstraints gbc_textfieldSeed = new GridBagConstraints();
		gbc_textfieldSeed.gridwidth = 4;
		gbc_textfieldSeed.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, inset_b);
		gbc_textfieldSeed.fill = GridBagConstraints.HORIZONTAL;
		gbc_textfieldSeed.gridx = 1;
		gbc_textfieldSeed.gridy = 2;
		panelGeneral.add(textfieldSeed, gbc_textfieldSeed);
		
		//Number of instances (NOI) label
		JLabel labelNOI = new JLabel("Number of instances");
		GridBagConstraints gbc_labelNOI = new GridBagConstraints();
		gbc_labelNOI.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
		gbc_labelNOI.fill = GridBagConstraints.HORIZONTAL;
		gbc_labelNOI.gridx = 0;
		gbc_labelNOI.gridy = 3;
		panelGeneral.add(labelNOI, gbc_labelNOI);
		
		//Spinner for number of instances (NOI)
		spinnerNOI = new JSpinner();
		spinnerNOI.setModel(new SpinnerNumberModel(new Integer(0), new Integer(0), null, new Integer(1)));
		spinnerNOI.addChangeListener((ChangeEvent e)->{
			if(isChangeFlag())return;
			creator.setProcessInstances((Integer)spinnerNOI.getValue());
			setSaved(false);
		});
		GridBagConstraints gbc_spinnerNOI = new GridBagConstraints();
		gbc_spinnerNOI.gridwidth = 1;
		gbc_spinnerNOI.fill = GridBagConstraints.BOTH;
		gbc_spinnerNOI.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, inset_b);
		gbc_spinnerNOI.gridx = 1;
		gbc_spinnerNOI.gridy = 3;
		panelGeneral.add(spinnerNOI, gbc_spinnerNOI);
		
		//Start Date label
		JLabel labelStartDate = new JLabel("Start Date");
		GridBagConstraints gbc_labelStartDate = new GridBagConstraints();
		gbc_labelStartDate.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
		gbc_labelStartDate.fill = GridBagConstraints.HORIZONTAL;
		gbc_labelStartDate.gridx = 0;
		gbc_labelStartDate.gridy = 4;
		panelGeneral.add(labelStartDate, gbc_labelStartDate);
		
		//Startdate input field
		dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
		SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
		textfieldStartDate = new JFormattedTextField(df);
		textfieldStartDate.getDocument().addDocumentListener(new InsertRemoveListener((DocumentEvent e)->{
			if(isChangeFlag())return;
			try{
				LocalDate d = LocalDate.parse(textfieldStartDate.getText(),dateFormatter);
				if(startDateTime == null)startDateTime = ZonedDateTime.now();
				if(!d.equals(startDateTime.toLocalDate())){
					startDateTime = startDateTime.with(d);
					creator.setStartDateTime(startDateTime);
					setSaved(false);
				}
			}catch(Exception exc){}
		}));
		textfieldStartDate.setText(dateFormatter.format(LocalDate.now()));
		GridBagConstraints gbc_textfieldStartDate = new GridBagConstraints();
		gbc_textfieldStartDate.insets =   new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET,  ScyllaGUI.STDINSET);
		gbc_textfieldStartDate.fill = GridBagConstraints.HORIZONTAL;
		gbc_textfieldStartDate.gridx = 1;
		gbc_textfieldStartDate.gridy = 4;
		panelGeneral.add(textfieldStartDate, gbc_textfieldStartDate);
		
		//Start Time label
		JLabel labelStartTime = new JLabel("at");
		GridBagConstraints gbc_labelStartTime = new GridBagConstraints();
		gbc_labelStartTime.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
		gbc_labelStartTime.fill = GridBagConstraints.NONE;
		gbc_labelStartTime.gridx = 2;
		gbc_labelStartTime.gridy = 4;
		panelGeneral.add(labelStartTime, gbc_labelStartTime);
		
		//Start time input field
		textfieldStartTime = new JFormattedTextField(DateTimeFormatter.ISO_LOCAL_TIME.toFormat());
		textfieldStartTime.getDocument().addDocumentListener(new InsertRemoveListener((DocumentEvent e)->{
			if(isChangeFlag())return;
			try{
				LocalTime l = LocalTime.parse(textfieldStartTime.getText());
				if(startDateTime == null)startDateTime = ZonedDateTime.now();
				if(!l.equals(startDateTime.toLocalTime())){
					startDateTime = startDateTime.with(l);
					creator.setStartDateTime(startDateTime);
					setSaved(false);
				}
			}catch(Exception exc){}
		}));
		textfieldStartTime.setValue(LocalTime.of(0, 0, 0));//Triggers event!
		GridBagConstraints gbc_textfieldStartTime = new GridBagConstraints();
		gbc_textfieldStartTime.gridwidth = 2;
		gbc_textfieldStartTime.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, inset_b);
		gbc_textfieldStartTime.fill = GridBagConstraints.HORIZONTAL;
		gbc_textfieldStartTime.gridx = 3;
		gbc_textfieldStartTime.gridy = 4;
		panelGeneral.add(textfieldStartTime, gbc_textfieldStartTime);
		
		//End Date label
		JLabel labelEndDate = new JLabel("End Date");
		GridBagConstraints gbc_labelEndDate = new GridBagConstraints();
		gbc_labelEndDate.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
		gbc_labelEndDate.fill = GridBagConstraints.HORIZONTAL;
		gbc_labelEndDate.gridx = 0;
		gbc_labelEndDate.gridy = 5;
		panelGeneral.add(labelEndDate, gbc_labelEndDate);
		
		//Enddate input field
		textfieldEndDate = new JFormattedTextField(df);
		textfieldEndDate.getDocument().addDocumentListener(new InsertRemoveListener((DocumentEvent e)->{
			if(isChangeFlag())return;
			if(textfieldEndDate.getText().equals(""));
			else try{
				LocalDate d = LocalDate.parse(textfieldEndDate.getText(),dateFormatter);
				if(endDateTime == null)endDateTime = ZonedDateTime.now();
				if(!d.equals(endDateTime.toLocalDate())){
					endDateTime = endDateTime.with(d);
					creator.setEndDateTime(endDateTime);
					setSaved(false);
				}
			}catch(Exception exc){}
		}));
		textfieldEndDate.setText(dateFormatter.format(LocalDate.now()));
		GridBagConstraints gbc_textfieldEndDate = new GridBagConstraints();
		gbc_textfieldEndDate.insets =   new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
		gbc_textfieldEndDate.fill = GridBagConstraints.HORIZONTAL;
		gbc_textfieldEndDate.gridx = 1;
		gbc_textfieldEndDate.gridy = 5;
		panelGeneral.add(textfieldEndDate, gbc_textfieldEndDate);
		
		//End Time label
		JLabel labelEndTime = new JLabel("at");
		GridBagConstraints gbc_labelEndTime = new GridBagConstraints();
		gbc_labelEndTime.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
		gbc_labelEndTime.fill = GridBagConstraints.NONE;
		gbc_labelEndTime.gridx = 2;
		gbc_labelEndTime.gridy = 5;
		panelGeneral.add(labelEndTime, gbc_labelEndTime);
		
		//End time input field
		textfieldEndTime = new JFormattedTextField(DateTimeFormatter.ISO_LOCAL_TIME.toFormat());
		textfieldEndTime.getDocument().addDocumentListener(new InsertRemoveListener((DocumentEvent e)->{
			if(isChangeFlag())return;
			try{
				LocalTime l = LocalTime.parse(textfieldEndTime.getText());
				if(endDateTime == null)endDateTime = ZonedDateTime.now();
				if(!l.equals(endDateTime.toLocalTime())){
					endDateTime = endDateTime.with(l);
					creator.setEndDateTime(endDateTime);
					setSaved(false);
				}
			}catch(Exception exc){}
		}));
		textfieldEndTime.setValue(LocalTime.of(23, 59, 59));//Triggers event!
		GridBagConstraints gbc_textfieldEndTime = new GridBagConstraints();
		gbc_textfieldEndTime.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
		gbc_textfieldEndTime.fill = GridBagConstraints.HORIZONTAL;
		gbc_textfieldEndTime.gridx = 3;
		gbc_textfieldEndTime.gridy = 5;
		panelGeneral.add(textfieldEndTime, gbc_textfieldEndTime);
		
		//Check box for unlimited simulation time (= no end time)
		checkboxUnlimited = new JCheckBox("unlimited");
		checkboxUnlimited.setIcon(new ScalingCheckBoxIcon(textfieldEndTime.getFont().getSize()));
		checkboxUnlimited.addItemListener((ItemEvent e)->{
			boolean checked = checkboxUnlimited.isSelected();
			textfieldEndDate.setEnabled(!checked);
			textfieldEndTime.setEnabled(!checked);
			if(isChangeFlag())return;
			if(checked){
				creator.removeEndDateTime();
			}else{
				creator.setEndDateTime(endDateTime);
			}
		});
		GridBagConstraints gbc_checkboxUnlimited = new GridBagConstraints();
		gbc_checkboxUnlimited.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, inset_b);
		gbc_checkboxUnlimited.gridx = 4;
		gbc_checkboxUnlimited.gridy = 5;
		panelGeneral.add(checkboxUnlimited, gbc_checkboxUnlimited);
		
		
		
		
		//------ Start Event Panel -----
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
		startEventPanel = new StartEventPanel(this);
		panelStarteventExpand = new ExpandPanel(starteventLabel, createPMErrorLabel());
		panelMain.add(panelStarteventExpand, gbc_panelStartevent);
		
		//------ Task Panel ------
		taskPanel = new ListChooserPanel();
		
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
		panelTasksExpand = new ExpandPanel(tasksLabel, createPMErrorLabel());
		panelMain.add(panelTasksExpand, gbc_panelTasks);
		
		// ----- Gateway Panel -----
		gatewayPanel = new ListChooserPanel();
		
		
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
		panelGatewaysExpand = new ExpandPanel(gatewaysLabel, createPMErrorLabel());
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
		
		setEnabled(false);
		
		{//TODO delete
			setBounds(100, 100, 1475, 902);
		}
		

	}

	private void be_openPM() {
		//Choose file to be opened
		ScalingFileChooser chooser = new ScalingFileChooser(ScyllaGUI.DEFAULTFILEPATH);
		chooser.setDialogTitle("Open Process Model File");
		int c = chooser.showDialog(SimulationConfigurationPane.this,"Open");
		//if the process is canceled, nothing happens
		if(c == ScalingFileChooser.APPROVE_OPTION){
			if(chooser.getSelectedFile() != null){
				try {
					boolean success = true;
					bpmnPath = chooser.getSelectedFile().getPath();
					if(creator != null)success = updateModel();
					if(!success) {
						clearBpmnPath();
					}else {
						labelRefPMshow.setText(bpmnPath);
					}
				} catch (JDOMException | IOException e1) {
					e1.printStackTrace();
				}
				ScyllaGUI.DEFAULTFILEPATH = chooser.getSelectedFile().getPath();
			}else{
				System.err.println("Could not open file");
			}
		}
	}
	
	private void be_openGC() {
		//Choose file to be opened
		ScalingFileChooser chooser = new ScalingFileChooser(ScyllaGUI.DEFAULTFILEPATH);
		chooser.setDialogTitle("Open Global Configuration File");
		int c = chooser.showDialog(SimulationConfigurationPane.this,"Open");
		//if the process is canceled, nothing happens
		if(c == ScalingFileChooser.APPROVE_OPTION){
			if(chooser.getSelectedFile() != null){
				try {
					globalPath = chooser.getSelectedFile().getPath();
					gcc = GlobalConfigurationCreator.createFromFile(globalPath);
					if(creator != null)updateGCC();
					labelRefGCshow.setText(globalPath);
				} catch (JDOMException | IOException e1) {
					e1.printStackTrace();
				}
				ScyllaGUI.DEFAULTFILEPATH = chooser.getSelectedFile().getPath();
			}else{
				System.err.println("Could not open file");
			}
		}
	}

	@Override
	protected void create() {
		setChangeFlag(true);
		close();
		labelFiletitle.setText("<unsaved file>");
		creator = new SimulationConfigurationCreator();
		if(gcc != null)updateGCC();
		if(bpmnPath != null && !bpmnPath.isEmpty())try {
			updateModel();
		} catch (JDOMException | IOException e) {
			e.printStackTrace();
		}
		creator.setStartDateTime(startDateTime);
		creator.setEndDateTime(endDateTime);
		setSaved(false);
		setEnabled(true);
		setChangeFlag(false);
		//Id should be edited right after creation
		textfieldId.setText("NewSimulationConfiguration");
		textfieldId.requestFocusInWindow();
		textfieldId.selectAll();
	}

	@Override
	protected void save() {
		try {
			setSaved(true);
			creator.save(getFile().getPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void open() throws JDOMException, IOException {
		buttonClosefile.setEnabled(true);
		try {
			creator = SimulationConfigurationCreator.createFromFile(getFile().getPath());
		} catch (NotAValidFileException e1) {
			setFile(null);
			e1.printStackTrace();
			return;
		}
		if(gcc != null)creator.setGCC(gcc);
		Element modelRoot = null;
		if(bpmnPath != null && !bpmnPath.isEmpty())try {
	        Document doc;
	        SAXBuilder builder = new SAXBuilder();
			doc = builder.build(bpmnPath);
	        modelRoot = doc.getRootElement();
	        creator.setModel(modelRoot,false);
		} catch (JDOMException | IOException e) {
			e.printStackTrace();
		} catch (NoProcessSpecifiedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotAuthorizedToOverrideException e) {
			int override = showModelOverrideConfirmationDialog(e);
			if(override == 0)
				try {
					creator.setModel(modelRoot, true);
				} catch (NoProcessSpecifiedException | NotAuthorizedToOverrideException e1) {
					e1.printStackTrace();
				}
			else clearBpmnPath();
			e.printStackTrace();
		}
		setChangeFlag(true);
		textfieldId.setText(creator.getId());
		if(creator.getRandomSeed() != null){
			textfieldSeed.setValue(creator.getRandomSeed());
		}
		if(creator.getProcessInstances() != null)spinnerNOI.setValue(Integer.parseInt(creator.getProcessInstances()));
		String startDt = creator.getStartDateTime();
		if(startDt != null){
			startDateTime = ZonedDateTime.parse(startDt);
			textfieldStartTime.setValue(startDateTime.toLocalTime());
			textfieldStartDate.setText(dateFormatter.format(startDateTime.toLocalDate()));
		}
		String endDt = creator.getEndDateTime();
		if(endDt != null){
			checkboxUnlimited.setSelected(false);
			endDateTime = ZonedDateTime.parse(endDt);
			textfieldEndTime.setValue(endDateTime.toLocalTime());
			textfieldEndDate.setText(dateFormatter.format(endDateTime.toLocalDate()));
		}else {
			checkboxUnlimited.setSelected(true);
			textfieldEndDate.setEnabled(false);
			textfieldEndTime.setEnabled(false);
		}
		
		importCreatorElements();
		
		setChangeFlag(false);
		setEnabled(true);
	}
	

	private void importCreatorElements() {
		if(creator.getStartEvent() != null)startEventPanel.setStartEvent(creator.getStartEvent());
		
		for(ElementLink el : creator.getElements()){
			if(el instanceof Task){
				taskPanel.add((ComponentHolder)new TaskPanel((Task) el, this, gcc));
			}else if(el instanceof ExclusiveGateway){
				gatewayPanel.add((ComponentHolder)new ExclusiveGatewayPanel((ExclusiveGateway) el, this, creator));
			}
		}
	}

	@Override
	protected void close() {
		setChangeFlag(true);
		creator = null;
		setFile(null);
		globalPath = null;
		bpmnPath = null;
		clear();
		setChangeFlag(false);
		setSaved(true);
		setEnabled(false);
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
		
		button_openGC.setEnabled(b);
		button_openPM.setEnabled(b);
		
		textfieldId.setEnabled(b);
		textfieldSeed.setEnabled(b);
		spinnerNOI.setEnabled(b);
		textfieldStartTime.setEnabled(b);
		textfieldStartDate.setEnabled(b);
		textfieldEndTime.setEnabled(b && !checkboxUnlimited.isSelected());
		textfieldEndDate.setEnabled(b && !checkboxUnlimited.isSelected());
		checkboxUnlimited.setEnabled(b);
		
		startEventPanel.setEnabled(b);
		taskPanel.setEnabled(b);
		gatewayPanel.setEnabled(b);
		
		if(b) {
			panelStarteventExpand.expand();
			panelTasksExpand.expand();
			panelGatewaysExpand.expand();
		}else {
			panelStarteventExpand.collapse();
			panelTasksExpand.collapse();
			panelGatewaysExpand.collapse();
		}
		
		super.setEnabled(b);
	}
	
	public void clear() {
		labelRefGCshow.setText(" ");
		labelRefPMshow.setText(" ");
		textfieldId.setText("");
		textfieldSeed.setValue(null);
		spinnerNOI.setValue(0);
		textfieldStartTime.setValue(LocalTime.of(0, 0, 0));
		textfieldStartDate.setText(dateFormatter.format(LocalDate.now()));
		textfieldEndTime.setValue(LocalTime.of(23, 59, 59));
		textfieldEndDate.setText(dateFormatter.format(LocalDate.now()));
		checkboxUnlimited.setSelected(true);
		
		startEventPanel.clear();
		panelStarteventExpand.setContent(createPMErrorLabel());
		taskPanel.clear();
		panelTasksExpand.setContent(createPMErrorLabel());
		gatewayPanel.clear();
		panelGatewaysExpand.setContent(createPMErrorLabel());
	}
	
	private boolean updateModel() throws JDOMException, IOException {
        Document doc;
        SAXBuilder builder = new SAXBuilder();
		doc = builder.build(bpmnPath);
        Element modelRoot = doc.getRootElement();
        try {
			creator.setModel(modelRoot, false);
		} catch (NoProcessSpecifiedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotAuthorizedToOverrideException e) {
			if(showModelOverrideConfirmationDialog(e) == 0) {
				try {
					creator.setModel(modelRoot, true);
				} catch (NoProcessSpecifiedException | NotAuthorizedToOverrideException e1) {
					e1.printStackTrace();
				}
			}
			else return false;
		}
        
        //Reimport updated creator elements
        startEventPanel.setStartEvent(creator.getStartEvent());
        taskPanel.clear();
        gatewayPanel.clear();
        importCreatorElements();
        
        button_openPM.setEnabled(false);
        
        panelStarteventExpand.setContent(startEventPanel);
        panelTasksExpand.setContent(taskPanel);
        panelGatewaysExpand.setContent(gatewayPanel);
        gatewayPanel.forAll((gateway)->{
        	if(gateway instanceof ExclusiveGatewayPanel) {
        		((ExclusiveGatewayPanel) gateway).initBranches();
        	}
        });
        
        return true;
	}
	
	private int showModelOverrideConfirmationDialog(NotAuthorizedToOverrideException e) {
		return JOptionPane.showOptionDialog(
				this,
				"Cannot find process ref "+e.getOldRef()+". Override with found ref "+e.getNewRef()+"?",
				"Override process ref",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE,
				null,
				new Object[]{"Override process ref","Clear process model path"}, 
				0);
	}
	
	private void clearBpmnPath() {
		bpmnPath = "";
		labelRefPMshow.setText(" ");
	}
	
	private void updateGCC(){
		creator.setGCC(gcc);
		taskPanel.forAll((taskPanel)->{
			((TaskPanel)taskPanel).setGcc(gcc);
		});
	}
	
	private static JLabel createPMErrorLabel() {
		JLabel errorLabel = new JLabel("  Cannot edit until process model file is specified");
		errorLabel.setOpaque(true);
		errorLabel.setForeground(ScyllaGUI.ERRORFONT_COLOR);
		errorLabel.setFont(ScyllaGUI.DEFAULTFONT);
		return errorLabel;
	}

}
