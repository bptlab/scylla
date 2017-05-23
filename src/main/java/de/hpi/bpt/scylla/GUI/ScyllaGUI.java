package de.hpi.bpt.scylla.GUI;

import java.awt.Color;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import co.paralleluniverse.common.monitoring.LatencyStatsReservoir;
import de.hpi.bpt.scylla.SimulationManager;
import de.hpi.bpt.scylla.plugin_loader.PluginLoader;
import java.awt.SystemColor;
import javax.swing.SwingConstants;
/**
 * @author Leon Bein
 *
 */
@SuppressWarnings("serial")
public class ScyllaGUI extends JFrame {
	
	
	private static final String DEFAULTFILEPATH = "samples\\";
	private static final String OUTPUTFILEPATH = "desmoj_output\\";
	

	public static final Color ColorField0 = new Color(200,200,200);
	public static final Color ColorField1 = new Color(240,240,240);
	public static final Color ColorField2 = new Color(255,255,255);
	public static final Color ColorBackground = new Color(100,100,100);

	
	private static GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
	private static java.awt.Rectangle r = env.getMaximumWindowBounds();
	private static int WIDTH = r.width;//1200
	private static int HEIGHT = r.height;//900
	private static double SCALE = HEIGHT/900;

//	private static double SCALE = 1;
//	private static int WIDTH = 1200;//(int)(1200.0 * SCALE);
//	private static int HEIGHT = 900;//(int)(900 * SCALE);
	
	public static final Dimension fileChooserDimension = new Dimension((int)(800.0*SCALE),(int)(500.0*SCALE));
	public static final Font fileChooserFont = new Font("Arial", Font.PLAIN, (int)(14.0*SCALE));

	public static final Font DEFAULTFONT = new Font("Arial", Font.PLAIN, (int)(16.0*SCALE));
	public static final Font TITLEFONT = new Font(DEFAULTFONT.getFontName(), Font.PLAIN, (int)(20.0*SCALE));

	private static int STD = HEIGHT/24;
	private static int STD1 = WIDTH/32;
	private static int STD2 = WIDTH/48;
	private static int STD3 = HEIGHT/36;
	private static int STDHEI = 3*STD;
	private static int STDHEIH = STDHEI/2;
	
	private static int ROW1 = STD3;
	private static int ROW2 = ROW1+STD;
//	private static int ROW3 = ROW2+STD;
	private static int ROW4 = ROW2+2*STD;
	private static int ROW5 = ROW4+STD;
//	private static int ROW6 = ROW5+STDHEI;
	private static int ROW7 = ROW5+STDHEI+STD;
	private static int ROW8 = ROW7+STD;
	private static int ROW9 = ROW8+STDHEI;
	
	private static int WIDTH1 = WIDTH/2-2*STD2;
	
	private static int COL1 = STD2;
	private static int COL2 = COL1 + WIDTH1;
	private static int COL3 = COL2 + STD2;
	
	public static final Insets LEFTMARGIN = new Insets(0, STD2, 0, 0);
	
	

	private JPanel contentPane;
	
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
	private JTextArea console;


	private JScrollPane scrollPane_Console;
	private JButton button_OpenLastOutput;
	private String lastOutPutFolder;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ScyllaGUI frame = new ScyllaGUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public ScyllaGUI() {
		setTitle("Skylla GUI");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100,WIDTH,HEIGHT);
		
	    setExtendedState(JFrame.MAXIMIZED_BOTH);
		
		contentPane = new JPanel();
		contentPane.setBackground(ColorBackground);
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		
//		JSeparator separator = new JSeparator();
//		separator.setBounds(600, 0, 1, 812);
//		contentPane.add(separator);
		
		textfield_CurrentGlobalConfig_info = new JTextField();
		textfield_CurrentGlobalConfig_info.setHighlighter(null);
		textfield_CurrentGlobalConfig_info.setFont(TITLEFONT);
		textfield_CurrentGlobalConfig_info.setBackground(ColorField0);
		textfield_CurrentGlobalConfig_info.setBounds(COL1, ROW1, WIDTH1, STD);
		textfield_CurrentGlobalConfig_info.setEditable(false);
		textfield_CurrentGlobalConfig_info.setText("Current Global Config ");
		contentPane.add(textfield_CurrentGlobalConfig_info);
		textfield_CurrentGlobalConfig_info.setColumns(10);
		
		button_openglobalconfig = new JButton("...");
		button_openglobalconfig.setBackground(ColorField1);
		button_openglobalconfig.setFont(DEFAULTFONT);
		button_openglobalconfig.setToolTipText("Choose other file");
		button_openglobalconfig.setBounds(COL2 - STD1, ROW2, STD1, STD);
		button_openglobalconfig.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ScalingFileChooser chooser = new ScalingFileChooser(DEFAULTFILEPATH);
				chooser.setFont(fileChooserFont);
				chooser.setForeground(ColorField2);
				chooser.setPreferredSize(fileChooserDimension);
				int c = chooser.showDialog(null,"Open");
				if(c == ScalingFileChooser.APPROVE_OPTION){
					textfield_CurrentGlobalConfig_chosen.setText(chooser.getSelectedFile().getPath());
				}
			}
		});
		contentPane.add(button_openglobalconfig);
		
		textfield_CurrentGlobalConfig_chosen = new JTextField();
		textfield_CurrentGlobalConfig_chosen.setMargin(LEFTMARGIN);
		textfield_CurrentGlobalConfig_chosen.setFont(DEFAULTFONT);
		textfield_CurrentGlobalConfig_chosen.setBackground(ColorField2);
		textfield_CurrentGlobalConfig_chosen.setToolTipText("Path for current global configuarition file");
		textfield_CurrentGlobalConfig_chosen.setBounds(COL1, ROW2, WIDTH1-STD1, STD);
		textfield_CurrentGlobalConfig_chosen.setEditable(false);
		contentPane.add(textfield_CurrentGlobalConfig_chosen);
		textfield_CurrentGlobalConfig_chosen.setColumns(10);
		
		scrollPane_BpmnFiles = new JScrollPane();
		scrollPane_BpmnFiles.setFont(DEFAULTFONT);
		scrollPane_BpmnFiles.setBounds(COL1, ROW5, WIDTH1-STD1, STDHEI);
		scrollPane_BpmnFiles.setToolTipText("");
		contentPane.add(scrollPane_BpmnFiles);
		
		list_CurrentBpmnFiles = new JList<String>();
		list_CurrentBpmnFiles.setBorder(new EmptyBorder(LEFTMARGIN));
		list_CurrentBpmnFiles.setBackground(ColorField2);
		list_CurrentBpmnFiles.setFont(DEFAULTFONT);
		list_CurrentBpmnFiles.setModel(new DefaultListModel<>());
		list_CurrentBpmnFiles.setDragEnabled(true);
		scrollPane_BpmnFiles.setViewportView(list_CurrentBpmnFiles);
		
		scrollPane_SimFiles = new JScrollPane();
		scrollPane_SimFiles.setFont(DEFAULTFONT);
		scrollPane_SimFiles.setBounds(COL1, ROW8, WIDTH1-STD1, STDHEI);
		scrollPane_SimFiles.setToolTipText("");
		contentPane.add(scrollPane_SimFiles);
		
		list_CurrentSimFiles = new JList<String>();
		list_CurrentSimFiles.setBorder(new EmptyBorder(LEFTMARGIN));
		list_CurrentSimFiles.setBackground(ColorField2);
		list_CurrentSimFiles.setFont(DEFAULTFONT);
		list_CurrentSimFiles.setModel(new DefaultListModel<>());
		list_CurrentSimFiles.setDragEnabled(true);
		scrollPane_SimFiles.setViewportView(list_CurrentSimFiles);
		
		textfield_CurrentBpmnFiles = new JTextField();
		textfield_CurrentBpmnFiles.setEditable(false);
		textfield_CurrentBpmnFiles.setHighlighter(null);
		textfield_CurrentBpmnFiles.setFont(TITLEFONT);
		textfield_CurrentBpmnFiles.setBackground(ColorField0);
		textfield_CurrentBpmnFiles.setBounds(COL1, ROW4, WIDTH1, STD);
		textfield_CurrentBpmnFiles.setText("Current BPMN Files");
		contentPane.add(textfield_CurrentBpmnFiles);
		textfield_CurrentBpmnFiles.setColumns(10);
		
		button_addBpmnFile = new JButton("");
		button_addBpmnFile.setFont(DEFAULTFONT);
		button_addBpmnFile.setBackground(ColorField1);
		button_addBpmnFile.setToolTipText("Add BPMN file");
		button_addBpmnFile.setBounds(COL2 - STD1, ROW5, STD1, STDHEIH);
		button_addBpmnFile.setIcon(new ImageIcon(ScyllaGUI.class.getResource("/GUI/plus.png")));
		button_addBpmnFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ScalingFileChooser chooser = new ScalingFileChooser(DEFAULTFILEPATH);
				int c = chooser.showDialog(null,"Open");
				if(c == ScalingFileChooser.APPROVE_OPTION){
					DefaultListModel<String> m = (DefaultListModel<String>) list_CurrentBpmnFiles.getModel();
					chooser.getSelectedFile();
					m.addElement(chooser.getSelectedFile().getPath());
				}
				
			}
		});
		contentPane.add(button_addBpmnFile);
		
		button_removeBpmnFile = new JButton("");
		button_removeBpmnFile.setFont(DEFAULTFONT);
		button_removeBpmnFile.setBackground(ColorField1);
		button_removeBpmnFile.setToolTipText("Remove selected file(s)");
		button_removeBpmnFile.setBounds(COL2 - STD1, ROW5+STDHEIH, STD1, STDHEIH);
		button_removeBpmnFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DefaultListModel<String> m = (DefaultListModel<String>) list_CurrentBpmnFiles.getModel();
				List<String> remove = list_CurrentBpmnFiles.getSelectedValuesList();
				for(int i = 0; i < remove.size(); i++)m.removeElement(remove.get(i));
			}
		});
		button_removeBpmnFile.setIcon(new ImageIcon(ScyllaGUI.class.getResource("/GUI/remove.png")));
		contentPane.add(button_removeBpmnFile);
		
		textfield_CurrentSimulationFiles = new JTextField();
		textfield_CurrentSimulationFiles.setEditable(false);
		textfield_CurrentSimulationFiles.setHighlighter(null);
		textfield_CurrentSimulationFiles.setFont(TITLEFONT);
		textfield_CurrentSimulationFiles.setBackground(ColorField0);
		textfield_CurrentSimulationFiles.setBounds(COL1, ROW7, WIDTH1, STD);
		textfield_CurrentSimulationFiles.setText("Current Simulation Files");
		textfield_CurrentSimulationFiles.setColumns(10);
		contentPane.add(textfield_CurrentSimulationFiles);

		button_addSimfile = new JButton("");
		button_addSimfile.setFont(DEFAULTFONT);
		button_addSimfile.setBackground(ColorField1);
		button_addSimfile.setToolTipText("Add simulation file");
		button_addSimfile.setBounds(COL2 - STD1, ROW8, STD1, STDHEIH);
		button_addSimfile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ScalingFileChooser chooser = new ScalingFileChooser(DEFAULTFILEPATH);
				int c = chooser.showDialog(null,"Open");
				if(c == ScalingFileChooser.APPROVE_OPTION){
					DefaultListModel<String> m = (DefaultListModel<String>) list_CurrentSimFiles.getModel();
					m.addElement(chooser.getSelectedFile().getPath());
				}
				
			}
		});
		button_addSimfile.setIcon(new ImageIcon(ScyllaGUI.class.getResource("/GUI/plus.png")));
		contentPane.add(button_addSimfile);
		
		button_removeSimfile = new JButton("");
		button_removeSimfile.setFont(DEFAULTFONT);
		button_removeSimfile.setBackground(ColorField1);
		button_removeSimfile.setToolTipText("Remove selected file(s)");
		button_removeSimfile.setBounds(COL2 - STD1, ROW8 + STDHEIH, STD1, STDHEIH);
		button_removeSimfile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DefaultListModel<String> m = (DefaultListModel<String>) list_CurrentSimFiles.getModel();
				List<String> remove = list_CurrentSimFiles.getSelectedValuesList();
				for(int i = 0; i < remove.size(); i++)m.removeElement(remove.get(i));
			}
		});
		button_removeSimfile.setIcon(new ImageIcon(ScyllaGUI.class.getResource("/GUI/remove.png")));
		contentPane.add(button_removeSimfile);
		
		button_StartSimulation = new JButton("Start Simulation");
		button_StartSimulation.setFont(TITLEFONT);
		button_StartSimulation.setBackground(ColorField1);
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
		button_StartSimulation.setBounds(WIDTH/2-WIDTH/10,HEIGHT-HEIGHT/8-STD3, WIDTH/5, STDHEIH);
		contentPane.add(button_StartSimulation);
		
		
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
		button_OpenLastOutput.setFont(DEFAULTFONT);
		button_OpenLastOutput.setBackground(ColorField1);
		button_OpenLastOutput.setBounds(WIDTH/2-WIDTH/10,HEIGHT-HEIGHT/8-STD3+STDHEIH, WIDTH/5, STD/2);
    	button_OpenLastOutput.setEnabled(false);
		contentPane.add(button_OpenLastOutput);
		
		scrollPane_plugins = new JScrollPane();
		scrollPane_plugins.getVerticalScrollBar().setUnitIncrement(16);
		scrollPane_plugins.setToolTipText("Plugin List");
		scrollPane_plugins.setFont(DEFAULTFONT);
		scrollPane_plugins.setBounds(COL3, ROW2, WIDTH1, ROW9-ROW2);
		contentPane.add(scrollPane_plugins);
		


		panel_plugins = new Container();
		panel_plugins.setFont(DEFAULTFONT);
		panel_plugins.setLayout(new GridBagLayout());
		
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
			ListPanel listpanel = new ListPanel(e.getKey(),e.getValue());
			gbc.gridy = i;
			if(i == plugins.entrySet().size()-1)gbc.weighty = 1.0;
			panel_plugins.add(listpanel,gbc);
			i++;
			//gbc.anchor = GridBagConstraints.BASELINE;
		}
		scrollPane_plugins.setViewportView(panel_plugins);
		scrollPane_plugins.getViewport().setBackground(ColorField2);
		

		
		textfield_Plugins = new JTextField();
		textfield_Plugins.setHighlighter(null);
		textfield_Plugins.setFont(TITLEFONT);
		textfield_Plugins.setBackground(ColorField0);
		textfield_Plugins.setText("Plugins");
		textfield_Plugins.setEditable(false);
		textfield_Plugins.setColumns(10);
		textfield_Plugins.setBounds(COL3, ROW1, WIDTH1, STD);
		contentPane.add(textfield_Plugins);
		
		textField_Console = new JTextField();
		textField_Console.setText("Console Output");
		textField_Console.setFont(TITLEFONT);
		textField_Console.setBackground(ColorField0);
		textField_Console.setEditable(false);
		textField_Console.setColumns(10);
		textField_Console.setBounds(COL1, ROW9+STD, COL3+WIDTH1-COL1, STD);
		contentPane.add(textField_Console);
		
		scrollPane_Console = new JScrollPane();
		scrollPane_Console.setBounds(COL1, ROW9+2*STD, COL3+WIDTH1-COL1, HEIGHT-HEIGHT/8-STD3 - (ROW9+3*STD));
		contentPane.add(scrollPane_Console);
		
		console = new JTextArea();
		console.setHighlighter(null);
		console.setFont(DEFAULTFONT);
		console.setBackground(ColorField1);
		console.setEditable(false);
		scrollPane_Console.setViewportView(console);
		console.setColumns(10);
		console.setMargin(LEFTMARGIN);
		

		
		System.setOut(
				new PrintStream(new OutputStream(){
					@Override
					public void write(int c) throws IOException {
						console.append(String.valueOf((char)c));
						console.setCaretPosition(console.getText().length());
					}
					
				})
		);
		

		
	}

	private void startSimulation(String resFilename, String[] bpmnFilename, String[] simFilenames) {
 
    	
        boolean enableBpsLogging = true;
        boolean enableDesmojLogging = true;
        
        boolean success = true;

        SimulationManager manager = new SimulationManager(OUTPUTFILEPATH, bpmnFilename, simFilenames, resFilename,
                enableBpsLogging, enableDesmojLogging);
        button_StartSimulation.setEnabled(false);
        try{
            manager.run();
        }catch(Exception e){
        	e.printStackTrace();
        	System.out.println("Fatal error, simulation has been canceled");
        	success = false;
        }
        if(success){
        	lastOutPutFolder = manager.getOutputPath();
        	button_OpenLastOutput.setEnabled(true);
        }
        button_StartSimulation.setEnabled(true);
	}
}
