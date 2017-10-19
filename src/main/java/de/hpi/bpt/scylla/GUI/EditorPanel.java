package de.hpi.bpt.scylla.GUI;

import java.awt.AWTKeyStroke;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;

import org.jdom2.JDOMException;



@SuppressWarnings("serial")
public abstract class EditorPanel extends JPanel implements FormManager{
	
	/**Flag to display if there are any non-user changes performed at user input objects,
	 * in order to prevent user input events to be fired.
	 * Integer value represents the number of methods/threads that are currently performing those changes,
	 * if equal 0 => no changes.*/
	private int changeFlag;
	
	/**Shows whether there are any unsaved changes*/
	private boolean saved;
	

	/**File reference to the current opened file, null if none is opened*/
	private File file;

	protected JPanel panelHeader;
	protected JButton buttonNewfile;
	protected JButton buttonSavefile;
	protected JButton buttonSavefileAs;
	protected JButton buttonOpenfile;
	protected JLabel labelFiletitle;
	protected JButton buttonClosefile;

	protected JPanel panelMain;
	protected JScrollPane scrollPane;
	
	public EditorPanel() {
		
		
		setFocusable(true);
		setBackground(ScyllaGUI.ColorBackground);
		setLayout(new GridLayout(0, 1, 0, 0));
		
		//---Header panel---
		panelHeader = new JPanel();
		panelHeader.setFocusable(true);
		panelHeader.setBackground(Color.DARK_GRAY);
		GridBagLayout gbl_panelHeader = new GridBagLayout();
		panelHeader.setLayout(gbl_panelHeader);
		
		// Button "New file"
		buttonNewfile = new JButton();
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
		buttonOpenfile = new JButton();
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
		
		//--- Main Panel ---
		panelMain = new JPanel();
		panelMain.setFocusable(true);
		panelMain.setBackground(ScyllaGUI.ColorBackground);
		GridBagLayout gbl_panelMain = new GridBagLayout();
		panelMain.setLayout(gbl_panelMain);
		
		//---- Root Scrollpane ----
		scrollPane = new JScrollPane();
		scrollPane.getVerticalScrollBar().setUnitIncrement(32);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		add(scrollPane);
		
		scrollPane.setColumnHeaderView(panelHeader);
		scrollPane.setViewportView(panelMain);
		
		//Add enter traversal for textfields etc.
		Set<AWTKeyStroke> forwardKeys = new HashSet<AWTKeyStroke>(getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
		forwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
		setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, forwardKeys);
    		
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
				if(parent.getSelectedComponent() != EditorPanel.this)return;
				be_save();
			}
		});
		parent.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_S,InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK),"saveAs");
		parent.getActionMap().put("saveAs", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(parent.getSelectedComponent() != EditorPanel.this)return;
				be_saveAs();
			}
		});
		parent.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_O,InputEvent.CTRL_DOWN_MASK),"open");
		parent.getActionMap().put("open", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(parent.getSelectedComponent() != EditorPanel.this)return;
				be_open();
			}
		});
		parent.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_N,InputEvent.CTRL_DOWN_MASK),"new");
		parent.getActionMap().put("new", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(parent.getSelectedComponent() != EditorPanel.this)return;
				be_create();
			}
		});
	}

	/**
	 * Button event for creating new files
	 */
	protected void be_create(){
		//Show unsaved changes dialog; if cancel is pressed the whole process is canceled
		if(!isSaved()){
			int i = showUnsavedChangesDialog();
			if(i != 1)return;
		}
		create();
	}
	
	/**
	 * Button event for save button,
	 * overrides file if already existing, otherwise opens "save as" dialog
	 */
	protected void be_save(){
		if(isSaved())return;
		if(getFile() != null)save();
		else be_saveAs();
	}
	
	/**
	 * Button event for "save as" button
	 */
	protected void be_saveAs(){
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
		if(getFile() != null)chooser.setSelectedFile(getFile());
		else if(!getId().equals("")) chooser.setSelectedFile(new File(ScyllaGUI.DEFAULTFILEPATH+"\\"+getId()+".xml"));
		chooser.setDialogTitle("Save");
		int c = chooser.showDialog(null,"Save");
		if(c == ScalingFileChooser.APPROVE_OPTION){
			setFile(chooser.getSelectedFile());
			if(getFile() != null){
				ScyllaGUI.DEFAULTFILEPATH = chooser.getSelectedFile().getPath();
				save();
			}else{
				System.err.println("Could not find file");//TODO
			}
		}
	}
	
	/**
	 * Button event for opening existing gcs
	 */
	protected void be_open(){	
		//Show unsaved changes dialog; if cancel is pressed the whole process is canceled
		if(!isSaved()){
			int i = showUnsavedChangesDialog();
			if(i != 1)return;
		}
		//Choose file to be opened
		ScalingFileChooser chooser = new ScalingFileChooser(ScyllaGUI.DEFAULTFILEPATH);
		chooser.setDialogTitle("Open");
		int c = chooser.showDialog(this,"Open");
		//if the process is canceled, nothing happens
		if(c == ScalingFileChooser.APPROVE_OPTION){
			setFile(chooser.getSelectedFile());
			if(getFile() != null){
				//Close current opened file
				close();
				//Update default file path
				ScyllaGUI.DEFAULTFILEPATH = chooser.getSelectedFile().getPath();
				try {
					open();
				} catch (JDOMException | IOException e) {
					e.printStackTrace();
				}
			}else{
				System.err.println("Could not find file");//TODO
			}
		}
	}	
	
	/**
	 * Button event close
	 * Closes gc but asks to save unsaved changes if existing
	 */
	protected void be_close(){
		if(!isSaved()){
			int i = showUnsavedChangesDialog();
			//If cancel was pressed OR the gc was saved, it is closed
			if(i == 1)close();
			//else return;
		}else close();
	}
	
	//Actions
	protected abstract void create();
	protected abstract void save();
	protected abstract void open() throws JDOMException, IOException;
	protected abstract void close();


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
	
	public boolean isSaved(){
		return saved;
	}
	
	@Override
	public void setSaved(boolean b){
		saved = b;
		//Also set the save button to display if saved or not
		buttonSavefile.setEnabled(!b);
	}
	
	/**
	 * Shows a dialog to save the current gc
	 * @return
	 * 	-1 : if canceled by closing the dialog OR if save changes is pressed and canceled
	 * 	1 : if discard changes is pressed OR if save changes is pressed and it is saved
	 * 	2 : if canceled by pressing cancel
	 * 
	 */
	protected int showUnsavedChangesDialog(){
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
		if(isSaved())i = 1;
		return i;
	}
	
	/**
	 * @return The name of the current opened gc, if already a file exists, the filename is taken
	 * !File Name, not file Path!
	 */
	private String getFileName(){
		if(getFile() != null)return getFile().getName();
		else return labelFiletitle.getText();
	}

	protected File getFile() {
		return file;
	}
	
	protected abstract String getId();

	protected void setFile(File file) {
		if(file != null)labelFiletitle.setText(file.getPath());
		else labelFiletitle.setText("<No editor opened. Open an existing file or create a new one.>");
		this.file = file;
	}
	
	/**
	 * Enables or disables all User input components
	 */
	@Override
	public void setEnabled(boolean b){
		buttonClosefile.setEnabled(b);
		buttonSavefileAs.setEnabled(b);
	}

}
