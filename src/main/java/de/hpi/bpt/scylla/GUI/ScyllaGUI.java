package de.hpi.bpt.scylla.GUI;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ColorUIResource;

import de.hpi.bpt.scylla.SimulationManager;
import de.hpi.bpt.scylla.logger.DebugLogger;
import de.hpi.bpt.scylla.plugin_loader.PluginLoader;
/**
 * @author Leon Bein
 *
 */
@SuppressWarnings("serial")
public class ScyllaGUI extends JFrame {
	
	
	
	private static final String DEFAULTFILEPATH = "samples\\";
	private static final String DESMOJOUTPUTPATH = "desmoj_output\\";
	

	public static final Color ColorField0 = new Color(45,112,145);
	public static final Color ColorField1 = new Color(240,240,240);
	public static final Color ColorField2 = new Color(255,255,255);
	public static final Color ColorBackground = new Color(0,142,185);

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
	public static final Color DEFAULTFONT_COLOR = new Color(0,0,0);
	public static final Font TITLEFONT = new Font(DEFAULTFONT.getFontName(), Font.PLAIN, (int)(20.0*SCALE));
	public static final Color TITLEFONT_COLOR = new Color(255,255,255);
	public static final Font CONSOLEFONT = new Font("Consolas",Font.PLAIN,(int)(14.0*SCALE));

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
	
	public static final ImageIcon ICON_PLUS = resizeIcon(new ImageIcon(ScyllaGUI.class.getResource("/GUI/plus.png")),STD1/2,STDHEIH/2);
	public static final ImageIcon ICON_X = resizeIcon(new ImageIcon(ScyllaGUI.class.getResource("/GUI/remove.png")),STD1/2,STDHEIH/2);
	public static final ImageIcon ICON_MORE = resizeIcon(new ImageIcon(ScyllaGUI.class.getResource("/GUI/more.png")),STD1/2,STD1/2);
	
	public static final ImageIcon ICON_EXPAND = resizeIcon(new ImageIcon(ScyllaGUI.class.getResource("/GUI/expand.png")),DEFAULTFONT.getSize(),DEFAULTFONT.getSize());
	public static final ImageIcon ICON_COLLAPSE = resizeIcon(new ImageIcon(ScyllaGUI.class.getResource("/GUI/collapse.png")),DEFAULTFONT.getSize(),DEFAULTFONT.getSize());
	
	public static final ImageIcon ICON_OPTIONS = resizeIcon(new ImageIcon(ScyllaGUI.class.getResource("/GUI/options.png")),STD1/2,STD1/2);
	
	

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
	private Console console;
	private JScrollPane scrollPane_Console;
	private PrintStream stdErr;
	
	private JButton button_OpenLastOutput;
	private String lastOutPutFolder;
	private JButton button_AdvancedOptions;
	private JPanel panel_AdvancedOptions;
	private JCheckBox checkbox_debug;
	private JCheckBox checkbox_desmoj;
	private JButton button_allplugins;

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
		
		try {
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e2) {
			e2.printStackTrace();
		}

		UIManager.put("ToolTip.background", ScyllaGUI.ColorField1);
		UIManager.put("ToolTip.border", 5);
		UIManager.put("ToolTip.font", ScyllaGUI.DEFAULTFONT);


		UIManager.put("Button.background",ScyllaGUI.ColorField1);
		UIManager.put("Button.select",ScyllaGUI.ColorField1.darker());
		UIManager.put("Button.focus", new ColorUIResource(new Color(0, 0, 0, 0)));

		UIManager.put("Button.rollover",false);


		UIManager.put("TextField.background", ScyllaGUI.ColorField2);
		UIManager.put("TextField.foreground", ScyllaGUI.DEFAULTFONT_COLOR);
		
		UIManager.put("List.selectionBackground", ScyllaGUI.ColorField1);
		
		UIManager.put("ScrollBar.width", (int) ((int)UIManager.get("ScrollBar.width") * SCALE));
		
		
		
		setTitle("Scylla GUI");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100,WIDTH,HEIGHT);
		
	    if(WIDTH == r.getWidth() && HEIGHT == r.getHeight())setExtendedState(JFrame.MAXIMIZED_BOTH);
		
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
		textfield_CurrentGlobalConfig_info.setForeground(TITLEFONT_COLOR);
		textfield_CurrentGlobalConfig_info.setBounds(COL1, ROW1, WIDTH1, STD);
		textfield_CurrentGlobalConfig_info.setEditable(false);
		textfield_CurrentGlobalConfig_info.setText("Current Global Config ");
		contentPane.add(textfield_CurrentGlobalConfig_info);
		textfield_CurrentGlobalConfig_info.setColumns(10);
		
		button_openglobalconfig = new JButton();
		button_openglobalconfig.setIcon(ICON_MORE);
		button_openglobalconfig.setFont(DEFAULTFONT);
		button_openglobalconfig.setToolTipText("Choose other file");
		button_openglobalconfig.setBounds(COL2 - STD1, ROW2, STD1, STD);
		button_openglobalconfig.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ScalingFileChooser chooser = new ScalingFileChooser(DEFAULTFILEPATH);
				chooser.setDialogTitle("Choose global config");
				chooser.setFont(fileChooserFont);
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
		list_CurrentSimFiles.setFont(DEFAULTFONT);
		list_CurrentSimFiles.setModel(new DefaultListModel<>());
		list_CurrentSimFiles.setDragEnabled(true);
		scrollPane_SimFiles.setViewportView(list_CurrentSimFiles);
		
		textfield_CurrentBpmnFiles = new JTextField();
		textfield_CurrentBpmnFiles.setEditable(false);
		textfield_CurrentBpmnFiles.setHighlighter(null);
		textfield_CurrentBpmnFiles.setFont(TITLEFONT);

		textfield_CurrentBpmnFiles.setBackground(ColorField0);
		textfield_CurrentBpmnFiles.setForeground(TITLEFONT_COLOR);
		textfield_CurrentBpmnFiles.setBounds(COL1, ROW4, WIDTH1, STD);
		textfield_CurrentBpmnFiles.setText("Current BPMN Files");
		contentPane.add(textfield_CurrentBpmnFiles);
		textfield_CurrentBpmnFiles.setColumns(10);
		
		button_addBpmnFile = new JButton("");
		button_addBpmnFile.setFont(DEFAULTFONT);
		button_addBpmnFile.setToolTipText("Add BPMN file");
		button_addBpmnFile.setBounds(COL2 - STD1, ROW5, STD1, STDHEIH);
		button_addBpmnFile.setIcon(ICON_PLUS);
		button_addBpmnFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ScalingFileChooser chooser = new ScalingFileChooser(DEFAULTFILEPATH);
				chooser.setDialogTitle("Add business process diagram");
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
		button_removeBpmnFile.setToolTipText("Remove selected file(s)");
		button_removeBpmnFile.setBounds(COL2 - STD1, ROW5+STDHEIH, STD1, STDHEIH);
		button_removeBpmnFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DefaultListModel<String> m = (DefaultListModel<String>) list_CurrentBpmnFiles.getModel();
				List<String> remove = list_CurrentBpmnFiles.getSelectedValuesList();
				for(int i = 0; i < remove.size(); i++)m.removeElement(remove.get(i));
			}
		});
		button_removeBpmnFile.setIcon(ICON_X);
		contentPane.add(button_removeBpmnFile);
		
		textfield_CurrentSimulationFiles = new JTextField();
		textfield_CurrentSimulationFiles.setEditable(false);
		textfield_CurrentSimulationFiles.setHighlighter(null);
		textfield_CurrentSimulationFiles.setFont(TITLEFONT);
		textfield_CurrentSimulationFiles.setBackground(ColorField0);
		textfield_CurrentSimulationFiles.setForeground(TITLEFONT_COLOR);
		textfield_CurrentSimulationFiles.setBounds(COL1, ROW7, WIDTH1, STD);
		textfield_CurrentSimulationFiles.setText("Current Simulation Files");
		textfield_CurrentSimulationFiles.setColumns(10);
		contentPane.add(textfield_CurrentSimulationFiles);

		button_addSimfile = new JButton("");
		button_addSimfile.setFont(DEFAULTFONT);
		button_addSimfile.setToolTipText("Add simulation file");
		button_addSimfile.setBounds(COL2 - STD1, ROW8, STD1, STDHEIH);
		button_addSimfile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ScalingFileChooser chooser = new ScalingFileChooser(DEFAULTFILEPATH);
				chooser.setDialogTitle("Add simulation file");
				int c = chooser.showDialog(null,"Open");
				if(c == ScalingFileChooser.APPROVE_OPTION){
					DefaultListModel<String> m = (DefaultListModel<String>) list_CurrentSimFiles.getModel();
					m.addElement(chooser.getSelectedFile().getPath());
				}
				
			}
		});
		button_addSimfile.setIcon(ICON_PLUS);
		contentPane.add(button_addSimfile);
		
		button_removeSimfile = new JButton("");
		button_removeSimfile.setFont(DEFAULTFONT);
		button_removeSimfile.setToolTipText("Remove selected file(s)");
		button_removeSimfile.setBounds(COL2 - STD1, ROW8 + STDHEIH, STD1, STDHEIH);
		button_removeSimfile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DefaultListModel<String> m = (DefaultListModel<String>) list_CurrentSimFiles.getModel();
				List<String> remove = list_CurrentSimFiles.getSelectedValuesList();
				for(int i = 0; i < remove.size(); i++)m.removeElement(remove.get(i));
			}
		});
		button_removeSimfile.setIcon(ICON_X);
		contentPane.add(button_removeSimfile);
		
		button_StartSimulation = new JButton("Start Simulation");
		button_StartSimulation.setFont(TITLEFONT);
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
		button_StartSimulation.setBounds(WIDTH/2-WIDTH/10,HEIGHT-HEIGHT/8-STD3, WIDTH/5-STD1, STDHEIH);
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
		button_OpenLastOutput.setBounds(WIDTH/2-WIDTH/10,HEIGHT-HEIGHT/8-STD3+STDHEIH, WIDTH/5-STD1, STD/2);
    	button_OpenLastOutput.setEnabled(false);
		contentPane.add(button_OpenLastOutput);
		
		
		button_AdvancedOptions = new JButton("");
		button_AdvancedOptions.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				panel_AdvancedOptions.setVisible(!panel_AdvancedOptions.isVisible());
			}
		});
		button_AdvancedOptions.setToolTipText("Show Advanced Simulation Options");
		button_AdvancedOptions.setIcon(ICON_OPTIONS);
		button_AdvancedOptions.setBounds(WIDTH/2+WIDTH/10-STD1,HEIGHT-HEIGHT/8-STD3, STD1, STDHEIH+STD/2);
		contentPane.add(button_AdvancedOptions);
		
		panel_AdvancedOptions = new JPanel();
		panel_AdvancedOptions.setBounds(WIDTH/2+WIDTH/10, HEIGHT-HEIGHT/8-STD3, WIDTH/10, STDHEIH+STD/2);
		
		
		
		contentPane.add(panel_AdvancedOptions);
		panel_AdvancedOptions.setLayout(new BoxLayout(panel_AdvancedOptions, BoxLayout.Y_AXIS));
		panel_AdvancedOptions.setVisible(false);
		
		checkbox_debug = new JCheckBox("Debug Mode");
		checkbox_debug.setHorizontalAlignment(SwingConstants.LEFT);
		checkbox_debug.setToolTipText("Runs simulation in Debug Mode. Additional output is shown.");
		checkbox_debug.setFont(DEFAULTFONT);
		checkbox_debug.setIcon(new ScalingCheckBoxIcon(DEFAULTFONT.getSize()));
		panel_AdvancedOptions.add(checkbox_debug);
		
		checkbox_desmoj = new JCheckBox("Desmoj Output");
		checkbox_desmoj.setToolTipText("Create Desmoj output files.");
		checkbox_desmoj.setHorizontalAlignment(SwingConstants.LEFT);
		checkbox_desmoj.setFont(DEFAULTFONT);
		checkbox_desmoj.setIcon(new ScalingCheckBoxIcon(DEFAULTFONT.getSize()));
		panel_AdvancedOptions.add(checkbox_desmoj);
		
		
		
		scrollPane_plugins = new JScrollPane();
		scrollPane_plugins.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane_plugins.getVerticalScrollBar().setUnitIncrement(16);
		scrollPane_plugins.setToolTipText("Plugin List");
		scrollPane_plugins.setFont(DEFAULTFONT);
		scrollPane_plugins.setBounds(COL3, ROW2, WIDTH1, ROW9-ROW2);
		contentPane.add(scrollPane_plugins);
		


		panel_plugins = new Container();
		panel_plugins.setFont(DEFAULTFONT);
		panel_plugins.setLayout(new GridBagLayout());
		
		scrollPane_plugins.setViewportView(panel_plugins);
		scrollPane_plugins.getViewport().setBackground(ColorField2);
		
		
		button_allplugins = new JButton("Select/Deselect all");
		button_allplugins.addActionListener(new ActionListener() {
			boolean b = false;
			public void actionPerformed(ActionEvent arg0) {
				for(Component c : panel_plugins.getComponents()){
					if(c instanceof ListPanel)((ListPanel)c).setSelected(b);
				}
				b = !b;
			}
		});
		button_allplugins.setToolTipText("Select/Deselect all plugins");
		button_allplugins.setFont(DEFAULTFONT);
		scrollPane_plugins.setColumnHeaderView(button_allplugins);
		

		
		textfield_Plugins = new JTextField();
		textfield_Plugins.setHighlighter(null);
		textfield_Plugins.setFont(TITLEFONT);
		textfield_Plugins.setBackground(ColorField0);
		textfield_Plugins.setForeground(TITLEFONT_COLOR);
		textfield_Plugins.setText("Plugins");
		textfield_Plugins.setEditable(false);
		textfield_Plugins.setColumns(10);
		textfield_Plugins.setBounds(COL3, ROW1, WIDTH1, STD);
		contentPane.add(textfield_Plugins);
		
		textField_Console = new JTextField();
		textField_Console.setText("Console Output");
		textField_Console.setFont(TITLEFONT);
		textField_Console.setBackground(ColorField0);
		textField_Console.setForeground(TITLEFONT_COLOR);
		textField_Console.setEditable(false);
		textField_Console.setColumns(10);
		textField_Console.setBounds(COL1, ROW9+STD, COL3+WIDTH1-COL1, STD);
		contentPane.add(textField_Console);
		
		scrollPane_Console = new JScrollPane();
		scrollPane_Console.setBounds(COL1, ROW9+2*STD, COL3+WIDTH1-COL1, HEIGHT-HEIGHT/8-STD3 - (ROW9+3*STD));
		contentPane.add(scrollPane_Console);
		
		console = new Console();
		console.setHighlighter(null);
		console.setFont(CONSOLEFONT);
		console.setBackground(ColorField1);
		console.setEditable(false);
		scrollPane_Console.setViewportView(console);
		console.setMargin(LEFTMARGIN);
		
		System.setOut(console.getOut());
    	stdErr = System.err;
		

		loadPlugins();
		
	}

	private void loadPlugins() {
		System.setErr(console.getErr());
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
	}


	private void startSimulation(String resFilename, String[] bpmnFilename, String[] simFilenames) {

        button_StartSimulation.setEnabled(false);
        
    	boolean enableDebugLogging = checkbox_debug.isSelected();
        boolean redirectErrors = checkbox_debug.isSelected();
        boolean enableDesmojLogging = checkbox_desmoj.isSelected();
        
        DebugLogger.allowDebugLogging = enableDebugLogging;
        if(redirectErrors) System.setErr(console.getErr());
        else System.setErr(stdErr);
        
        boolean enableBpsLogging = true;
        
        boolean success = true;

        SimulationManager manager = new SimulationManager(DESMOJOUTPUTPATH, bpmnFilename, simFilenames, resFilename,
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
	

	public static ImageIcon resizeIcon(ImageIcon imageIcon,int w, int h) {
		Image img = imageIcon.getImage();
		Image scaled = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
		return new ImageIcon(scaled);
	}
}
