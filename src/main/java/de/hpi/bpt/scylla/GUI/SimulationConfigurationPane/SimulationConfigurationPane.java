package de.hpi.bpt.scylla.GUI.SimulationConfigurationPane;


import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import de.hpi.bpt.scylla.GUI.EditorPane;
import de.hpi.bpt.scylla.GUI.ExpandPanel;
import de.hpi.bpt.scylla.GUI.ListChooserPanel;
import de.hpi.bpt.scylla.GUI.ListChooserPanel.ComponentHolder;
import de.hpi.bpt.scylla.GUI.ScalingCheckBoxIcon;
import de.hpi.bpt.scylla.GUI.ScalingFileChooser;
import de.hpi.bpt.scylla.GUI.ScyllaGUI;
import de.hpi.bpt.scylla.GUI.InputFields.DateField;
import de.hpi.bpt.scylla.GUI.InputFields.NumberField;
import de.hpi.bpt.scylla.GUI.InputFields.NumberSpinner;
import de.hpi.bpt.scylla.GUI.InputFields.StringField;
import de.hpi.bpt.scylla.GUI.InputFields.TimeField;
import de.hpi.bpt.scylla.GUI.plugin.EditorTabPluggable;
import de.hpi.bpt.scylla.GUI.plugin.InputFieldPluggable;
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
	
	private StringField textfieldId;
	private NumberField<Long> textfieldSeed;
	private NumberSpinner<Integer> spinnerNOI;
	private DateField textfieldStartDate;
	private ZonedDateTime startDateTime;
	private TimeField textfieldStartTime;
	private TimeField textfieldEndTime;
	private ZonedDateTime endDateTime;
	private DateField textfieldEndDate;
	private JCheckBox checkboxUnlimited;
	private StartEventPanel startEventPanel;
	private ListChooserPanel gatewayPanel;
	private ListChooserPanel taskPanel;
//	private DateTimeFormatter dateFormatter;
	private JLabel labelRefPMshow;
	private JLabel labelRefGCshow;
	private JButton button_openPM;
	private JButton button_openGC;
	private ExpandPanel panelTasksExpand;
	private ExpandPanel panelGatewaysExpand;
	private ExpandPanel panelStarteventExpand;
	
	public enum Tabs {FILEREFERENCE, GENERAL, STARTARRIVAL, TASKS, GATEWAYS}

	/**
	 * Create the panel.
	 */
	public SimulationConfigurationPane() {
		
		// -- Reference Panel --
		JPanel panelReference = new JPanel();
		panelReference.setFocusable(true);
		GridBagConstraints gbc_panelReference = new GridBagConstraints();
		gbc_panelReference.anchor = GridBagConstraints.PAGE_START;
		gbc_panelReference.insets = new Insets(INSET_B,INSET_B,INSET_B,INSET_B);
		gbc_panelReference.gridx = 0;
		gbc_panelReference.gridy = GridBagConstraints.RELATIVE;
		gbc_panelReference.fill = GridBagConstraints.HORIZONTAL;
		gbc_panelReference.weightx = 1;
		addTab(Tabs.FILEREFERENCE, panelReference, gbc_panelReference);
		GridBagLayout gbl_panelReference = new GridBagLayout();
		gbl_panelReference.columnWeights = new double[]{0.1,4.0,0};
		panelReference.setLayout(gbl_panelReference);
		
		//Referenced files title label
		JLabel labelReferenceTitle = createTabLabel("Referenced files");
		GridBagConstraints gbc_labelReferenceTitle = new GridBagConstraints();
		gbc_labelReferenceTitle.fill = GridBagConstraints.HORIZONTAL;
		gbc_labelReferenceTitle.gridx = 0;
		gbc_labelReferenceTitle.gridy = 0;
		gbc_labelReferenceTitle.gridwidth = 3;
		panelReference.add(labelReferenceTitle, gbc_labelReferenceTitle);
		
		//Label referenced Process Model
		JLabel labelRefPM = new JLabel("Referenced process model file");
		GridBagConstraints gbc_labelRefPM = createInputLabelConstraints();
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
		GridBagConstraints gbc_labelRefGC = createInputLabelConstraints();
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
		gbc_panelGeneral.insets = new Insets(0 ,INSET_B,INSET_B,INSET_B);
		gbc_panelGeneral.gridx = 0;
		gbc_panelGeneral.gridy = GridBagConstraints.RELATIVE;
		gbc_panelGeneral.fill = GridBagConstraints.HORIZONTAL;
		gbc_panelGeneral.weightx = 1;
		addTab(Tabs.GENERAL, panelGeneral, gbc_panelGeneral);
		GridBagLayout gbl_panelGeneral = new GridBagLayout();
		gbl_panelGeneral.columnWeights = new double[]{1.0,1.0,0.0,1.0, 0.0};
		panelGeneral.setLayout(gbl_panelGeneral);
		
		//General panel title label
		JLabel labelGeneralTitle = createTabLabel("General");
		GridBagConstraints gbc_labelGeneralTitle = new GridBagConstraints();
		gbc_labelGeneralTitle.insets = new Insets(0, 0, 0, 0);
		gbc_labelGeneralTitle.fill = GridBagConstraints.HORIZONTAL;
		gbc_labelGeneralTitle.gridx = 0;
		gbc_labelGeneralTitle.gridy = 0;
		gbc_labelGeneralTitle.gridwidth = GridBagConstraints.REMAINDER;
		panelGeneral.add(labelGeneralTitle, gbc_labelGeneralTitle);
		
		//Id label
		JLabel labelId = new JLabel("Id");
		GridBagConstraints gbc_labelId = createInputLabelConstraints();
		panelGeneral.add(labelId, gbc_labelId);
		
		//Id input field
		textfieldId = new StringField(this) {
			protected String getSavedValue() {
				if(creator == null)return null;
				return creator.getId();
			}
			protected void setSavedValue(String v) {creator.setId(v);}
		};
		GridBagConstraints gbc_textfieldId = new GridBagConstraints();
		gbc_textfieldId.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, INSET_B);
		gbc_textfieldId.fill = GridBagConstraints.HORIZONTAL;
		gbc_textfieldId.gridx = GridBagConstraints.RELATIVE;
		gbc_textfieldId.gridwidth = GridBagConstraints.REMAINDER;
		panelGeneral.add(textfieldId.getComponent(), gbc_textfieldId);
		
		//Seed label
		JLabel labelSeed = new JLabel("Seed");
		GridBagConstraints gbc_labelSeed = createInputLabelConstraints();
		panelGeneral.add(labelSeed, gbc_labelSeed);
		
		//Seed input field
		textfieldSeed = new NumberField<Long>(this) {

			@Override
			protected void setSavedValue(Long v) {
				if(creator != null)creator.setRandomSeed(v);
			}
			
			@Override
			protected Long getSavedValue() {
				return creator != null ? creator.getRandomSeed() : null;
			}
			
		};
		GridBagConstraints gbc_textfieldSeed = createInputFieldConstraints();
		panelGeneral.add(textfieldSeed.getComponent(), gbc_textfieldSeed);
		
		//Number of instances (NOI) label
		JLabel labelNOI = new JLabel("Number of instances");
		GridBagConstraints gbc_labelNOI = createInputLabelConstraints();
		panelGeneral.add(labelNOI, gbc_labelNOI);
		
		//Spinner for number of instances (NOI)
		spinnerNOI = new NumberSpinner<Integer>(this,0,0,null,1) {
			
			@Override
			protected void setSavedValue(Integer v) {
				creator.setProcessInstances(v);
			}
			
			@Override
			protected Integer getSavedValue() {
				if(creator == null || creator.getProcessInstances() == null)return null;
				return Integer.valueOf(creator.getProcessInstances());
			}
			
		};
		GridBagConstraints gbc_spinnerNOI = createInputFieldConstraints();
		gbc_spinnerNOI.gridwidth = 1;
		panelGeneral.add(spinnerNOI.getComponent(), gbc_spinnerNOI);

		GridBagConstraints gbc_spinnerPadding = createInputFieldConstraints();
		panelGeneral.add(new Label(), gbc_spinnerPadding);
		
		//Start Date label
		JLabel labelStartDate = new JLabel("Start Date");
		GridBagConstraints gbc_labelStartDate = createInputLabelConstraints();
		panelGeneral.add(labelStartDate, gbc_labelStartDate);
		
		//Startdate input field
		textfieldStartDate = new DateField(this) {
			@Override
			protected void setSavedValue(LocalDate v) {
				startDateTime = startDateTime.with(v);
				creator.setStartDateTime(startDateTime);
			}
			
			@Override
			protected LocalDate getSavedValue() {
				if(startDateTime == null)startDateTime = ZonedDateTime.now();
				return startDateTime.toLocalDate();
			}
		};
		GridBagConstraints gbc_textfieldStartDate = createInputFieldConstraints();
		gbc_textfieldStartDate.gridwidth = 1;
		panelGeneral.add(textfieldStartDate.getComponent(), gbc_textfieldStartDate);
		
		//Start Time label
		JLabel labelStartTime = new JLabel("at");
		GridBagConstraints gbc_labelStartTime = createInputLabelConstraints();
		gbc_labelStartTime.gridx = 2;
		panelGeneral.add(labelStartTime, gbc_labelStartTime);
		
		//Start time input field
		textfieldStartTime = new TimeField(this) {
			
			@Override
			protected void setSavedValue(LocalTime v) {
				startDateTime = startDateTime.with(v);
				creator.setStartDateTime(startDateTime);
			}
			
			@Override
			protected LocalTime getSavedValue() {
				if(startDateTime == null)startDateTime = ZonedDateTime.now();
				return startDateTime.toLocalTime();
			}
		};
		GridBagConstraints gbc_textfieldStartTime = createInputFieldConstraints();
		panelGeneral.add(textfieldStartTime.getComponent(), gbc_textfieldStartTime);
		
		//End Date label
		JLabel labelEndDate = new JLabel("End Date");
		GridBagConstraints gbc_labelEndDate = createInputLabelConstraints();
		panelGeneral.add(labelEndDate, gbc_labelEndDate);
		
		//Enddate input field
		textfieldEndDate = new DateField(this) {
			@Override
			protected void setSavedValue(LocalDate v) {
				endDateTime = endDateTime.with(v);
				creator.setEndDateTime(endDateTime);
			}
			
			@Override
			protected LocalDate getSavedValue() {
				if(endDateTime == null)endDateTime = ZonedDateTime.now();
				return endDateTime.toLocalDate();
			}
		};
		GridBagConstraints gbc_textfieldEndDate = createInputFieldConstraints();
		gbc_textfieldEndDate.gridwidth = 1;
		panelGeneral.add(textfieldEndDate.getComponent(), gbc_textfieldEndDate);
		
		//End Time label
		JLabel labelEndTime = new JLabel("at");
		GridBagConstraints gbc_labelEndTime = createInputLabelConstraints();
		gbc_labelEndTime.gridx = 2;
		panelGeneral.add(labelEndTime, gbc_labelEndTime);
		
		//End time input field
		textfieldEndTime = new TimeField(this) {
			
			@Override
			protected void setSavedValue(LocalTime v) {
				endDateTime = endDateTime.with(v);
				creator.setEndDateTime(endDateTime);
			}
			
			@Override
			protected LocalTime getSavedValue() {
				if(endDateTime == null)endDateTime = ZonedDateTime.now();
				return endDateTime.toLocalTime();
			}
			
			@Override
			protected LocalTime defaultValue() {
				return LocalTime.of(23,59,59);
			}
		};
		GridBagConstraints gbc_textfieldEndTime = createInputFieldConstraints();
		gbc_textfieldEndTime.gridwidth = 1;
		panelGeneral.add(textfieldEndTime.getComponent(), gbc_textfieldEndTime);
		
		//Check box for unlimited simulation time (= no end time)
		checkboxUnlimited = new JCheckBox("unlimited");
		checkboxUnlimited.setIcon(new ScalingCheckBoxIcon(textfieldEndTime.getComponent().getFont().getSize()));
		checkboxUnlimited.addItemListener((ItemEvent e)->{
			boolean checked = checkboxUnlimited.isSelected();
			textfieldEndDate.getComponent().setEnabled(!checked);
			textfieldEndTime.getComponent().setEnabled(!checked);
			if(isChangeFlag())return;
			if(checked){
				creator.removeEndDateTime();
			}else{
				creator.setEndDateTime(endDateTime);
			}
		});
		GridBagConstraints gbc_checkboxUnlimited = createInputFieldConstraints();
		panelGeneral.add(checkboxUnlimited, gbc_checkboxUnlimited);
		
		
		
		
		//------ Start Event Panel -----
		startEventPanel = new StartEventPanel(this);
		panelStarteventExpand = addTab(Tabs.STARTARRIVAL, "Start Arrival", createPMErrorLabel());
		
		//------ Task Panel ------
		taskPanel = new ListChooserPanel();
		panelTasksExpand = addTab(Tabs.TASKS, "Tasks", createPMErrorLabel());
		
		// ----- Gateway Panel -----
		gatewayPanel = new ListChooserPanel();
		panelGatewaysExpand = addTab(Tabs.GATEWAYS, "Gateways", createPMErrorLabel());
		
		EditorTabPluggable.runPlugins(this);
		InputFieldPluggable.runPlugins(this);
		
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
		gbc_panelBuffer.gridy = GridBagConstraints.RELATIVE;
		addTab(null, panelBuffer,gbc_panelBuffer);
		
		setEnabled(false);

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
		setFile(new File("NewFile"+unnamedcount++ + ".xml"));
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
		textfieldId.setValue("NewSimulationConfiguration");
		textfieldId.getComponent().requestFocusInWindow();
		textfieldId.getComponent().selectAll();
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
		textfieldId.loadSavedValue();
		if(creator.getRandomSeed() != null){
			textfieldSeed.setValue(creator.getRandomSeed());
		}
		if(creator.getProcessInstances() != null)spinnerNOI.setValue(Integer.parseInt(creator.getProcessInstances()));
		String startDt = creator.getStartDateTime();
		if(startDt != null){
			startDateTime = ZonedDateTime.parse(startDt);
			textfieldStartTime.setValue(startDateTime.toLocalTime());
			textfieldStartDate.setValue(startDateTime.toLocalDate());
		}
		String endDt = creator.getEndDateTime();
		if(endDt != null){
			checkboxUnlimited.setSelected(false);
			endDateTime = ZonedDateTime.parse(endDt);
			textfieldEndTime.setValue(endDateTime.toLocalTime());
			textfieldEndDate.setValue(endDateTime.toLocalDate());
		}else {
			checkboxUnlimited.setSelected(true);
			textfieldEndDate.getComponent().setEnabled(false);
			textfieldEndTime.getComponent().setEnabled(false);
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
		
		textfieldId.getComponent().setEnabled(b);
		textfieldSeed.getComponent().setEnabled(b);
		spinnerNOI.getComponent().setEnabled(b);
		textfieldStartTime.getComponent().setEnabled(b);
		textfieldStartDate.getComponent().setEnabled(b);
		textfieldEndTime.getComponent().setEnabled(b && !checkboxUnlimited.isSelected());
		textfieldEndDate.getComponent().setEnabled(b && !checkboxUnlimited.isSelected());
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
		textfieldId.reset();
		textfieldSeed.setValue(null);
		spinnerNOI.reset();
		textfieldStartTime.reset();
		textfieldStartDate.reset();
		textfieldEndTime.reset();
		textfieldEndDate.reset();
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
