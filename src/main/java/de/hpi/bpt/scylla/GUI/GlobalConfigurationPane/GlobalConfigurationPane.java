package de.hpi.bpt.scylla.GUI.GlobalConfigurationPane;

import java.awt.AWTKeyStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.DocumentEvent;

import org.jdom2.JDOMException;

import de.hpi.bpt.scylla.GUI.ExpandPanel;
import de.hpi.bpt.scylla.GUI.FormManager;
import de.hpi.bpt.scylla.GUI.InsertRemoveListener;
import de.hpi.bpt.scylla.GUI.ListChooserPanel;
import de.hpi.bpt.scylla.GUI.ListChooserPanel.ComponentHolder;
import de.hpi.bpt.scylla.GUI.ScalingFileChooser;
import de.hpi.bpt.scylla.GUI.ScyllaGUI;
import de.hpi.bpt.scylla.creation.GlobalConfiguration.GlobalConfigurationCreator;
import de.hpi.bpt.scylla.creation.GlobalConfiguration.GlobalConfigurationCreator.ResourceType;
import de.hpi.bpt.scylla.creation.GlobalConfiguration.GlobalConfigurationCreator.Timetable;
import org.eclipse.wb.swing.FocusTraversalOnArray;

/**
 * 
 * @author Leon Bein
 *
 */
@SuppressWarnings("serial")
public class GlobalConfigurationPane extends JPanel implements FormManager{
	private JLabel labelFiletitle;
	private JTextField textfieldId;
	private JFormattedTextField textfieldSeed;
	private JComboBox<ZoneId> comboboxTimezone;

	private ListChooserPanel panelTimetables;
	/**List of all JComboboxes, that display timetables, in order to update their entries*/
	private List<JComboBox<String>> timetableObserverList;
	/**List of all known timetable ids, in order to pass them to newly created displays (e.g. Comboboxes)*/
	private List<String> timetables;
	private ListChooserPanel panelResources;
	
	/**Central XML-Link object*/
	private GlobalConfigurationCreator creator;
	/**File reference to the current opened gc file, null if none is opened*/
	private File file;
	/**Shows whether there are any unsaved changes*/
	private boolean saved;
	private JButton buttonSavefile;
	private JButton buttonSavefileAs;
	private JButton buttonClosefile;
	/**Flag to display if there are any non-user changes performed at user input objects,
	 * in order to prevent user input events to be fired.
	 * Integer value represents the number of methods/threads that are currently performing those changes,
	 * if equal 0 => no changes.*/
	private int changeFlag;

	/**
	 * Create the panel.
	 */
	public GlobalConfigurationPane() {
		setFocusable(true);
		setBackground(ScyllaGUI.ColorBackground);
		setLayout(new GridLayout(0, 1, 0, 0));
		
		//---Header panel---
		JPanel panelHeader = new JPanel();
		panelHeader.setFocusable(true);
		panelHeader.setBackground(Color.DARK_GRAY);
		GridBagLayout gbl_panelHeader = new GridBagLayout();
		panelHeader.setLayout(gbl_panelHeader);
		
		// Button "New file"
		JButton buttonNewfile = new JButton();
		buttonNewfile.setToolTipText("New File (Ctrl + N)");
		buttonNewfile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				be_create();
			}
		});
		buttonNewfile.setIcon(ScyllaGUI.ICON_NEW);
		GridBagConstraints gbc_buttonNewfile = new GridBagConstraints();
		gbc_buttonNewfile.weightx = 0;
		gbc_buttonNewfile.fill = GridBagConstraints.BOTH;
		panelHeader.add(buttonNewfile, gbc_buttonNewfile);
		
		// Button "Save file"
		buttonSavefile = new JButton();
		buttonSavefile.setToolTipText("Save (Ctrl + S)");
		setSaved(true);
		buttonSavefile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				be_save();
			}
		});
		buttonSavefile.setIcon(ScyllaGUI.ICON_SAVE);
		GridBagConstraints gbc_buttonSavefile = new GridBagConstraints();
		gbc_buttonSavefile.weightx = 0;
		gbc_buttonSavefile.fill = GridBagConstraints.BOTH;
		panelHeader.add(buttonSavefile, gbc_buttonSavefile);
		
		// Button "Save file as"
		buttonSavefileAs = new JButton("");
		buttonSavefileAs.setToolTipText("Save as (Ctrl + Shift + S)");
		buttonSavefileAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				be_saveAs();
			}
		});
		buttonSavefileAs.setIcon(ScyllaGUI.ICON_SAVEAS);
		GridBagConstraints gbc_buttonSavefileAs = new GridBagConstraints();
		gbc_buttonSavefileAs.weightx = 0;
		gbc_buttonSavefileAs.fill = GridBagConstraints.BOTH;
		panelHeader.add(buttonSavefileAs, gbc_buttonSavefileAs);
		
		// Button "Open file"
		JButton buttonOpenfile = new JButton();
		buttonOpenfile.setToolTipText("Open (Ctrl + O)");
		buttonOpenfile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				be_open();
			}
		});
		buttonOpenfile.setIcon(ScyllaGUI.ICON_OPEN);
		GridBagConstraints gbc_buttonOpenfile = new GridBagConstraints();
		gbc_buttonOpenfile.weightx = 0;
		gbc_buttonOpenfile.fill = GridBagConstraints.BOTH;
		panelHeader.add(buttonOpenfile, gbc_buttonOpenfile);
		
		//Label displaying file title
		labelFiletitle = new JLabel();
		labelFiletitle.setText("<No editor opened. Open an existing file or create a new one.>");
		labelFiletitle.setForeground(Color.WHITE);
		GridBagConstraints gbc_textfieldFiletitle = new GridBagConstraints();
		gbc_textfieldFiletitle.weightx = 37;
		gbc_textfieldFiletitle.insets = new Insets(0,ScyllaGUI.TITLEFONT.getSize(),  0, 0);
		gbc_textfieldFiletitle.fill = GridBagConstraints.BOTH;
		panelHeader.add(labelFiletitle, gbc_textfieldFiletitle);
		
		//Button "Close file"
		buttonClosefile = new JButton();
		buttonClosefile.setToolTipText("Close current");
		buttonClosefile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				be_close();
			}
		});
		buttonClosefile.setIcon(ScyllaGUI.resizeIcon(ScyllaGUI.ICON_X,ScyllaGUI.TITLEFONT.getSize(),ScyllaGUI.TITLEFONT.getSize()));
		GridBagConstraints gbc_buttonClosefile = new GridBagConstraints();
		gbc_buttonClosefile.weightx = 0;
		gbc_buttonClosefile.fill = GridBagConstraints.BOTH;
		panelHeader.add(buttonClosefile, gbc_buttonClosefile);
		
		//---- Root Scrollpane ----
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.getVerticalScrollBar().setUnitIncrement(32);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		add(scrollPane);
		
		scrollPane.setColumnHeaderView(panelHeader);

		//--- Main Panel ---
		JPanel panelMain = new JPanel();
		panelMain.setFocusable(true);
		panelMain.setBackground(ScyllaGUI.ColorBackground);
		scrollPane.setViewportView(panelMain);
		GridBagLayout gbl_panelMain = new GridBagLayout();
		panelMain.setLayout(gbl_panelMain);
		
		// -- General Information Panel --
		JPanel panelGeneral = new JPanel();
		panelGeneral.setFocusable(true);
		GridBagConstraints gbc_panelGeneral = new GridBagConstraints();
		gbc_panelGeneral.anchor = GridBagConstraints.PAGE_START;
		int inset_b = (int)(25.0*ScyllaGUI.SCALE);
		gbc_panelGeneral.insets = new Insets(inset_b,inset_b,inset_b,inset_b);
		gbc_panelGeneral.gridx = 0;
		gbc_panelGeneral.gridy = 0;
		gbc_panelGeneral.fill = GridBagConstraints.HORIZONTAL;
		gbc_panelGeneral.weightx = 1;
		panelMain.add(panelGeneral, gbc_panelGeneral);
		GridBagLayout gbl_panelGeneral = new GridBagLayout();
		gbl_panelGeneral.columnWeights = new double[]{1.0,3.0};
		panelGeneral.setLayout(gbl_panelGeneral);
		
		//Label id
		JLabel labelId = new JLabel();
		labelId.setText("ID");
		GridBagConstraints gbc_textfieldId = new GridBagConstraints();
		gbc_textfieldId.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
		gbc_textfieldId.fill = GridBagConstraints.HORIZONTAL;
		gbc_textfieldId.gridx = 0;
		gbc_textfieldId.gridy = 0;
		panelGeneral.add(labelId, gbc_textfieldId);
		
		//Text input field for id
		textfieldId = new JTextField();
		textfieldId.getDocument().addDocumentListener(new InsertRemoveListener((DocumentEvent e)->{
			if(isChangeFlag())return;
			creator.setId(textfieldId.getText());
			setSaved(false);
		}));
		GridBagConstraints gbc_textfieldIdEdit = new GridBagConstraints();
		gbc_textfieldIdEdit.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, inset_b);
		gbc_textfieldIdEdit.fill = GridBagConstraints.HORIZONTAL;
		gbc_textfieldIdEdit.gridx = 1;
		gbc_textfieldIdEdit.gridy = 0;
		panelGeneral.add(textfieldId, gbc_textfieldIdEdit);
		textfieldId.setColumns(10);
		
		//Label seed
		JLabel labelSeed = new JLabel();
		labelSeed.setText("Seed");
		GridBagConstraints gbc_textfieldSeed = new GridBagConstraints();
		gbc_textfieldSeed.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
		gbc_textfieldSeed.fill = GridBagConstraints.HORIZONTAL;
		gbc_textfieldSeed.gridx = 0;
		gbc_textfieldSeed.gridy = 1;
		panelGeneral.add(labelSeed, gbc_textfieldSeed);
		
		//Seed input field
		NumberFormat format = NumberFormat.getInstance();
		format.setGroupingUsed(false);
		textfieldSeed = new JFormattedTextField(format);
		textfieldSeed.getDocument().addDocumentListener(new InsertRemoveListener((DocumentEvent e)->{
			if(isChangeFlag())return;
			try{
				long s = creator.getSeed();
				long n = Long.parseLong(textfieldSeed.getText());
				if(s != n){
					creator.setSeed(n);
					setSaved(false);
				}
			}catch(Exception exc){}
		}));
		GridBagConstraints gbc_textfieldSeedEdit = new GridBagConstraints();
		gbc_textfieldSeedEdit.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, inset_b);
		gbc_textfieldSeedEdit.fill = GridBagConstraints.HORIZONTAL;
		gbc_textfieldSeedEdit.gridx = 1;
		gbc_textfieldSeedEdit.gridy = 1;
		panelGeneral.add(textfieldSeed, gbc_textfieldSeedEdit);
		textfieldSeed.setColumns(10);
		
		//Label timezone
		JLabel labelTimezone = new JLabel();
		labelTimezone.setText("Timezone");
		GridBagConstraints gbc_textfieldTimezone = new GridBagConstraints();
		gbc_textfieldTimezone.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
		gbc_textfieldTimezone.fill = GridBagConstraints.HORIZONTAL;
		gbc_textfieldTimezone.gridx = 0;
		gbc_textfieldTimezone.gridy = 2;
		panelGeneral.add(labelTimezone, gbc_textfieldTimezone);
		
		//25 Timezone from -12 to +12 (which are indeed equal)
		ZoneId[] timeZones = new ZoneId[25];
		for(int i = -12; i <= 12; i++){
			timeZones[i+12] = ZoneId.ofOffset("UTC",ZoneOffset.ofHours(i));
		}
		String[] timeZoneNames = new String[]{
				"Baker Island Time","Samoa Standard Time","Hawaii–Aleutian Standard Time","Alaska Standard Time","Pacific Standard Time (North America)",
				"Mountain Standard Time (North America)","Central Standard Time (North America)", "Eastern Standard Time (North America)", "Venezuelan Standard Time", "Brasilia Time",
				"South Georgia and the South Sandwich Islands Time", "Eastern Greenland Time", "Coordinated Universal Time/Greenwich Mean Time", "Central European Time", "Eastern European Time",
				"Moscow Time", "Georgia Standard Time", "Pakistan Standard Time", "Bangladesh Standard Time", "Indochina Time",
				"Western Standard Time", "Japan Standard Time", "Australian Eastern Standard Time", "Vanuatu Time", "New Zealand Standard Time", ""
		};
		//Timezone input combobox
		comboboxTimezone = new JComboBox<ZoneId>(timeZones);
		comboboxTimezone.setRenderer(new DefaultListCellRenderer(){
			@Override 
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				int i = 0;
				for(; i < timeZones.length; i++){
					if(timeZones[i].equals(value))break;
				}
				if(value == null)value = "";
				return super.getListCellRendererComponent(list, value.toString()+" "+timeZoneNames[i], index, isSelected, cellHasFocus);
			}
				       
		});
		comboboxTimezone.addItemListener((ItemEvent e)->{
			if(e.getStateChange() == ItemEvent.SELECTED){
				if(isChangeFlag())return;
				creator.setTimeOffset(((ZoneId)comboboxTimezone.getSelectedItem()).getRules().getStandardOffset(null));
				setSaved(false);
			}
		});
		GridBagConstraints gbc_comboboxTimezone = new GridBagConstraints();
		gbc_comboboxTimezone.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, inset_b);
		gbc_comboboxTimezone.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboboxTimezone.gridx = 1;
		gbc_comboboxTimezone.gridy = 2;
		panelGeneral.add(comboboxTimezone, gbc_comboboxTimezone);
		
		//---Resource Panel---
		panelResources = new ListChooserPanel(){

			@Override
			public void onDelete(ComponentHolder toDel) {
				creator.removeResourceType(toDel.toString());
				setSaved(false);
			}

			@Override
			public ComponentHolder onCreate() {
				setSaved(false);
				return newResource(
						creator.addResourceType("<enter name>")
				);
			}
			
		};
		GridBagConstraints gbc_panelResources = new GridBagConstraints();
		gbc_panelResources.anchor = GridBagConstraints.PAGE_START;
		gbc_panelResources.fill = GridBagConstraints.HORIZONTAL;
		gbc_panelResources.weightx = 1.0;
		gbc_panelResources.weighty = 1.0;
		gbc_panelResources.insets = new Insets(0, inset_b, inset_b, inset_b);
		gbc_panelResources.gridx = 0;
		gbc_panelResources.gridy = 1;

		//Resource title label
		JLabel resourceLabel = new JLabel("Resources");
		resourceLabel.setBackground(ScyllaGUI.ColorField0);
		resourceLabel.setForeground(ScyllaGUI.TITLEFONT_COLOR);
		resourceLabel.setFont(ScyllaGUI.TITLEFONT);
		resourceLabel.setOpaque(true);
		ExpandPanel panelResourcesExpand = new ExpandPanel(resourceLabel, panelResources);
		panelResourcesExpand.expand();
		panelMain.add(panelResourcesExpand, gbc_panelResources);
		

		//Timetable initialization
		timetableObserverList = new ArrayList<JComboBox<String>>();
		timetables = new ArrayList<String>();
		timetables.add("");//"No timetable"-option
		
		//--- Timetable panel ---
		panelTimetables = new ListChooserPanel(){

			@Override
			public void onDelete(ComponentHolder toDel) {
				String toDels = toDel.toString();
				for(JComboBox<String> cbm : getTimetableObserverList()){
					String sel = (String)cbm.getSelectedItem();
					cbm.removeItem(toDels);
					if(sel != null && sel.equals(toDels))cbm.setSelectedItem("");
				}
				timetables.remove(toDels);
				creator.deleteTimetable(toDels);
				setSaved(false);
			}

			@Override
			public ComponentHolder onCreate() {
				setSaved(false);
				//Note: Newly created timetables/resources are not added until they are renamed
//				for(JComboBox<String> cbm : getTimetableObserverList()){
//					cbm.addItem("<enter name>");
//				}
//				timetables.add("<enter name>");
				return newTimetable(
						creator.createTimetable("<enter name>")
				);
			}
			
		};
		GridBagConstraints gbc_panelTimetables = new GridBagConstraints();
		gbc_panelTimetables.anchor = GridBagConstraints.PAGE_START;
		gbc_panelTimetables.fill = GridBagConstraints.HORIZONTAL;
		gbc_panelTimetables.weightx = 1.0;
		gbc_panelTimetables.weighty = 1.0;
		gbc_panelTimetables.insets = new Insets(0, inset_b, inset_b, inset_b);
		gbc_panelTimetables.gridx = 0;
		gbc_panelTimetables.gridy = 2;
		
		//Timetable title label
		JLabel timetableLabel = new JLabel("Timetables");
		timetableLabel.setBackground(ScyllaGUI.ColorField0);
		timetableLabel.setForeground(ScyllaGUI.TITLEFONT_COLOR);
		timetableLabel.setFont(ScyllaGUI.TITLEFONT);
		timetableLabel.setOpaque(true);
		ExpandPanel panelTimetablesExpand = new ExpandPanel(timetableLabel, panelTimetables);
		panelTimetablesExpand.expand();
		panelMain.add(panelTimetablesExpand, gbc_panelTimetables);
		
		//Layout fixing, empty buffer panel
		JPanel panelBuffer = new JPanel();
		panelBuffer.setFocusable(true);
		panelBuffer.setBackground(panelMain.getBackground());
		GridBagConstraints gbc_panelBuffer = new GridBagConstraints();
		gbc_panelBuffer.anchor = GridBagConstraints.PAGE_START;
		gbc_panelBuffer.fill = GridBagConstraints.BOTH;
		gbc_panelBuffer.weighty = 100;
		gbc_panelBuffer.weightx = 1;
		gbc_panelBuffer.gridx = 0;
		gbc_panelBuffer.gridy = 3;
		panelMain.add(panelBuffer,gbc_panelBuffer);
		
		//Disable as no gc is opened
		setEnabled(false);
		//Add enter traversal for textfields etc.
		Set<AWTKeyStroke> forwardKeys = new HashSet<AWTKeyStroke>(getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
		forwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
		setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, forwardKeys);
        
		setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{buttonNewfile, buttonSavefile, buttonSavefileAs, buttonOpenfile, buttonClosefile, textfieldId, textfieldSeed, comboboxTimezone, panelResourcesExpand, panelTimetablesExpand}));
		
	}
	
	/**
	 * Initialization method that has to be called directly after the constructor,
	 * but can only be called if the panel has already been added to a parent.<br>
	 * Initializes keybindings, as they won't work otherwise
	 */
	public void init(){
		JTabbedPane parent = (JTabbedPane) getParent();
		parent.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_S,InputEvent.CTRL_DOWN_MASK),"save");
		parent.getActionMap().put("save", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//Workaround: adding directly to this.getInputMap does not work, but adding to parent also triggers events when this panel is not selected
				if(parent.getSelectedComponent() != GlobalConfigurationPane.this)return;
				be_save();
			}
		});
		parent.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_S,InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK),"saveAs");
		parent.getActionMap().put("saveAs", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(parent.getSelectedComponent() != GlobalConfigurationPane.this)return;
				be_saveAs();
			}
		});
		parent.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_O,InputEvent.CTRL_DOWN_MASK),"open");
		parent.getActionMap().put("open", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(parent.getSelectedComponent() != GlobalConfigurationPane.this)return;
				be_open();
			}
		});
		parent.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_N,InputEvent.CTRL_DOWN_MASK),"new");
		parent.getActionMap().put("new", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(parent.getSelectedComponent() != GlobalConfigurationPane.this)return;
				be_create();
			}
		});
	}
	
	/**
	 * Button event for creating new gc files
	 */
	private void be_create(){
		setChangeFlag(true);
		labelFiletitle.setText("<unsaved file>");
		createGC();
		setChangeFlag(false);
		//Id should be edited right after creation
		textfieldId.setText("NewGlobalConfiguration");
		textfieldId.requestFocusInWindow();
		textfieldId.selectAll();
	}
	
	/**
	 * Creates a new global configuration
	 */
	private void createGC(){
		creator = new GlobalConfigurationCreator();
		setSaved(false);
		setEnabled(true);
	}

	/**
	 * Button event for opening existing gcs
	 */
	private void be_open(){	
		//Show unsaved changes dialog; if cancel is pressed the whole process is canceled
		if(!saved){
			int i = showUnsavedChangesDialog();
			if(i != 1)return;
		}
		//Choose file to be opened
		ScalingFileChooser chooser = new ScalingFileChooser(ScyllaGUI.DEFAULTFILEPATH);
		chooser.setDialogTitle("Open");
		int c = chooser.showDialog(this,"Open");
		//if the process is canceled, nothing happens
		if(c == ScalingFileChooser.APPROVE_OPTION){
			file = chooser.getSelectedFile();
			if(file != null){
				//Close current opened file
				closeGC();
				//Update default file path
				ScyllaGUI.DEFAULTFILEPATH = chooser.getSelectedFile().getPath();
				try {
					creator = GlobalConfigurationCreator.createFromFile(file.getPath());
					labelFiletitle.setText(chooser.getSelectedFile().getPath());
					openGC();
				} catch (JDOMException | IOException e) {
					e.printStackTrace();
				}
			}else{
				System.err.println("Could not find file");//TODO
			}
		}
		
	}	
	
	/**
	 * Opens the gc that is currently loaded in the creator
	 */
	private void openGC(){
		setChangeFlag(true);
		textfieldId.setText(creator.getId());
		if(creator.getSeed() != null)textfieldSeed.setValue(creator.getSeed());
		if(creator.getTimeOffset() != null)comboboxTimezone.setSelectedItem(ZoneId.ofOffset("UTC",creator.getTimeOffset()));
		
		for(Timetable t : creator.getTimetables()){
			importTimetable(t);
		}
		for(ResourceType res : creator.getResourceTypes()){
			importResource(res);
		}
		setChangeFlag(false);
		setEnabled(true);
	}

	/**
	 * Button event for save button,
	 * overrides file if already existing, otherwise opens "save as" dialog
	 */
	private void be_save(){
		if(saved)return;
		if(file != null)saveGC();
		else be_saveAs();
	}
	
	/**
	 * Button event for "save as" button
	 */
	private void be_saveAs(){
		//Select file and confirm override if file is already existing
		ScalingFileChooser chooser = new ScalingFileChooser(ScyllaGUI.DEFAULTFILEPATH){
			@Override
			public void approveSelection(){
				File f = getSelectedFile();
				if(f == null)return;
				if(f.exists()){
					int override = JOptionPane.showConfirmDialog(this, "File already exists. Override?","Confirm Override",JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE);
					switch(override){
					case JOptionPane.YES_OPTION:
						super.approveSelection();
						return;
					case JOptionPane.CANCEL_OPTION:
						super.cancelSelection();
						return;
					default : return;
					}
				}else{
					super.approveSelection();
				}
			}
		};
		if(file != null)chooser.setSelectedFile(file);
		else if(!creator.getId().equals("")) chooser.setSelectedFile(new File(ScyllaGUI.DEFAULTFILEPATH+"\\"+creator.getId()+".xml"));
		chooser.setDialogTitle("Save");
		int c = chooser.showDialog(null,"Save");
		if(c == ScalingFileChooser.APPROVE_OPTION){
			file = chooser.getSelectedFile();
			if(file != null){
				labelFiletitle.setText(chooser.getSelectedFile().getPath());
				ScyllaGUI.DEFAULTFILEPATH = chooser.getSelectedFile().getPath();
				saveGC();
			}else{
				System.err.println("Could not find file");//TODO
			}
		}
	}
	
	/**
	 * Saves the gc to the current file
	 */
	private void saveGC(){
		try {
			setSaved(true);
			creator.save(file.getPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Button event close
	 * Closes gc but asks to save unsaved changes if existing
	 */
	private void be_close(){
		if(!saved){
			int i = showUnsavedChangesDialog();
			//If cancel was pressed OR the gc was saved, it is closed
			if(i == 1)closeGC();
			//else return;
		}else closeGC();
	}
	/**
	 * Closes the gc, resets, clears and disables all fields
	 */
	private void closeGC(){
		setChangeFlag(true);
		creator = null;
		labelFiletitle.setText("<No editor opened. Open an existing file or create a new one.>");
		textfieldId.setText("");
		textfieldSeed.setValue(null);
		comboboxTimezone.setSelectedItem(null);
		
		panelResources.clear();
		panelTimetables.clear();
		timetableObserverList.clear();
		timetables.clear();
		timetables.add("");
		setChangeFlag(false);
		setSaved(true);
		setEnabled(false);
	}
	
	/**
	 * Shows a dialog to save the current gc
	 * @return
	 * 	-1 : if canceled by closing the dialog OR if save changes is pressed and canceled
	 * 	1 : if discard changes is pressed OR if save changes is pressed and it is saved
	 * 	2 : if canceled by pressing cancel
	 * 
	 */
	private int showUnsavedChangesDialog(){
		int i = JOptionPane.showOptionDialog(
				this,
				getFileName()+"has unsaved changes. Would you like to save them?",
				"Unsaved Changes",
				JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE,
				ScyllaGUI.ICON_SAVE,
				new Object[]{new JButton("Save Changes"){{
					setIcon(ScyllaGUI.ICON_SAVE);
					addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
							((JDialog)getTopLevelAncestor()).dispose();
							be_save();
							//closeGC();
						}
					});
				}},"Discard Changes","Cancel"}, 0);
		if(saved)i = 1;
		return i;
	}
	

	@Override
	public void setSaved(boolean b){
		saved = b;
		//Also set the save button to display if saved or not
		buttonSavefile.setEnabled(!b);
	}
	
	@Override
	public boolean isChangeFlag() {
		//If the counter is higher than 0, at least one method is performing changes
		return changeFlag > 0;
	}
	
	@Override
	public void setChangeFlag(boolean b) {
		//Increment the counter, if another method starts doing changes,
		if(b)changeFlag++;
		//Decrement the counter, if a method stops doing changes
		else changeFlag--;
		//Throw an error (type should be changed), if more methods stop than having started
		if(changeFlag < 0)throw new java.lang.NegativeArraySizeException();
	}
	
	@Override
	public List<JComboBox<String>> getTimetableObserverList() {
		return timetableObserverList;
	}

	@Override
	public List<String> getTimetables() {
		return timetables;
	}

	/**
	 * @return The name of the current opened gc, if already a file exists, the filename is taken
	 */
	private String getFileName(){
		if(file != null)return file.getName();
		else return labelFiletitle.getText();
	}
	

	/**
	 * Enables or disables all User input components
	 */
	@Override
	public void setEnabled(boolean b){
		buttonClosefile.setEnabled(b);
		buttonSavefileAs.setEnabled(b);
		textfieldId.setEnabled(b);
		textfieldSeed.setEnabled(b);
		comboboxTimezone.setEnabled(b);
		panelTimetables.setEnabled(b);
		panelResources.setEnabled(b);
		super.setEnabled(b);
	}
	
	/**
	 * Imports a resource to the resource panel, i.e. creating a panel/page for it
	 * @param res : Wrapper object of the resource to add
	 */
	public void importResource(ResourceType res){
		panelResources.add(
				newResource(res)
		);
	}
	
	/**
	 * Creates a panel for a given resource
	 * @param res : Wrapper of the given resource
	 * @return : A new componentholder for a ResourcePanel object
	 * @see {@link ResourcePanel}
	 */
	private ComponentHolder newResource(ResourceType res){
		return new ListChooserPanel.ComponentHolder() {
			ResourcePanel p = new ResourcePanel(GlobalConfigurationPane.this);
			{
				p.setResourceType(res);
			}
			@Override
			public Component getComponent() {
				return p;
			}
			@Override
			public String toString(){
				return res.getId();
			}
			@Override
			public void setName(String s){
				String t = s;
				int i = 2;
				//If the name has changed, but is already given,
				//it will be serially numbered, and the first free number is chosen
				if(!res.getId().equals(s)){
					while(creator.getResourceType(t) != null){
						t = s+"("+i+")";
						i++;
					}
				}
				res.setId(t);
				setSaved(false);
			}
		};
	}
	
	/**
	 * Imports a timetable, i.e. creates a TimetablePanel object and adds it to the timeable panel
	 * @param t : Wrapper for the timetable
	 */
	public void importTimetable(Timetable t){
		panelTimetables.add(newTimetable(t));
		for(JComboBox<String> cbm : getTimetableObserverList()){
			cbm.addItem(t.getId());
		}
		timetables.add(t.getId());
	}
	
	/**
	 * Creates a panel for a given timetable
	 * @param res : Wrapper of the given timetable
	 * @return : A new componentholder for a TimetablePanel object
	 * @see {@link TimetablePanel}
	 */
	private ComponentHolder newTimetable(Timetable t){
		return new ListChooserPanel.ComponentHolder() {
			TimetablePanel p = new TimetablePanel(GlobalConfigurationPane.this);
			{
				p.setTimetable(t);
			}
			@Override
			public Component getComponent() {
				return p;
			}
			@Override
			public String toString(){
				return t.getId();
			}
			@Override
			public void setName(String n){
				String s = n;
				//If the name has changed, but is already given,
				//it will be serially numbered, and the first free number is chosen
				int i = 2;
				if(!t.getId().equals(n)){
					while(creator.getTimetable(s) != null){
						s = n+"("+i+")";
						i++;
					}
				}
				//Notify timetable observer
				for(JComboBox<String> cbm : getTimetableObserverList()){
					String sel = (String) cbm.getSelectedItem();
					cbm.removeItem(t.getId());
					cbm.addItem(s);
					//Reset combobox selection to null, as it will automatically change when the set is changed
					if(sel == null || sel.equals(""))cbm.setSelectedItem("");
					//If the current item is the selected one, it is also renamed in the box
					else if(sel.equals(t.getId()))cbm.setSelectedItem(s);
					
				}
				//Update timetable list
				timetables.remove(t.getId());
				timetables.add(s);
				//Set id
				t.setId(s);
				setSaved(false);
			}
		};
	}

	
	
	
	
}