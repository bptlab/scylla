package de.hpi.bpt.scylla.GUI;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.ColorUIResource;

import de.hpi.bpt.scylla.GUI.SimulationPane.SimulationPane;
import de.hpi.bpt.scylla.GUI.WebSwing.WebSwingUtils;
/**
 * Scylla UI Main class, provides UI constants and starts the UI.
 * @author Leon Bein
 */
@SuppressWarnings("serial")
public class ScyllaGUI extends JFrame {
	
	/**A Developer variable to suppress e.g. output forwarding*/
	public static final boolean DEBUG = false;
	
	/**Tells whether the programm is running in a jar or not*/
	public static final boolean INJAR = ScyllaGUI.class.getResource(ScyllaGUI.class.getSimpleName()+".class").toString().startsWith("jar:");
	
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
	/**Color for error messages*/
	public static final Color ERRORFONT_COLOR = new Color(255,0,0);
	
	/**Padding margin for textfields*/
	public static final Insets LEFTMARGIN = new Insets(0, WIDTH/48, 0, 0);
	
	/**Icon for adding objects*/
	public static final ImageIcon ICON_PLUS = resizeIcon(new ImageIcon(getResource("/GUI/plus.png")),ICONSIZE,ICONSIZE);
	/**Icon for deleting/removing objects*/
	public static final ImageIcon ICON_REMOVE = resizeIcon(new ImageIcon(getResource("/GUI/remove.png")),ICONSIZE,ICONSIZE);
	/**Icon to exit or close (can also be used for remove)*/
	public static final ImageIcon ICON_CLOSE = resizeIcon(new ImageIcon(getResource("/GUI/close.png")),TITLEFONT.getSize(),TITLEFONT.getSize());
	/**Icon for "further options"*/
	public static final ImageIcon ICON_MORE = resizeIcon(new ImageIcon(getResource("/GUI/more.png")),ICONSIZE,ICONSIZE);
	/**Icon for editing*/
	public static final ImageIcon ICON_EDIT = resizeIcon(new ImageIcon(getResource("/GUI/edit.png")),ICONSIZE,ICONSIZE);
	
	/**Icon for global config*/
	public static final ImageIcon ICON_GLOBALCONF = resizeIcon(new ImageIcon(getResource("/GUI/globalConf.png")),DEFAULTFONT.getSize(),DEFAULTFONT.getSize());
	/**Icon for sim config*/
	public static final ImageIcon ICON_SIMCONF = resizeIcon(new ImageIcon(getResource("/GUI/simConf.png")),DEFAULTFONT.getSize(),DEFAULTFONT.getSize());
	
	/**Icon to expand*/
	public static final ImageIcon ICON_EXPAND = resizeIcon(new ImageIcon(getResource("/GUI/expand.png")),DEFAULTFONT.getSize(),DEFAULTFONT.getSize());
	/**Icon to collapse*/
	public static final ImageIcon ICON_COLLAPSE = resizeIcon(new ImageIcon(getResource("/GUI/collapse.png")),DEFAULTFONT.getSize(),DEFAULTFONT.getSize());
	
	/**Icon for options*/
	public static final ImageIcon ICON_OPTIONS = resizeIcon(new ImageIcon(getResource("/GUI/options.png")),ICONSIZE,ICONSIZE);
	
	/**Icon for new file*/
	public static final ImageIcon ICON_NEW = resizeIcon(new ImageIcon(getResource("/GUI/newfile.png")),TITLEFONT.getSize(),TITLEFONT.getSize());
	/**Icon for save file*/
	public static final ImageIcon ICON_SAVE = resizeIcon(new ImageIcon(getResource("/GUI/save.png")),TITLEFONT.getSize(),TITLEFONT.getSize());
	/**Icon for save file as*/
	public static final ImageIcon ICON_SAVEAS = resizeIcon(new ImageIcon(getResource("/GUI/saveAs.png")),TITLEFONT.getSize(),TITLEFONT.getSize());
	/**Icon for open file*/
	public static final ImageIcon ICON_OPEN = resizeIcon(new ImageIcon(getResource("/GUI/open.png")),TITLEFONT.getSize(),TITLEFONT.getSize());

	
	public static final String ACTIONKEY_NEW = "new";
	public static final String ACTIONKEY_OPEN = "open";
	public static final String ACTIONKEY_SAVE = "save";
	public static final String ACTIONKEY_SAVEAS = "saveas";
	
	
	

	/**Simulation pane for running and configuring simulations*/
	private SimulationPane simulationPane;
	/**Tabpane to switch between the panes*/
	private JTabbedPane contentPane;
	
	/**Reference to the actual error output*/
	private static final PrintStream stdErr = System.err;
	


	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		if(args.length != 1)throw new IllegalArgumentException("Internal error: Number of main parameters does not match!");
		WebSwingUtils.init(args[0]);
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
		UIManager.put("ScrollPane.border", "");
		
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

		simulationPane = new SimulationPane(this);
	    contentPane = new JTabbedPane(JTabbedPane.TOP);
		setContentPane(contentPane);
	    initKeyBindings();
		contentPane.addTab("Simulation", simulationPane);
		
		if(!DEBUG)System.setOut(simulationPane.getConsole().getOut());
		setUndecorated(true);
	}
	
	public static Image getResource(String path) {
		String prefix = INJAR ? "/resources" : "";
		try {
			return ImageIO.read(ScyllaGUI.class.getResourceAsStream(prefix+path));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
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
	
	/**
	 * Initializes key bindings and key event redirection to the current selected panel
	 */
	public void initKeyBindings(){
		contentPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_S,InputEvent.CTRL_DOWN_MASK),ACTIONKEY_SAVE);
		contentPane.getActionMap().put(ACTIONKEY_SAVE, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				((JComponent)contentPane.getSelectedComponent()).getActionMap().get(ACTIONKEY_SAVE).actionPerformed(e);
			}
		});
		contentPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_S,InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK),ACTIONKEY_SAVEAS);
		contentPane.getActionMap().put(ACTIONKEY_SAVEAS, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				((JComponent)contentPane.getSelectedComponent()).getActionMap().get(ACTIONKEY_SAVEAS).actionPerformed(e);
			}
		});
		contentPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_O,InputEvent.CTRL_DOWN_MASK),ACTIONKEY_OPEN);
		contentPane.getActionMap().put(ACTIONKEY_OPEN, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				((JComponent)contentPane.getSelectedComponent()).getActionMap().get(ACTIONKEY_OPEN).actionPerformed(e);
			}
		});
		contentPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_N,InputEvent.CTRL_DOWN_MASK),ACTIONKEY_NEW);
		contentPane.getActionMap().put(ACTIONKEY_NEW, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				((JComponent)contentPane.getSelectedComponent()).getActionMap().get(ACTIONKEY_NEW).actionPerformed(e);
			}
		});
	}
	
	public void addEditor(EditorPane component) {
		contentPane.addTab("", component);
		contentPane.setTabComponentAt(contentPane.indexOfComponent(component), new EditorTabTitlePanel(contentPane, component));
		contentPane.setSelectedComponent(component);
	}
	
}