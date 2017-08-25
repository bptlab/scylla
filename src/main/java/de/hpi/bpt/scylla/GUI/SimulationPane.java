package de.hpi.bpt.scylla.GUI;

import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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
/**
 * 
 * @author Leon Bein
 *
 */
@SuppressWarnings("serial")
public class SimulationPane extends JPanel{
	
	private ScyllaGUI gui;
		
	private static int ROW1 = ScyllaGUI.STD3;
	private static int ROW2 = ROW1+ScyllaGUI.STD;
//	private static int ROW3 = ROW2+STD;
	private static int ROW4 = ROW2+2*ScyllaGUI.STD;
	private static int ROW5 = ROW4+ScyllaGUI.STD;
//	private static int ROW6 = ROW5+STDHEI;
	private static int ROW7 = ROW5+ScyllaGUI.STDHEI+ScyllaGUI.STD;
	private static int ROW8 = ROW7+ScyllaGUI.STD;
	private static int ROW9 = ROW8+ScyllaGUI.STDHEI;
	
	private static int WIDTH1 = ScyllaGUI.WIDTH/2-2*ScyllaGUI.STD2;
	
	private static int COL1 = ScyllaGUI.STD2;
	private static int COL2 = COL1 + WIDTH1;
	private static int COL3 = COL2 + ScyllaGUI.STD2;
	
	
	private JTextField textfield_CurrentGlobalConfig_info;
	private JTextField textfield_CurrentGlobalConfig_chosen;
	private JButton button_openglobalconfig;
	
	private JTextField textfield_CurrentBpmnFiles;
	private JScrollPane scrollPane_BpmnFiles;
	private JList<String> list_CurrentBpmnFiles;
	private JButton button_addBpmnFile;
	private JButton button_removeBpmnFile;
	
	private JTextField textfield_CurrentSimulationFiles;
	private JScrollPane scrollPane_SimFiles;
	private JList<String> list_CurrentSimFiles;
	private JButton button_addSimfile;
	private JButton button_removeSimfile;
	
	private JTextField textfield_Plugins;
	private JScrollPane scrollPane_plugins;

	private JButton button_StartSimulation;
	private Container panel_plugins;
	
	private JTextField textField_Console;
	private Console console;


	private JScrollPane scrollPane_Console;
	
	private JButton button_OpenLastOutput;
	private String lastOutPutFolder;
	private JButton button_AdvancedOptions;
	private JPanel panel_AdvancedOptions;
	private JCheckBox checkbox_debug;
	private JCheckBox checkbox_desmoj;
	private JButton button_allplugins;
	
	
	public SimulationPane(ScyllaGUI g){
		gui = g;
		
		setBackground(ScyllaGUI.ColorBackground);
		setLayout(null);
		
		textfield_CurrentGlobalConfig_info = new JTextField();
		textfield_CurrentGlobalConfig_info.setHighlighter(null);
		textfield_CurrentGlobalConfig_info.setFont(ScyllaGUI.TITLEFONT);
		textfield_CurrentGlobalConfig_info.setBackground(ScyllaGUI.ColorField0);
		textfield_CurrentGlobalConfig_info.setForeground(ScyllaGUI.TITLEFONT_COLOR);
		textfield_CurrentGlobalConfig_info.setBounds(COL1, ROW1, WIDTH1, ScyllaGUI.STD);
		textfield_CurrentGlobalConfig_info.setEditable(false);
		textfield_CurrentGlobalConfig_info.setText("Current Global Config ");
		this.add(textfield_CurrentGlobalConfig_info);
		textfield_CurrentGlobalConfig_info.setColumns(10);
		
		button_openglobalconfig = new JButton();
		button_openglobalconfig.setIcon(ScyllaGUI.ICON_MORE);
		button_openglobalconfig.setFont(ScyllaGUI.DEFAULTFONT);
		button_openglobalconfig.setToolTipText("Choose other file");
		button_openglobalconfig.setBounds(COL2 - ScyllaGUI.STD1, ROW2, ScyllaGUI.STD1, ScyllaGUI.STD);
		button_openglobalconfig.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ScalingFileChooser chooser = new ScalingFileChooser(ScyllaGUI.DEFAULTFILEPATH);
				chooser.setDialogTitle("Choose global config");
				chooser.setFont(ScyllaGUI.fileChooserFont);
				chooser.setPreferredSize(ScyllaGUI.fileChooserDimension);
				int c = chooser.showDialog(null,"Open");
				if(c == ScalingFileChooser.APPROVE_OPTION){
					textfield_CurrentGlobalConfig_chosen.setText(chooser.getSelectedFile().getPath());
					ScyllaGUI.DEFAULTFILEPATH = chooser.getSelectedFile().getPath();
				}
			}
		});
		this.add(button_openglobalconfig);
		
		textfield_CurrentGlobalConfig_chosen = new JTextField();
		textfield_CurrentGlobalConfig_chosen.setMargin(ScyllaGUI.LEFTMARGIN);
		textfield_CurrentGlobalConfig_chosen.setFont(ScyllaGUI.DEFAULTFONT);
		textfield_CurrentGlobalConfig_chosen.setToolTipText("Path for current global configuarition file");
		textfield_CurrentGlobalConfig_chosen.setBounds(COL1, ROW2, WIDTH1-ScyllaGUI.STD1, ScyllaGUI.STD);
		textfield_CurrentGlobalConfig_chosen.setEditable(false);
		this.add(textfield_CurrentGlobalConfig_chosen);
		textfield_CurrentGlobalConfig_chosen.setColumns(10);
		
		scrollPane_BpmnFiles = new JScrollPane();
		scrollPane_BpmnFiles.setFont(ScyllaGUI.DEFAULTFONT);
		scrollPane_BpmnFiles.setBounds(COL1, ROW5, WIDTH1-ScyllaGUI.STD1, ScyllaGUI.STDHEI);
		scrollPane_BpmnFiles.setToolTipText("");
		this.add(scrollPane_BpmnFiles);
		
		list_CurrentBpmnFiles = new JList<String>();
		list_CurrentBpmnFiles.setBorder(new EmptyBorder(ScyllaGUI.LEFTMARGIN));
		list_CurrentBpmnFiles.setFont(ScyllaGUI.DEFAULTFONT);
		list_CurrentBpmnFiles.setModel(new DefaultListModel<>());
		list_CurrentBpmnFiles.setDragEnabled(true);
		scrollPane_BpmnFiles.setViewportView(list_CurrentBpmnFiles);
		
		scrollPane_SimFiles = new JScrollPane();
		scrollPane_SimFiles.setFont(ScyllaGUI.DEFAULTFONT);
		scrollPane_SimFiles.setBounds(COL1, ROW8, WIDTH1-ScyllaGUI.STD1, ScyllaGUI.STDHEI);
		scrollPane_SimFiles.setToolTipText("");
		this.add(scrollPane_SimFiles);
		
		list_CurrentSimFiles = new JList<String>();
		list_CurrentSimFiles.setBorder(new EmptyBorder(ScyllaGUI.LEFTMARGIN));
		list_CurrentSimFiles.setFont(ScyllaGUI.DEFAULTFONT);
		list_CurrentSimFiles.setModel(new DefaultListModel<>());
		list_CurrentSimFiles.setDragEnabled(true);
		scrollPane_SimFiles.setViewportView(list_CurrentSimFiles);
		
		textfield_CurrentBpmnFiles = new JTextField();
		textfield_CurrentBpmnFiles.setEditable(false);
		textfield_CurrentBpmnFiles.setHighlighter(null);
		textfield_CurrentBpmnFiles.setFont(ScyllaGUI.TITLEFONT);

		textfield_CurrentBpmnFiles.setBackground(ScyllaGUI.ColorField0);
		textfield_CurrentBpmnFiles.setForeground(ScyllaGUI.TITLEFONT_COLOR);
		textfield_CurrentBpmnFiles.setBounds(COL1, ROW4, WIDTH1, ScyllaGUI.STD);
		textfield_CurrentBpmnFiles.setText("Current BPMN Files");
		this.add(textfield_CurrentBpmnFiles);
		textfield_CurrentBpmnFiles.setColumns(10);
		
		button_addBpmnFile = new JButton("");
		button_addBpmnFile.setFont(ScyllaGUI.DEFAULTFONT);
		button_addBpmnFile.setToolTipText("Add BPMN file");
		button_addBpmnFile.setBounds(COL2 - ScyllaGUI.STD1, ROW5, ScyllaGUI.STD1, ScyllaGUI.STDHEIH);
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
		this.add(button_addBpmnFile);
		
		button_removeBpmnFile = new JButton("");
		button_removeBpmnFile.setFont(ScyllaGUI.DEFAULTFONT);
		button_removeBpmnFile.setToolTipText("Remove selected file(s)");
		button_removeBpmnFile.setBounds(COL2 - ScyllaGUI.STD1, ROW5+ScyllaGUI.STDHEIH, ScyllaGUI.STD1, ScyllaGUI.STDHEIH);
		button_removeBpmnFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DefaultListModel<String> m = (DefaultListModel<String>) list_CurrentBpmnFiles.getModel();
				List<String> remove = list_CurrentBpmnFiles.getSelectedValuesList();
				for(int i = 0; i < remove.size(); i++)m.removeElement(remove.get(i));
			}
		});
		button_removeBpmnFile.setIcon(ScyllaGUI.ICON_X);
		this.add(button_removeBpmnFile);
		
		textfield_CurrentSimulationFiles = new JTextField();
		textfield_CurrentSimulationFiles.setEditable(false);
		textfield_CurrentSimulationFiles.setHighlighter(null);
		textfield_CurrentSimulationFiles.setFont(ScyllaGUI.TITLEFONT);
		textfield_CurrentSimulationFiles.setBackground(ScyllaGUI.ColorField0);
		textfield_CurrentSimulationFiles.setForeground(ScyllaGUI.TITLEFONT_COLOR);
		textfield_CurrentSimulationFiles.setBounds(COL1, ROW7, WIDTH1, ScyllaGUI.STD);
		textfield_CurrentSimulationFiles.setText("Current Simulation Files");
		textfield_CurrentSimulationFiles.setColumns(10);
		this.add(textfield_CurrentSimulationFiles);

		button_addSimfile = new JButton("");
		button_addSimfile.setFont(ScyllaGUI.DEFAULTFONT);
		button_addSimfile.setToolTipText("Add simulation file");
		button_addSimfile.setBounds(COL2 - ScyllaGUI.STD1, ROW8, ScyllaGUI.STD1, ScyllaGUI.STDHEIH);
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
		this.add(button_addSimfile);
		
		button_removeSimfile = new JButton("");
		button_removeSimfile.setFont(ScyllaGUI.DEFAULTFONT);
		button_removeSimfile.setToolTipText("Remove selected file(s)");
		button_removeSimfile.setBounds(COL2 - ScyllaGUI.STD1, ROW8 + ScyllaGUI.STDHEIH, ScyllaGUI.STD1, ScyllaGUI.STDHEIH);
		button_removeSimfile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DefaultListModel<String> m = (DefaultListModel<String>) list_CurrentSimFiles.getModel();
				List<String> remove = list_CurrentSimFiles.getSelectedValuesList();
				for(int i = 0; i < remove.size(); i++)m.removeElement(remove.get(i));
			}
		});
		button_removeSimfile.setIcon(ScyllaGUI.ICON_X);
		this.add(button_removeSimfile);
		
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
		button_StartSimulation.setBounds(ScyllaGUI.WIDTH/2-ScyllaGUI.WIDTH/10,ScyllaGUI.HEIGHT-ScyllaGUI.HEIGHT/8-ScyllaGUI.STD3, ScyllaGUI.WIDTH/5-ScyllaGUI.STD1, ScyllaGUI.STDHEIH);
		this.add(button_StartSimulation);
		
		
		lastOutPutFolder = "";
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
		button_OpenLastOutput.setBounds(ScyllaGUI.WIDTH/2-ScyllaGUI.WIDTH/10,ScyllaGUI.HEIGHT-ScyllaGUI.HEIGHT/8-ScyllaGUI.STD3+ScyllaGUI.STDHEIH, ScyllaGUI.WIDTH/5-ScyllaGUI.STD1, ScyllaGUI.STD/2);
    	button_OpenLastOutput.setEnabled(false);
		this.add(button_OpenLastOutput);
		
		
		button_AdvancedOptions = new JButton("");
		button_AdvancedOptions.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				panel_AdvancedOptions.setVisible(!panel_AdvancedOptions.isVisible());
			}
		});
		button_AdvancedOptions.setToolTipText("Show Advanced Simulation Options");
		button_AdvancedOptions.setIcon(ScyllaGUI.ICON_OPTIONS);
		button_AdvancedOptions.setBounds(ScyllaGUI.WIDTH/2+ScyllaGUI.WIDTH/10-ScyllaGUI.STD1,ScyllaGUI.HEIGHT-ScyllaGUI.HEIGHT/8-ScyllaGUI.STD3, ScyllaGUI.STD1, ScyllaGUI.STDHEIH+ScyllaGUI.STD/2);
		this.add(button_AdvancedOptions);
		
		panel_AdvancedOptions = new JPanel();
		panel_AdvancedOptions.setBounds(ScyllaGUI.WIDTH/2+ScyllaGUI.WIDTH/10, ScyllaGUI.HEIGHT-ScyllaGUI.HEIGHT/8-ScyllaGUI.STD3, ScyllaGUI.WIDTH/10, ScyllaGUI.STDHEIH+ScyllaGUI.STD/2);
		
		
		
		this.add(panel_AdvancedOptions);
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
		panel_AdvancedOptions.add(checkbox_desmoj);
		
		
		
		scrollPane_plugins = new JScrollPane();
		scrollPane_plugins.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane_plugins.getVerticalScrollBar().setUnitIncrement(16);
		scrollPane_plugins.setToolTipText("Plugin List");
		scrollPane_plugins.setFont(ScyllaGUI.DEFAULTFONT);
		scrollPane_plugins.setBounds(COL3, ROW2, WIDTH1, ROW9-ROW2);
		this.add(scrollPane_plugins);
		


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
		

		
		textfield_Plugins = new JTextField();
		textfield_Plugins.setHighlighter(null);
		textfield_Plugins.setFont(ScyllaGUI.TITLEFONT);
		textfield_Plugins.setBackground(ScyllaGUI.ColorField0);
		textfield_Plugins.setForeground(ScyllaGUI.TITLEFONT_COLOR);
		textfield_Plugins.setText("Plugins");
		textfield_Plugins.setEditable(false);
		textfield_Plugins.setColumns(10);
		textfield_Plugins.setBounds(COL3, ROW1, WIDTH1, ScyllaGUI.STD);
		this.add(textfield_Plugins);
		
		textField_Console = new JTextField();
		textField_Console.setText("Console Output");
		textField_Console.setFont(ScyllaGUI.TITLEFONT);
		textField_Console.setBackground(ScyllaGUI.ColorField0);
		textField_Console.setForeground(ScyllaGUI.TITLEFONT_COLOR);
		textField_Console.setEditable(false);
		textField_Console.setColumns(10);
		textField_Console.setBounds(COL1, ROW9+ScyllaGUI.STD, COL3+WIDTH1-COL1, ScyllaGUI.STD);
		this.add(textField_Console);
		
		scrollPane_Console = new JScrollPane();
		scrollPane_Console.setBounds(COL1, ROW9+2*ScyllaGUI.STD, COL3+WIDTH1-COL1, ScyllaGUI.HEIGHT-ScyllaGUI.HEIGHT/8-ScyllaGUI.STD3 - (ROW9+3*ScyllaGUI.STD));
		this.add(scrollPane_Console);
		
		console = new Console();
		console.setHighlighter(null);
		console.setFont(ScyllaGUI.CONSOLEFONT);
		console.setBackground(ScyllaGUI.ColorField1);
		console.setEditable(false);
		scrollPane_Console.setViewportView(console);
		console.setMargin(ScyllaGUI.LEFTMARGIN);
		

		loadPlugins();
	}
	
	private void loadPlugins() {
		//TODO System.setErr(console.getErr());
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
		for(Entry<String, ArrayList<PluginLoader.PluginWrapper>> e : plugins.entrySet()){
			CheckboxListPanel listpanel = new CheckboxListPanel(e.getKey(),e.getValue());
			gbc.gridy = i;
			if(i == plugins.entrySet().size()-1)gbc.weighty = 1.0;
			panel_plugins.add(listpanel,gbc);
			i++;
			//gbc.anchor = GridBagConstraints.BASELINE;
		}
	}
	
	private void startSimulation(String resFilename, String[] bpmnFilename, String[] simFilenames) {

        button_StartSimulation.setEnabled(false);
        
    	boolean enableDebugLogging = checkbox_debug.isSelected();
        boolean redirectErrors = checkbox_debug.isSelected();
        boolean enableDesmojLogging = checkbox_desmoj.isSelected();
        
        DebugLogger.allowDebugLogging = enableDebugLogging;
        /* TODO if(redirectErrors) System.setErr(console.getErr());
        else System.setErr(gui.getStdErr());*/
        
        boolean enableBpsLogging = true;
        
        boolean success = true;

        SimulationManager manager = new SimulationManager(ScyllaGUI.DESMOJOUTPUTPATH, bpmnFilename, simFilenames, resFilename,
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
	}
	
	public Console getConsole() {
		return console;
	}

}
