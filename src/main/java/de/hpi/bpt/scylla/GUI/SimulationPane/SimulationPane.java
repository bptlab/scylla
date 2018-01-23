package de.hpi.bpt.scylla.GUI.SimulationPane;

import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.function.Consumer;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import de.hpi.bpt.scylla.SimulationManager;
import de.hpi.bpt.scylla.GUI.CheckboxListPanel;
import de.hpi.bpt.scylla.GUI.Console;
import de.hpi.bpt.scylla.GUI.EditorPane;
import de.hpi.bpt.scylla.GUI.ListPanel;
import de.hpi.bpt.scylla.GUI.ScalingCheckBoxIcon;
import de.hpi.bpt.scylla.GUI.ScalingFileChooser;
import de.hpi.bpt.scylla.GUI.ScyllaGUI;
import de.hpi.bpt.scylla.GUI.GlobalConfigurationPane.GlobalConfigurationPane;
import de.hpi.bpt.scylla.GUI.SimulationConfigurationPane.SimulationConfigurationPane;
import de.hpi.bpt.scylla.logger.DebugLogger;
import de.hpi.bpt.scylla.plugin_loader.PluginLoader;
import javax.swing.filechooser.FileFilter;


/**
 * Pane for configuring and running simulations
 * @author Leon Bein
 *
 */
@SuppressWarnings("serial")
public class SimulationPane extends JPanel{
	

	private static final FileFilter FILEFILTER_XML = new FileNameExtensionFilter("XML files","xml");
	private static final FileFilter FILEFILTER_BPMN = new FileNameExtensionFilter("BPMN files","bpmn");
	
	//Global config components
	private JLabel lblCurrentGlobalConfig;
	private FileListEntry displayCurrentGlobalConfigChosen;
	private JButton button_openglobalconfig;
	
	//BPMN file components
	private JLabel lblCurrentBpmnFiles;
	private JScrollPane scrollPane_BpmnFiles;
	private ListPanel<FileListEntry> list_CurrentBpmnFiles;
	private JButton button_openBpmnFile;
//	private JButton button_removeBpmnFile;
	
	//Simulation file components
	private JLabel lblCurrentSimulationFiles;
	private JScrollPane scrollPane_SimFiles;
	private ListPanel<FileListEntry> list_CurrentSimFiles;
	private JButton button_openSimfile;
	private JButton button_newSimfile;
	
	//Plugin components
	private JLabel lblPlugins;
	private JScrollPane scrollPane_plugins;
	private Container panel_plugins;
	private JButton button_allplugins;
	
	//Console components
	private JLabel lblConsoleOutput;
	private JScrollPane scrollPane_Console;
	private Console console;

	//Simulation components
	private JPanel panelBottom;
	private JButton button_StartSimulation;
	private JButton button_OpenLastOutput;
	private String lastOutPutFolder;
	private JButton button_AdvancedOptions;
	private JPanel panel_AdvancedOptions;
	private JCheckBox checkbox_debug;
	private JCheckBox checkbox_desmoj;
	private JButton button_newglobalconfig;
	
	/**
	 * Constructor
	 */
	public SimulationPane(ScyllaGUI parent){
		
		setBackground(ScyllaGUI.ColorBackground);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWeights = new double[]{24,1,20};
		gridBagLayout.rowWeights = new double[]{3,3,3,4.5,4.5,3,4.5,4.5,3,18,5.5};
		setLayout(gridBagLayout);
		
		int COL1 = ScyllaGUI.WIDTH/48;
		int ROW1 = ScyllaGUI.HEIGHT/36;
		
		lblCurrentGlobalConfig = new JLabel();
		lblCurrentGlobalConfig.setOpaque(true);
		lblCurrentGlobalConfig.setFont(ScyllaGUI.TITLEFONT);
		lblCurrentGlobalConfig.setBackground(ScyllaGUI.ColorField0);
		lblCurrentGlobalConfig.setForeground(ScyllaGUI.TITLEFONT_COLOR);
		lblCurrentGlobalConfig.setText(" Current Global Config ");
		GridBagConstraints gbc_lblCurrentGlobalConfig = new GridBagConstraints();
		gbc_lblCurrentGlobalConfig.fill = GridBagConstraints.BOTH;
		gbc_lblCurrentGlobalConfig.insets = new Insets(ROW1, COL1, 0, COL1);
		gbc_lblCurrentGlobalConfig.gridwidth = 2;
		gbc_lblCurrentGlobalConfig.gridx = 0;
		gbc_lblCurrentGlobalConfig.gridy = 0;
		this.add(lblCurrentGlobalConfig, gbc_lblCurrentGlobalConfig);
		
		
		displayCurrentGlobalConfigChosen = new FileListEntry(null," ", (s)->{
			EditorPane ep =  new GlobalConfigurationPane();
			File f = new File(s);
			ep.openFile(f);
			parent.addEditor(ep);
		}, false);
		displayCurrentGlobalConfigChosen.buttonEdit.setVisible(false);
		//textfield_CurrentGlobalConfig_chosen.setMargin(ScyllaGUI.LEFTMARGIN);
		displayCurrentGlobalConfigChosen.setFont(ScyllaGUI.DEFAULTFONT);
		displayCurrentGlobalConfigChosen.setToolTipText("Path for current global configuarition file");
		GridBagConstraints gbc_displayCurrentGlobalConfigChosenWrap = new GridBagConstraints();
		gbc_displayCurrentGlobalConfigChosenWrap.fill = GridBagConstraints.BOTH;
		gbc_displayCurrentGlobalConfigChosenWrap.insets = new Insets(0, COL1, 0, 0);
		gbc_displayCurrentGlobalConfigChosenWrap.gridx = 0;
		gbc_displayCurrentGlobalConfigChosenWrap.gridy = 1;
		JScrollPane panelGlobalConfigWrap = new JScrollPane();
		panelGlobalConfigWrap.setViewportView(displayCurrentGlobalConfigChosen);
		this.add(panelGlobalConfigWrap, gbc_displayCurrentGlobalConfigChosenWrap);
		
		
		Container container_globalButtons = new Container();
		container_globalButtons.setLayout(new GridLayout(2,1));
		
		button_openglobalconfig = new JButton();
		button_openglobalconfig.setIcon(ScyllaGUI.ICON_OPEN);
		button_openglobalconfig.setFont(ScyllaGUI.DEFAULTFONT);
		button_openglobalconfig.setToolTipText("Choose other file");
		button_openglobalconfig.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ScalingFileChooser chooser = new ScalingFileChooser(ScyllaGUI.DEFAULTFILEPATH);
				chooser.setDialogTitle("Choose global config");
				chooser.addChoosableFileFilter(FILEFILTER_XML);
				chooser.setFileFilter(FILEFILTER_XML);
				int c = chooser.showDialog(null,"Open");
				if(c == ScalingFileChooser.APPROVE_OPTION){
					displayCurrentGlobalConfigChosen.setText(chooser.getSelectedFile().getPath());
					displayCurrentGlobalConfigChosen.buttonEdit.setVisible(true);
					ScyllaGUI.DEFAULTFILEPATH = chooser.getSelectedFile().getPath();
				}
			}
		});
		button_newglobalconfig = new JButton();
		button_newglobalconfig.setIcon(ScyllaGUI.ICON_NEW);
		button_newglobalconfig.setFont(ScyllaGUI.DEFAULTFONT);
		button_newglobalconfig.setToolTipText("Create new global configuration file");
		button_newglobalconfig.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				EditorPane ep =  new GlobalConfigurationPane();
				ep.be_create();
				parent.addEditor(ep);
			}
		});
		GridBagConstraints gbc_container_globalButtons = new GridBagConstraints();
		gbc_container_globalButtons.fill = GridBagConstraints.BOTH;
		gbc_container_globalButtons.insets = new Insets(0, 0, 0, COL1);
		gbc_container_globalButtons.gridx = 1;
		gbc_container_globalButtons.gridy = 1;
		container_globalButtons.add(button_openglobalconfig);
		container_globalButtons.add(button_newglobalconfig);
		this.add(container_globalButtons, gbc_container_globalButtons);
		
		
		lblCurrentBpmnFiles = new JLabel();
		lblCurrentBpmnFiles.setOpaque(true);
		lblCurrentBpmnFiles.setFont(ScyllaGUI.TITLEFONT);
		lblCurrentBpmnFiles.setBackground(ScyllaGUI.ColorField0);
		lblCurrentBpmnFiles.setForeground(ScyllaGUI.TITLEFONT_COLOR);
		lblCurrentBpmnFiles.setText(" Current BPMN Files");
		GridBagConstraints gbc_lblCurrentBpmnFiles = new GridBagConstraints();
		gbc_lblCurrentBpmnFiles.fill = GridBagConstraints.BOTH;
		gbc_lblCurrentBpmnFiles.insets = new Insets(ROW1, COL1, 0, COL1);
		gbc_lblCurrentBpmnFiles.gridwidth = 2;
		gbc_lblCurrentBpmnFiles.gridx = 0;
		gbc_lblCurrentBpmnFiles.gridy = 2;
		this.add(lblCurrentBpmnFiles, gbc_lblCurrentBpmnFiles);
		
		scrollPane_BpmnFiles = new JScrollPane();
		scrollPane_BpmnFiles.setFont(ScyllaGUI.DEFAULTFONT);
		scrollPane_BpmnFiles.setToolTipText("");
		GridBagConstraints gbc_scrollPane_BpmnFiles = new GridBagConstraints();
		gbc_scrollPane_BpmnFiles.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_BpmnFiles.insets = new Insets(0, COL1, 0, 0);
		gbc_scrollPane_BpmnFiles.gridheight = 2;
		gbc_scrollPane_BpmnFiles.gridx = 0;
		gbc_scrollPane_BpmnFiles.gridy = 3;
		this.add(scrollPane_BpmnFiles, gbc_scrollPane_BpmnFiles);
		
		list_CurrentBpmnFiles = new ListPanel<FileListEntry>();
		list_CurrentBpmnFiles.setBorder(new EmptyBorder(ScyllaGUI.LEFTMARGIN));
		list_CurrentBpmnFiles.setFont(ScyllaGUI.DEFAULTFONT);
		scrollPane_BpmnFiles.setViewportView(list_CurrentBpmnFiles);
		
		button_openBpmnFile = new JButton("");
		button_openBpmnFile.setFont(ScyllaGUI.DEFAULTFONT);
		button_openBpmnFile.setToolTipText("Add BPMN file");
		button_openBpmnFile.setIcon(ScyllaGUI.ICON_OPEN);
		button_openBpmnFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ScalingFileChooser chooser = new ScalingFileChooser(ScyllaGUI.DEFAULTFILEPATH);
				chooser.addChoosableFileFilter(FILEFILTER_BPMN);
				chooser.setFileFilter(FILEFILTER_BPMN);
				chooser.setDialogTitle("Add business process diagram");
				int c = chooser.showDialog(null,"Open");
				if(c == ScalingFileChooser.APPROVE_OPTION){
					chooser.getSelectedFile();
					list_CurrentBpmnFiles.addElement(new FileListEntry(list_CurrentBpmnFiles,chooser.getSelectedFile().getPath(),null, true));
					ScyllaGUI.DEFAULTFILEPATH = chooser.getSelectedFile().getPath();
				}
			}
		});
		GridBagConstraints gbc_button_openBpmnFile = new GridBagConstraints();
		gbc_button_openBpmnFile.fill = GridBagConstraints.BOTH;
		gbc_button_openBpmnFile.insets = new Insets(0, 0, 0, COL1);
		gbc_button_openBpmnFile.gridx = 1;
		gbc_button_openBpmnFile.gridy = 3;
		gbc_button_openBpmnFile.gridheight = 2;
		this.add(button_openBpmnFile, gbc_button_openBpmnFile);
		
//		button_removeBpmnFile = new JButton("");
//		button_removeBpmnFile.setFont(ScyllaGUI.DEFAULTFONT);
//		button_removeBpmnFile.setToolTipText("Remove selected file(s)");
//		button_removeBpmnFile.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				//List<ListEntry> remove = list_CurrentBpmnFiles.getSelectedValuesList();
//				//for(int i = 0; i < remove.size(); i++)m.removeElement(remove.get(i));
//			}
//		});
//		button_removeBpmnFile.setIcon(ScyllaGUI.ICON_X);
//		GridBagConstraints gbc_button_removeBpmnFile = new GridBagConstraints();
//		gbc_button_removeBpmnFile.fill = GridBagConstraints.BOTH;
//		gbc_button_removeBpmnFile.insets = new Insets(0, 0, 0, COL1);
//		gbc_button_removeBpmnFile.gridx = 1;
//		gbc_button_removeBpmnFile.gridy = 4;
//		this.add(button_removeBpmnFile, gbc_button_removeBpmnFile);
		
		
		lblCurrentSimulationFiles = new JLabel();
		lblCurrentSimulationFiles.setOpaque(true);
		lblCurrentSimulationFiles.setFont(ScyllaGUI.TITLEFONT);
		lblCurrentSimulationFiles.setBackground(ScyllaGUI.ColorField0);
		lblCurrentSimulationFiles.setForeground(ScyllaGUI.TITLEFONT_COLOR);
		lblCurrentSimulationFiles.setText(" Current Simulation Files");
		GridBagConstraints gbc_lblCurrentSimulationFiles = new GridBagConstraints();
		gbc_lblCurrentSimulationFiles.fill = GridBagConstraints.BOTH;
		gbc_lblCurrentSimulationFiles.insets = new Insets(ROW1, COL1, 0, COL1);
		gbc_lblCurrentSimulationFiles.gridwidth = 2;
		gbc_lblCurrentSimulationFiles.gridx = 0;
		gbc_lblCurrentSimulationFiles.gridy = 5;
		this.add(lblCurrentSimulationFiles, gbc_lblCurrentSimulationFiles);
		
		scrollPane_SimFiles = new JScrollPane();
		scrollPane_SimFiles.setFont(ScyllaGUI.DEFAULTFONT);
		scrollPane_SimFiles.setToolTipText("");
		GridBagConstraints gbc_scrollPane_SimFiles = new GridBagConstraints();
		gbc_scrollPane_SimFiles.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_SimFiles.insets = new Insets(0, COL1, 0, 0);
		gbc_scrollPane_SimFiles.gridheight = 2;
		gbc_scrollPane_SimFiles.gridx = 0;
		gbc_scrollPane_SimFiles.gridy = 6;
		this.add(scrollPane_SimFiles, gbc_scrollPane_SimFiles);
		
		list_CurrentSimFiles = new ListPanel<FileListEntry>();
		list_CurrentSimFiles.setBorder(new EmptyBorder(ScyllaGUI.LEFTMARGIN));
		list_CurrentSimFiles.setFont(ScyllaGUI.DEFAULTFONT);
		scrollPane_SimFiles.setViewportView(list_CurrentSimFiles);
		
		button_openSimfile = new JButton("");
		button_openSimfile.setFont(ScyllaGUI.DEFAULTFONT);
		button_openSimfile.setToolTipText("Add simulation file");
		button_openSimfile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ScalingFileChooser chooser = new ScalingFileChooser(ScyllaGUI.DEFAULTFILEPATH);
				chooser.setDialogTitle("Add simulation file");
				chooser.addChoosableFileFilter(FILEFILTER_XML);
				chooser.setFileFilter(FILEFILTER_XML);
				int c = chooser.showDialog(null,"Open");
				if(c == ScalingFileChooser.APPROVE_OPTION){
					list_CurrentSimFiles.addElement(new FileListEntry(list_CurrentSimFiles,chooser.getSelectedFile().getPath(),(s)->{
						EditorPane ep =  new SimulationConfigurationPane();
						File f = new File(s);
						ep.openFile(f);
						parent.addEditor(ep);
					},true));
					ScyllaGUI.DEFAULTFILEPATH = chooser.getSelectedFile().getPath();
				}
				
			}
		});
		button_openSimfile.setIcon(ScyllaGUI.ICON_OPEN);
		GridBagConstraints gbc_button_openSimfile = new GridBagConstraints();
		gbc_button_openSimfile.fill = GridBagConstraints.BOTH;
		gbc_button_openSimfile.insets = new Insets(0, 0, 0, COL1);
		gbc_button_openSimfile.gridx = 1;
		gbc_button_openSimfile.gridy = 6;
		this.add(button_openSimfile, gbc_button_openSimfile);

		button_newSimfile = new JButton();
		button_newSimfile.setFont(ScyllaGUI.DEFAULTFONT);
		button_newSimfile.setToolTipText("Create new simulation configuration file");
		button_newSimfile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				EditorPane ep =  new SimulationConfigurationPane();
				ep.be_create();
				parent.addEditor(ep);
			}
		});
		button_newSimfile.setIcon(ScyllaGUI.ICON_NEW);
		GridBagConstraints gbc_button_newSimfile = new GridBagConstraints();
		gbc_button_newSimfile.fill = GridBagConstraints.BOTH;
		gbc_button_newSimfile.insets = new Insets(0, 0, 0, COL1);
		gbc_button_newSimfile.gridx = 1;
		gbc_button_newSimfile.gridy = 7;
		this.add(button_newSimfile, gbc_button_newSimfile);
		
		
		lblPlugins = new JLabel();
		lblPlugins.setOpaque(true);
		lblPlugins.setFont(ScyllaGUI.TITLEFONT);
		lblPlugins.setBackground(ScyllaGUI.ColorField0);
		lblPlugins.setForeground(ScyllaGUI.TITLEFONT_COLOR);
		lblPlugins.setText(" Plugins");
		GridBagConstraints gbc_lblPlugins = new GridBagConstraints();
		gbc_lblPlugins.fill = GridBagConstraints.BOTH;
		gbc_lblPlugins.insets = new Insets(ROW1, 0, 0, COL1);
		gbc_lblPlugins.gridx = 2;
		gbc_lblPlugins.gridy = 0;
		this.add(lblPlugins, gbc_lblPlugins);
		
		scrollPane_plugins = new JScrollPane();
		scrollPane_plugins.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane_plugins.getVerticalScrollBar().setUnitIncrement(16);
		scrollPane_plugins.setToolTipText("Plugin List");
		scrollPane_plugins.setFont(ScyllaGUI.DEFAULTFONT);
		GridBagConstraints gbc_scrollPane_plugins = new GridBagConstraints();
		gbc_scrollPane_plugins.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_plugins.insets = new Insets(0, 0, 0, COL1);
		gbc_scrollPane_plugins.gridheight = 7;
		gbc_scrollPane_plugins.gridx = 2;
		gbc_scrollPane_plugins.gridy = 1;
		this.add(scrollPane_plugins, gbc_scrollPane_plugins);
		
		panel_plugins = new Container();
		panel_plugins.setFont(ScyllaGUI.DEFAULTFONT);
		panel_plugins.setLayout(new GridBagLayout());
		
		scrollPane_plugins.setViewportView(panel_plugins);
		scrollPane_plugins.getViewport().setBackground(ScyllaGUI.ColorField2);
		
		button_allplugins = new JButton("Select/Deselect all");
		button_allplugins.addActionListener(new ActionListener() {
			boolean b = false;
			public void actionPerformed(ActionEvent arg0) {
				for(Component c : panel_plugins.getComponents()){
					if(c instanceof CheckboxListPanel)((CheckboxListPanel)c).setSelected(b);
				}
				b = !b;
			}
		});
		button_allplugins.setToolTipText("Select/Deselect all plugins");
		button_allplugins.setFont(ScyllaGUI.DEFAULTFONT);
		scrollPane_plugins.setColumnHeaderView(button_allplugins);
	
		
		lblConsoleOutput = new JLabel();
		lblConsoleOutput.setOpaque(true);
		lblConsoleOutput.setText(" Console Output");
		lblConsoleOutput.setFont(ScyllaGUI.TITLEFONT);
		lblConsoleOutput.setBackground(ScyllaGUI.ColorField0);
		lblConsoleOutput.setForeground(ScyllaGUI.TITLEFONT_COLOR);
		GridBagConstraints gbc_lblConsoleOutput = new GridBagConstraints();
		gbc_lblConsoleOutput.fill = GridBagConstraints.BOTH;
		gbc_lblConsoleOutput.insets = new Insets(ROW1, COL1, 0, COL1);
		gbc_lblConsoleOutput.gridwidth = 3;
		gbc_lblConsoleOutput.gridx = 0;
		gbc_lblConsoleOutput.gridy = 8;
		this.add(lblConsoleOutput, gbc_lblConsoleOutput);
		
		scrollPane_Console = new JScrollPane();
		GridBagConstraints gbc_scrollPane_Console = new GridBagConstraints();
		gbc_scrollPane_Console.anchor = GridBagConstraints.NORTH;
		gbc_scrollPane_Console.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_Console.insets = new Insets(0, COL1, ROW1, COL1);
		gbc_scrollPane_Console.gridwidth = 3;
		gbc_scrollPane_Console.gridx = 0;
		gbc_scrollPane_Console.gridy = 9;
		this.add(scrollPane_Console, gbc_scrollPane_Console);
		
		console = new Console(){
			@Override
			public Dimension getPreferredScrollableViewportSize() {
				Dimension d = super.getPreferredScrollableViewportSize();
				d.setSize(d.getWidth(), 350.0*ScyllaGUI.SCALE);
				return d;
			}
		};
		console.setHighlighter(null);
		console.setFont(ScyllaGUI.CONSOLEFONT);
		console.setBackground(ScyllaGUI.ColorField1);
		console.setEditable(false);
		scrollPane_Console.setViewportView(console);
		console.setMargin(ScyllaGUI.LEFTMARGIN);
		
		panelBottom = new JPanel();
		panelBottom.setBackground(getBackground());
		GridBagConstraints gbc_panelBottom = new GridBagConstraints();
		gbc_panelBottom.fill = GridBagConstraints.BOTH;
		gbc_panelBottom.gridx = 0;
		gbc_panelBottom.gridy = 10;
		gbc_panelBottom.gridwidth = 3;
		gbc_panelBottom.insets = new Insets(0, 0, ROW1, 0);
		GridBagLayout gbl_panelBottom = new GridBagLayout();
		gbl_panelBottom.columnWeights = new double[]{23,1,24};
		gbl_panelBottom.rowWeights = new double[]{10,1};
		panelBottom.setLayout(gbl_panelBottom);
		add(panelBottom,gbc_panelBottom);
		
		
		button_StartSimulation = new JButton("Start Simulation");
		button_StartSimulation.setFont(ScyllaGUI.TITLEFONT);
		button_StartSimulation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				String[] bpmnFilenames = new String[list_CurrentBpmnFiles.getListSize()];
				for(int i = 0; i < bpmnFilenames.length; i++){
					bpmnFilenames[i] = list_CurrentBpmnFiles.getElementAt(i).getText();
				}
				String[] simFilenames = new String[list_CurrentSimFiles.getListSize()];
				for(int i = 0; i < simFilenames.length; i++){
					simFilenames[i] = list_CurrentSimFiles.getElementAt(i).getText();
				}
				
				startSimulation(
						displayCurrentGlobalConfigChosen.getText(),
						bpmnFilenames,
						simFilenames
						);
			}
		});
		GridBagConstraints gbc_button_StartSimulation = new GridBagConstraints();
		gbc_button_StartSimulation.anchor = GridBagConstraints.EAST;
		gbc_button_StartSimulation.fill = GridBagConstraints.BOTH;
		gbc_button_StartSimulation.insets = new Insets(0, 20*COL1, 0, 0);
		gbc_button_StartSimulation.gridx = 0;
		gbc_button_StartSimulation.gridy = 0;
		panelBottom.add(button_StartSimulation, gbc_button_StartSimulation);
		
		
		button_OpenLastOutput = new JButton("Open Last Output Path");
		button_OpenLastOutput.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					Desktop.getDesktop().open(new File(lastOutPutFolder));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		button_OpenLastOutput.setFont(ScyllaGUI.DEFAULTFONT);
		button_OpenLastOutput.setEnabled(false);
		GridBagConstraints gbc_button_OpenLastOutput = new GridBagConstraints();
		gbc_button_OpenLastOutput.anchor = GridBagConstraints.EAST;
		gbc_button_OpenLastOutput.fill = GridBagConstraints.BOTH;
		gbc_button_OpenLastOutput.insets = new Insets(0, 20*COL1, 0, 0);
		gbc_button_OpenLastOutput.gridx = 0;
		gbc_button_OpenLastOutput.gridy = 1;
		panelBottom.add(button_OpenLastOutput, gbc_button_OpenLastOutput);
			
		panel_AdvancedOptions = new JPanel();
		JPanel panel_AdvancedOptionsWrapper = new JPanel();
		panel_AdvancedOptionsWrapper.setLayout(new GridLayout());
		panel_AdvancedOptionsWrapper.setBackground(getBackground());
		GridBagConstraints gbc_panel_AdvancedOptions = new GridBagConstraints();
		gbc_panel_AdvancedOptions.anchor = GridBagConstraints.WEST;
		gbc_panel_AdvancedOptions.fill = GridBagConstraints.BOTH;
		gbc_panel_AdvancedOptions.gridheight = 2;
		gbc_panel_AdvancedOptions.gridx = 2;
		gbc_panel_AdvancedOptions.gridy = 0;
		gbc_panel_AdvancedOptions.insets = new Insets(0, 0, 0, 12*COL1);
		panel_AdvancedOptionsWrapper.add(panel_AdvancedOptions);
		panelBottom.add(panel_AdvancedOptionsWrapper, gbc_panel_AdvancedOptions);
		panel_AdvancedOptions.setLayout(new BoxLayout(panel_AdvancedOptions, BoxLayout.Y_AXIS));
		panel_AdvancedOptions.setVisible(false);
		
		checkbox_debug = new JCheckBox("Debug Mode");
		checkbox_debug.setHorizontalAlignment(SwingConstants.LEFT);
		checkbox_debug.setToolTipText("Runs simulation in Debug Mode. Additional output is shown.");
		checkbox_debug.setFont(ScyllaGUI.DEFAULTFONT);
		checkbox_debug.setIcon(new ScalingCheckBoxIcon(ScyllaGUI.DEFAULTFONT.getSize()));
		panel_AdvancedOptions.add(checkbox_debug);
		
		checkbox_desmoj = new JCheckBox("Desmoj Output");
		checkbox_desmoj.setToolTipText("Create Desmoj output files.");
		checkbox_desmoj.setHorizontalAlignment(SwingConstants.LEFT);
		checkbox_desmoj.setFont(ScyllaGUI.DEFAULTFONT);
		checkbox_desmoj.setIcon(new ScalingCheckBoxIcon(ScyllaGUI.DEFAULTFONT.getSize()));
		panel_AdvancedOptions.add(checkbox_desmoj);		button_AdvancedOptions = new JButton("");
		button_AdvancedOptions.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				panel_AdvancedOptions.setVisible(!panel_AdvancedOptions.isVisible());
			}
		});	
		button_AdvancedOptions.setToolTipText("Show Advanced Simulation Options");
		button_AdvancedOptions.setIcon(ScyllaGUI.ICON_OPTIONS);
		GridBagConstraints gbc_button_AdvancedOptions = new GridBagConstraints();
		gbc_button_AdvancedOptions.fill = GridBagConstraints.BOTH;
		gbc_button_AdvancedOptions.insets = new Insets(0, 0, 0, 0);
		gbc_button_AdvancedOptions.gridheight = 2;
		gbc_button_AdvancedOptions.gridx = 1;
		gbc_button_AdvancedOptions.gridy = 0;
		panelBottom.add(button_AdvancedOptions, gbc_button_AdvancedOptions);
		
		lastOutPutFolder = "";
		

		setBounds(new Rectangle(1200,900));
		loadPlugins();
	}
	
	/**
	 * Loads the plugins from PluginLoader class into the plugin panel
	 */
	private void loadPlugins() {
		if(!ScyllaGUI.DEBUG)System.setErr(console.getErr());
		PluginLoader p = PluginLoader.getDefaultPluginLoader();
		
		TreeMap<String, ArrayList<PluginLoader.PluginWrapper>> plugins = new TreeMap<String, ArrayList<PluginLoader.PluginWrapper>>();
		for(Entry<Class<?>, ArrayList<PluginLoader.PluginWrapper>> e : p.getExtensions().entrySet()){
			ArrayList<PluginLoader.PluginWrapper> l = e.getValue();
			for(int i = 0; i < l.size(); i++){
				PluginLoader.PluginWrapper w = l.get(i);
				String name = w.getPackage().getName();
				if(!plugins.containsKey(name))plugins.put(name,new ArrayList<PluginLoader.PluginWrapper>());
				plugins.get(name).add(w);
			}
		}
		
		int i = 0;
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.PAGE_START;
		gbc.weightx = 1.0;
		gbc.weighty = 0;
		gbc.ipady = 0;
		gbc.insets = new Insets(0,0,(int)(5.0*ScyllaGUI.SCALE),0);
		for(Entry<String, ArrayList<PluginLoader.PluginWrapper>> e : plugins.entrySet()){
			CheckboxListPanel listpanel = new CheckboxListPanel(e.getKey(),e.getValue());
			gbc.gridy = i;
			if(i == plugins.entrySet().size()-1)gbc.weighty = 1.0;
			panel_plugins.add(listpanel,gbc);
			i++;
			//gbc.anchor = GridBagConstraints.BASELINE;
		}
	}
	
	/**
	 * Starts the simulation
	 * @param resFilename : Filename of the global configuration
	 * @param bpmnFilenames : Array of bpmn file names
	 * @param simFilenames : Array of sim file names
	 */
	private synchronized void startSimulation(String resFilename, String[] bpmnFilenames, String[] simFilenames) {

		button_StartSimulation.setText("Running ...");
        button_StartSimulation.setEnabled(false);
        
        new Thread(()->{
        	boolean enableDebugLogging = checkbox_debug.isSelected();
            boolean redirectErrors = checkbox_debug.isSelected();
            boolean enableDesmojLogging = checkbox_desmoj.isSelected();
            
            DebugLogger.allowDebugLogging = enableDebugLogging;
            if(redirectErrors) System.setErr(console.getErr());
            else System.setErr(ScyllaGUI.getStdErr());
            
            boolean enableBpsLogging = true;
            
            boolean success = true;

            SimulationManager manager = new SimulationManager(ScyllaGUI.DESMOJOUTPUTPATH, bpmnFilenames, simFilenames, resFilename,
                    enableBpsLogging, enableDesmojLogging);
            try{
            	System.out.println("Starting simulation at "+new SimpleDateFormat("HH:mm:ss").format(new Date()));
                manager.run();
            }catch(Exception e){
            	e.printStackTrace();
            	System.out.println("Fatal error, simulation has been canceled");
            	success = false;
            }
            if(success){
            	System.out.println("Finished simulation at "+new SimpleDateFormat("HH:mm:ss").format(new Date()));
                lastOutPutFolder = manager.getOutputPath();
            	button_OpenLastOutput.setEnabled(true);
            }
            System.out.println();
            button_StartSimulation.setEnabled(true);
    		button_StartSimulation.setText("Start Simulation");
        }).start();
	}
	
	public Console getConsole() {
		return console;
	}
	
	
	private class FileListEntry extends JPanel{
		private JLabel labelText;
		private JButton buttonEdit;
		private JButton buttonRemove;

		public FileListEntry(ListPanel<FileListEntry> parent, String text, Consumer<String> onEdit, boolean hasRemove) {
			int textSize = ScyllaGUI.DEFAULTFONT.getSize();
			
			setBackground(ScyllaGUI.ColorField2);
			
			GridBagLayout gridBagLayout = new GridBagLayout();
			gridBagLayout.columnWeights = new double[]{1.0, 0.0, 0.0};
			setLayout(gridBagLayout);
			
			labelText = new JLabel(text);
			GridBagConstraints gbc_labelText = new GridBagConstraints();
			gbc_labelText.fill = GridBagConstraints.HORIZONTAL;
			gbc_labelText.insets = new Insets(0, 0, 0, 0);
			gbc_labelText.gridx = 0;
			gbc_labelText.gridy = 0;
			add(labelText, gbc_labelText);
			
			if(onEdit != null) {
				buttonEdit = new JButton();
				buttonEdit.setBackground(getBackground());
				buttonEdit.addActionListener((e)->{
					onEdit.accept(getText());
				});
				buttonEdit.setBorderPainted(false);
				buttonEdit.setRolloverEnabled(true);
				//buttonEdit.setContentAreaFilled(false);
				buttonEdit.setIcon(ScyllaGUI.resizeIcon(ScyllaGUI.ICON_EDIT, textSize, textSize));
				GridBagConstraints gbc_buttonEdit = new GridBagConstraints();
				gbc_buttonEdit.insets = new Insets(0, 0, 0, 0);
				gbc_buttonEdit.gridx = 1;
				gbc_buttonEdit.gridy = 0;
				add(buttonEdit, gbc_buttonEdit);
			}
			
			if(hasRemove) {
				buttonRemove = new JButton();
				buttonRemove.setBackground(getBackground());
				buttonRemove.addActionListener((e)->{
					parent.removeElement(this);
				});
				buttonRemove.setBorderPainted(false);
				buttonRemove.setRolloverEnabled(true);
				//buttonRemove.setContentAreaFilled(false);
				buttonRemove.setIcon(ScyllaGUI.resizeIcon(ScyllaGUI.ICON_CLOSE, textSize, textSize));
				GridBagConstraints gbc_buttonRemove = new GridBagConstraints();
				gbc_buttonRemove.insets = new Insets(0, 0, 0, 0);
				gbc_buttonRemove.gridx = 2;
				gbc_buttonRemove.gridy = 0;
				add(buttonRemove, gbc_buttonRemove);
			}
			
		}
		
		public void setText(String text) {
			labelText.setText(text);
		}
		
		public String getText() {
			return labelText.getText();
		}

	}

}
