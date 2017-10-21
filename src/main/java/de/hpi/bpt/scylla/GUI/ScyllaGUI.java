package de.hpi.bpt.scylla.GUI;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.ColorUIResource;

import de.hpi.bpt.scylla.GUI.GlobalConfigurationPane.GlobalConfigurationPane;
import de.hpi.bpt.scylla.GUI.SimulationConfigurationPane.SimulationConfigurationPane;
/**
 * Scylla UI Main class, provides UI constants and starts the UI.
 * @author Leon Bein
 */
@SuppressWarnings("serial")
public class ScyllaGUI extends JFrame {
	
	/** Default path for input and output files*/
	public static String DEFAULTFILEPATH = "samples\\";
	/** Path for desmoj output files*/
	public static final String DESMOJOUTPUTPATH = "desmoj_output\\";

	
	/**Color for titles and other highlighted bars*/
	public static final Color ColorField0 = new Color(45,112,145);
	/**Color mostly for buttons and static contents*/
	public static final Color ColorField1 = new Color(240,240,240);
	/**Color mostly for user input (textfields etc.) and dynamic contents; also usable as background color*/
	public static final Color ColorField2 = new Color(255,255,255);
	/**Main background Color*/
	public static final Color ColorBackground = new Color(0,142,185);

	/**Graphics environment in order to determine the screen size*/
	private static GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
	/**Screen rectangle*/
	private static java.awt.Rectangle r = env.getMaximumWindowBounds();

	/**Screen width*/
	public static final int WIDTH = r.width;//1200
	/**Screen height*/
	public static final int HEIGHT = r.height;//900
	/**General scale; calculated as height scale to a 1200x900 display*/
	public static final double SCALE = ((double)HEIGHT)/900.0;


//	private static double SCALE = 1;
//	private static int WIDTH = 1200;//(int)(1200.0 * SCALE);
//	private static int HEIGHT = 900;//(int)(900 * SCALE);

	/**Standard size for button etc. icons*/
	public static final int ICONSIZE = WIDTH/64;
	/**Standard inset width*/
	public static final int STDINSET = (int)(2.5*SCALE);
	
	/**Font for standard small text*/
	public static final Font DEFAULTFONT = new Font("Arial", Font.PLAIN, (int)(16.0*SCALE));
	/**Color for standard small text*/
	public static final Color DEFAULTFONT_COLOR = new Color(0,0,0);
	/**Font for standard larger text and titles*/
	public static final Font TITLEFONT = new Font(DEFAULTFONT.getFontName(), Font.PLAIN, (int)(20.0*SCALE));
	/**Color for standard larger text and titles*/
	public static final Color TITLEFONT_COLOR = new Color(255,255,255);
	/**Font used by filechoosers in order to make them scale correctly*/
	public static final Font FILECHOOSERFONT = new Font("Arial", Font.PLAIN, (int)(14.0*SCALE));
	/**Font used for the console*/
	public static final Font CONSOLEFONT = new Font("Consolas",Font.PLAIN,(int)(14.0*SCALE));

	/**Padding margin for textfields*/
	public static final Insets LEFTMARGIN = new Insets(0, WIDTH/48, 0, 0);
	
	/**Icon for adding objects*/
	public static final ImageIcon ICON_PLUS = resizeIcon(new ImageIcon(ScyllaGUI.class.getResource("/GUI/plus.png")),ICONSIZE,ICONSIZE);
	/**Icon for deleting/removing objects or to exit*/
	public static final ImageIcon ICON_X = resizeIcon(new ImageIcon(ScyllaGUI.class.getResource("/GUI/remove.png")),ICONSIZE,ICONSIZE);
	/**Icon for "further options"*/
	public static final ImageIcon ICON_MORE = resizeIcon(new ImageIcon(ScyllaGUI.class.getResource("/GUI/more.png")),ICONSIZE,ICONSIZE);
	
	/**Icon to expand*/
	public static final ImageIcon ICON_EXPAND = resizeIcon(new ImageIcon(ScyllaGUI.class.getResource("/GUI/expand.png")),DEFAULTFONT.getSize(),DEFAULTFONT.getSize());
	/**Icon to collapse*/
	public static final ImageIcon ICON_COLLAPSE = resizeIcon(new ImageIcon(ScyllaGUI.class.getResource("/GUI/collapse.png")),DEFAULTFONT.getSize(),DEFAULTFONT.getSize());
	
	/**Icon for options*/
	public static final ImageIcon ICON_OPTIONS = resizeIcon(new ImageIcon(ScyllaGUI.class.getResource("/GUI/options.png")),ICONSIZE,ICONSIZE);
	
	/**Icon for new file*/
	public static final ImageIcon ICON_NEW = resizeIcon(new ImageIcon(ScyllaGUI.class.getResource("/GUI/newfile.png")),TITLEFONT.getSize(),TITLEFONT.getSize());
	/**Icon for save file*/
	public static final ImageIcon ICON_SAVE = resizeIcon(new ImageIcon(ScyllaGUI.class.getResource("/GUI/save.png")),TITLEFONT.getSize(),TITLEFONT.getSize());
	/**Icon for save file as*/
	public static final ImageIcon ICON_SAVEAS = resizeIcon(new ImageIcon(ScyllaGUI.class.getResource("/GUI/saveas.png")),TITLEFONT.getSize(),TITLEFONT.getSize());
	/**Icon for open file*/
	public static final ImageIcon ICON_OPEN = resizeIcon(new ImageIcon(ScyllaGUI.class.getResource("/GUI/open.png")),TITLEFONT.getSize(),TITLEFONT.getSize());
	

	/**Simulation pane for running and configuring simulations*/
	private SimulationPane simulationPane;
	/**Pane to create and edit global configurations*/
	private GlobalConfigurationPane globalconfPane;
	/**Pane to create and edit simulation configurations*/
	private SimulationConfigurationPane simconfPane;
	/**Tabpane to switch between the panes*/
	private JTabbedPane contentPane;
	
	/**Reference to the actual error output*/
	private static final PrintStream stdErr = System.err;
	


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

		//Set all known font keys
		setDefaultFont(ScyllaGUI.TITLEFONT);
		
		//Change tooltip look and feel to match the program and to scale correctly
		UIManager.put("ToolTip.background", ScyllaGUI.ColorField1);
		int border = (int)(1.5*SCALE);
		UIManager.put("ToolTip.border", BorderFactory.createMatteBorder(border,border,border,border, ScyllaGUI.ColorField1.darker()));
		UIManager.put("ToolTip.font", ScyllaGUI.DEFAULTFONT);

		//Set button look and feel to remove focus and rollover effects
		UIManager.put("Button.background",ScyllaGUI.ColorField1);
		UIManager.put("Button.select",ScyllaGUI.ColorField1.darker());
		UIManager.put("Button.focus", new ColorUIResource(new Color(0, 0, 0, 0)));
		UIManager.put("Button.rollover",false);

		//Set textfield colors
		UIManager.put("TextField.background", ScyllaGUI.ColorField2);
		UIManager.put("TextField.foreground", ScyllaGUI.DEFAULTFONT_COLOR);
		//Set Combobox background
		UIManager.put("ComboBox.background", ScyllaGUI.ColorField2);
		
		//Set list and table selection background
		UIManager.put("List.selectionBackground", ScyllaGUI.ColorField1);
		UIManager.put("Table.selectionBackground", ScyllaGUI.ColorField1);
		
		//Scale ScrollBar widths
		UIManager.put("ScrollBar.width", (int) ((int)UIManager.get("ScrollBar.width") * SCALE));
		
		//Set tabpane font
		UIManager.put("TabbedPane.font", ScyllaGUI.TITLEFONT);
		
		//Set Locale to english in order to get one consistent language, unit system, etc.
		Locale.setDefault(Locale.ENGLISH);
		JComponent.setDefaultLocale(Locale.ENGLISH);
	
		//Init frame
		setTitle("Scylla GUI");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100,WIDTH,HEIGHT);
	    if(WIDTH == r.getWidth() && HEIGHT == r.getHeight())setExtendedState(JFrame.MAXIMIZED_BOTH);

		simulationPane = new SimulationPane();
		globalconfPane = new GlobalConfigurationPane();
		simconfPane = new SimulationConfigurationPane();
	    contentPane = new JTabbedPane(JTabbedPane.TOP);
		setContentPane(contentPane);
		contentPane.addTab("Simulation", simulationPane);
		contentPane.addTab("Global Configuration Editor", globalconfPane);
		globalconfPane.init();
		contentPane.addTab("Under Construction", simconfPane);
		
		//TODO System.setOut(simulationPane.getConsole().getOut());
	}
	
	/**
	 * Utility method: Rescales a given imageicon
	 * @param imageIcon : The icon to rescale
	 * @param w : The new width
	 * @param h : The new height
	 * @return A new ImageIcon with the same image but the new measurements
	 */
	public static ImageIcon resizeIcon(ImageIcon imageIcon,int w, int h) {
		Image img = imageIcon.getImage();
		Image scaled = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
		return new ImageIcon(scaled);
	}


	/**
	 * @return the standard error output stream
	 */
	public static PrintStream getStdErr() {
		return stdErr;
	}
	
	/**
	 * Sets all UIManager font keys to the given font
	 * @param font : The font to be set as default
	 */
	public static void setDefaultFont(Font font){
		Enumeration<Object> keys = UIManager.getDefaults().keys();
		for(Object key : Collections.list(keys)){
	    	if (key.toString().endsWith(".font"))UIManager.put (key, font);
	    }
	}
	
}