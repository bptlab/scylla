package de.hpi.bpt.scylla.GUI;

import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import de.hpi.bpt.scylla.SimulationManager;
import de.hpi.bpt.scylla.logger.DebugLogger;
import de.hpi.bpt.scylla.plugin_loader.PluginLoader;
import java.awt.Insets;
/**
 * Pane for configuring and running simulations
 * @author Leon Bein
 *
 */
@SuppressWarnings("serial")
public class SimulationPane extends JPanel{
	
	//Global config components
	private JLabel label_CurrentGlobalConfig;
	private JTextField textfield_CurrentGlobalConfig_chosen;
	private JButton button_openglobalconfig;
	
	//BPMN file components
	private JLabel label_CurrentBpmnFiles;
	private JScrollPane scrollPane_BpmnFiles;
	private JList<String> list_CurrentBpmnFiles;
	private JButton button_addBpmnFile;
	private JButton button_removeBpmnFile;
	
	//Simulation file components
	private JLabel label_CurrentSimulationFiles;
	private JScrollPane scrollPane_SimFiles;
	private JList<String> list_CurrentSimFiles;
	private JButton button_addSimfile;
	private JButton button_removeSimfile;
	
	//Plugin components
	private JLabel label_Plugins;
	private JScrollPane scrollPane_plugins;
	private Container panel_plugins;
	private JButton button_allplugins;
	
	//Console components
	private JLabel label_Console;
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
	
	/**
	 * Constructor
	 */
	public SimulationPane(){
		
		setBackground(ScyllaGUI.ColorBackground);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWeights = new double[]{24,1,20};
		gridBagLayout.rowWeights = new double[]{3,3,3,4.5,4.5,3,4.5,4.5,3,18,5.5};
		setLayout(gridBagLayout);
		
		int COL1 = ScyllaGUI.WIDTH/48;
		int ROW1 = ScyllaGUI.HEIGHT/36;
		
		label_CurrentGlobalConfig = new JLabel();
		label_CurrentGlobalConfig.setOpaque(true);
		label_CurrentGlobalConfig.setFont(ScyllaGUI.TITLEFONT);
		label_CurrentGlobalConfig.setBackground(ScyllaGUI.ColorField0);
		label_CurrentGlobalConfig.setForeground(ScyllaGUI.TITLEFONT_COLOR);
		label_CurrentGlobalConfig.setText("Current Global Config ");
		GridBagConstraints gbc_label_CurrentGlobalConfig = new GridBagConstraints();
		gbc_label_CurrentGlobalConfig.fill = GridBagConstraints.BOTH;
		gbc_label_CurrentGlobalConfig.insets = new Insets(ROW1, COL1, 0, COL1);
		gbc_label_CurrentGlobalConfig.gridwidth = 2;
		gbc_label_CurrentGlobalConfig.gridx = 0;
		gbc_label_CurrentGlobalConfig.gridy = 0;
		this.add(label_CurrentGlobalConfig, gbc_label_CurrentGlobalConfig);
		
		textfield_CurrentGlobalConfig_chosen = new JTextField();
		textfield_CurrentGlobalConfig_chosen.setMargin(ScyllaGUI.LEFTMARGIN);
		textfield_CurrentGlobalConfig_chosen.setFont(ScyllaGUI.DEFAULTFONT);
		textfield_CurrentGlobalConfig_chosen.setToolTipText("Path for current global configuarition file");
		textfield_CurrentGlobalConfig_chosen.setEditable(false);
		GridBagConstraints gbc_textfield_CurrentGlobalConfig_chosen = new GridBagConstraints();
		gbc_textfield_CurrentGlobalConfig_chosen.fill = GridBagConstraints.BOTH;
		gbc_textfield_CurrentGlobalConfig_chosen.insets = new Insets(0, COL1, 0, 0);
		gbc_textfield_CurrentGlobalConfig_chosen.gridx = 0;
		gbc_textfield_CurrentGlobalConfig_chosen.gridy = 1;
		this.add(textfield_CurrentGlobalConfig_chosen, gbc_textfield_CurrentGlobalConfig_chosen);
		textfield_CurrentGlobalConfig_chosen.setColumns(10);
		
		button_openglobalconfig = new JButton();
		button_openglobalconfig.setIcon(ScyllaGUI.ICON_MORE);
		button_openglobalconfig.setFont(ScyllaGUI.DEFAULTFONT);
		button_openglobalconfig.setToolTipText("Choose other file");
		button_openglobalconfig.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ScalingFileChooser chooser = new ScalingFileChooser(ScyllaGUI.DEFAULTFILEPATH);
				chooser.setDialogTitle("Choose global config");
				int c = chooser.showDialog(null,"Open");
				if(c == ScalingFileChooser.APPROVE_OPTION){
					textfield_CurrentGlobalConfig_chosen.setText(chooser.getSelectedFile().getPath());
					ScyllaGUI.DEFAULTFILEPATH = chooser.getSelectedFile().getPath();
				}
			}
		});
		GridBagConstraints gbc_button_openglobalconfig = new GridBagConstraints();
		gbc_button_openglobalconfig.fill = GridBagConstraints.BOTH;
		gbc_button_openglobalconfig.insets = new Insets(0, 0, 0, COL1);
		gbc_button_openglobalconfig.gridx = 1;
		gbc_button_openglobalconfig.gridy = 1;
		this.add(button_openglobalconfig, gbc_button_openglobalconfig);
		
		
		label_CurrentBpmnFiles = new JLabel();
		label_CurrentBpmnFiles.setOpaque(true);
		label_CurrentBpmnFiles.setFont(ScyllaGUI.TITLEFONT);
		label_CurrentBpmnFiles.setBackground(ScyllaGUI.ColorField0);
		label_CurrentBpmnFiles.setForeground(ScyllaGUI.TITLEFONT_COLOR);
		label_CurrentBpmnFiles.setText("Current BPMN Files");
		GridBagConstraints gbc_label_CurrentBpmnFiles = new GridBagConstraints();
		gbc_label_CurrentBpmnFiles.fill = GridBagConstraints.BOTH;
		gbc_label_CurrentBpmnFiles.insets = new Insets(ROW1, COL1, 0, COL1);
		gbc_label_CurrentBpmnFiles.gridwidth = 2;
		gbc_label_CurrentBpmnFiles.gridx = 0;
		gbc_label_CurrentBpmnFiles.gridy = 2;
		this.add(label_CurrentBpmnFiles, gbc_label_CurrentBpmnFiles);
		
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
		
		list_CurrentBpmnFiles = new JList<String>();
		list_CurrentBpmnFiles.setBorder(new EmptyBorder(ScyllaGUI.LEFTMARGIN));
		list_CurrentBpmnFiles.setFont(ScyllaGUI.DEFAULTFONT);
		list_CurrentBpmnFiles.setModel(new DefaultListModel<>());
		list_CurrentBpmnFiles.setDragEnabled(true);
		scrollPane_BpmnFiles.setViewportView(list_CurrentBpmnFiles);
		
		button_addBpmnFile = new JButton("");
		button_addBpmnFile.setFont(ScyllaGUI.DEFAULTFONT);
		button_addBpmnFile.setToolTipText("Add BPMN file");
		button_addBpmnFile.setIcon(ScyllaGUI.ICON_PLUS);
		button_addBpmnFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ScalingFileChooser chooser = new ScalingFileChooser(ScyllaGUI.DEFAULTFILEPATH);
				chooser.setDialogTitle("Add business process diagram");
				int c = chooser.showDialog(null,"Open");
				if(c == ScalingFileChooser.APPROVE_OPTION){
					DefaultListModel<String> m = (DefaultListModel<String>) list_CurrentBpmnFiles.getModel();
					chooser.getSelectedFile();
					m.addElement(chooser.getSelectedFile().getPath());
					ScyllaGUI.DEFAULTFILEPATH = chooser.getSelectedFile().getPath();
				}
			}
		});
		GridBagConstraints gbc_button_addBpmnFile = new GridBagConstraints();
		gbc_button_addBpmnFile.fill = GridBagConstraints.BOTH;
		gbc_button_addBpmnFile.insets = new Insets(0, 0, 0, COL1);
		gbc_button_addBpmnFile.gridx = 1;
		gbc_button_addBpmnFile.gridy = 3;
		this.add(button_addBpmnFile, gbc_button_addBpmnFile);
		
		button_removeBpmnFile = new JButton("");
		button_removeBpmnFile.setFont(ScyllaGUI.DEFAULTFONT);
		button_removeBpmnFile.setToolTipText("Remove selected file(s)");
		button_removeBpmnFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DefaultListModel<String> m = (DefaultListModel<String>) list_CurrentBpmnFiles.getModel();
				List<String> remove = list_CurrentBpmnFiles.getSelectedValuesList();
				for(int i = 0; i < remove.size(); i++)m.removeElement(remove.get(i));
			}
		});
		button_removeBpmnFile.setIcon(ScyllaGUI.ICON_X);
		GridBagConstraints gbc_button_removeBpmnFile = new GridBagConstraints();
		gbc_button_removeBpmnFile.fill = GridBagConstraints.BOTH;
		gbc_button_removeBpmnFile.insets = new Insets(0, 0, 0, COL1);
		gbc_button_removeBpmnFile.gridx = 1;
		gbc_button_removeBpmnFile.gridy = 4;
		this.add(button_removeBpmnFile, gbc_button_removeBpmnFile);
		
		
		label_CurrentSimulationFiles = new JLabel();
		label_CurrentSimulationFiles.setOpaque(true);
		label_CurrentSimulationFiles.setFont(ScyllaGUI.TITLEFONT);
		label_CurrentSimulationFiles.setBackground(ScyllaGUI.ColorField0);
		label_CurrentSimulationFiles.setForeground(ScyllaGUI.TITLEFONT_COLOR);
		label_CurrentSimulationFiles.setText("Current Simulation Files");
		GridBagConstraints gbc_label_CurrentSimulationFiles = new GridBagConstraints();
		gbc_label_CurrentSimulationFiles.fill = GridBagConstraints.BOTH;
		gbc_label_CurrentSimulationFiles.insets = new Insets(ROW1, COL1, 0, COL1);
		gbc_label_CurrentSimulationFiles.gridwidth = 2;
		gbc_label_CurrentSimulationFiles.gridx = 0;
		gbc_label_CurrentSimulationFiles.gridy = 5;
		this.add(label_CurrentSimulationFiles, gbc_label_CurrentSimulationFiles);
		
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
		
		list_CurrentSimFiles = new JList<String>();
		list_CurrentSimFiles.setBorder(new EmptyBorder(ScyllaGUI.LEFTMARGIN));
		list_CurrentSimFiles.setFont(ScyllaGUI.DEFAULTFONT);
		list_CurrentSimFiles.setModel(new DefaultListModel<>());
		list_CurrentSimFiles.setDragEnabled(true);
		scrollPane_SimFiles.setViewportView(list_CurrentSimFiles);
		
		button_addSimfile = new JButton("");
		button_addSimfile.setFont(ScyllaGUI.DEFAULTFONT);
		button_addSimfile.setToolTipText("Add simulation file");
		button_addSimfile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ScalingFileChooser chooser = new ScalingFileChooser(ScyllaGUI.DEFAULTFILEPATH);
				chooser.setDialogTitle("Add simulation file");
				int c = chooser.showDialog(null,"Open");
				if(c == ScalingFileChooser.APPROVE_OPTION){
					DefaultListModel<String> m = (DefaultListModel<String>) list_CurrentSimFiles.getModel();
					m.addElement(chooser.getSelectedFile().getPath());
					ScyllaGUI.DEFAULTFILEPATH = chooser.getSelectedFile().getPath();
				}
				
			}
		});
		button_addSimfile.setIcon(ScyllaGUI.ICON_PLUS);
		GridBagConstraints gbc_button_addSimfile = new GridBagConstraints();
		gbc_button_addSimfile.fill = GridBagConstraints.BOTH;
		gbc_button_addSimfile.insets = new Insets(0, 0, 0, COL1);
		gbc_button_addSimfile.gridx = 1;
		gbc_button_addSimfile.gridy = 6;
		this.add(button_addSimfile, gbc_button_addSimfile);

		button_removeSimfile = new JButton("");
		button_removeSimfile.setFont(ScyllaGUI.DEFAULTFONT);
		button_removeSimfile.setToolTipText("Remove selected file(s)");
		button_removeSimfile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DefaultListModel<String> m = (DefaultListModel<String>) list_CurrentSimFiles.getModel();
				List<String> remove = list_CurrentSimFiles.getSelectedValuesList();
				for(int i = 0; i < remove.size(); i++)m.removeElement(remove.get(i));
			}
		});
		button_removeSimfile.setIcon(ScyllaGUI.ICON_X);
		GridBagConstraints gbc_button_removeSimfile = new GridBagConstraints();
		gbc_button_removeSimfile.fill = GridBagConstraints.BOTH;
		gbc_button_removeSimfile.insets = new Insets(0, 0, 0, COL1);
		gbc_button_removeSimfile.gridx = 1;
		gbc_button_removeSimfile.gridy = 7;
		this.add(button_removeSimfile, gbc_button_removeSimfile);
		
		
		label_Plugins = new JLabel();
		label_Plugins.setOpaque(true);
		label_Plugins.setFont(ScyllaGUI.TITLEFONT);
		label_Plugins.setBackground(ScyllaGUI.ColorField0);
		label_Plugins.setForeground(ScyllaGUI.TITLEFONT_COLOR);
		label_Plugins.setText("Plugins");
		GridBagConstraints gbc_textfield_Plugins = new GridBagConstraints();
		gbc_textfield_Plugins.fill = GridBagConstraints.BOTH;
		gbc_textfield_Plugins.insets = new Insets(ROW1, 0, 0, COL1);
		gbc_textfield_Plugins.gridx = 2;
		gbc_textfield_Plugins.gridy = 0;
		this.add(label_Plugins, gbc_textfield_Plugins);
		
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
	
		
		label_Console = new JLabel();
		label_Console.setOpaque(true);
		label_Console.setText("Console Output");
		label_Console.setFont(ScyllaGUI.TITLEFONT);
		label_Console.setBackground(ScyllaGUI.ColorField0);
		label_Console.setForeground(ScyllaGUI.TITLEFONT_COLOR);
		GridBagConstraints gbc_label_Console = new GridBagConstraints();
		gbc_label_Console.fill = GridBagConstraints.BOTH;
		gbc_label_Console.insets = new Insets(ROW1, COL1, 0, COL1);
		gbc_label_Console.gridwidth = 3;
		gbc_label_Console.gridx = 0;
		gbc_label_Console.gridy = 8;
		this.add(label_Console, gbc_label_Console);
		
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

				DefaultListModel<String> m = (DefaultListModel<String>) list_CurrentBpmnFiles.getModel();
				String[] bpmnFilenames = new String[m.size()];
				for(int i = 0; i < bpmnFilenames.length; i++){
					bpmnFilenames[i] = (String) m.getElementAt(i);
				}
				m = (DefaultListModel<String>) list_CurrentSimFiles.getModel();
				String[] simFilenames = new String[m.size()];
				for(int i = 0; i < simFilenames.length; i++){
					simFilenames[i] = (String) m.getElementAt(i);
				}
				
				startSimulation(
						textfield_CurrentGlobalConfig_chosen.getText(),
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
	private void startSimulation(String resFilename, String[] bpmnFilenames, String[] simFilenames) {

		button_StartSimulation.setText("Running ...");
        button_StartSimulation.setEnabled(false);
        
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
	}
	
	public Console getConsole() {
		return console;
	}

}
