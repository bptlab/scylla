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
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Observer;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;

import org.jdom2.JDOMException;

import static de.hpi.bpt.scylla.Scylla.*;



@SuppressWarnings("serial")
/**
 * Abstract superclass for all kinds of file editing panes.
 * Provides creation, saving, opening and closing buttons and functionalities.
 * Provides a saving mechanism
 * @author Leon Bein
 *
 */
public abstract class EditorPane extends JPanel implements FormManager{
	
	/**Flag to display if there are any non-user changes performed at user input objects,
	 * in order to prevent user input events to be fired.
	 * Integer value represents the number of methods/threads that are currently performing those changes,
	 * if equal 0 => no changes.*/
	private int changeFlag;
	
	/**Shows whether there are any unsaved changes*/
	private boolean saved;
	

	/**File reference to the current opened file, null if none is opened*/
	private File file;
	
	/**Counts, how many unnamed file have been created in this program instance, in order to number them*/
	protected static int unnamedcount = 0;

	/**Header panel/button bar, accessible for subclasses*/
	protected JPanel panelHeader;
	/**Button for creating new files, accessible for subclasses*/
	protected JButton buttonNewfile;
	/**Button for override saving files, accessible for subclasses*/
	protected JButton buttonSavefile;
	/**Button for saving file with new name, accessible for subclasses*/
	protected JButton buttonSavefileAs;
	/**Button to open a new file, accessible for subclasses*/
	protected JButton buttonOpenfile;
	/**Label displaying the file title or any other title, accessible for subclasses*/
	protected JLabel labelFiletitle;
	/**Button to close the current file and clear the editor*/
	protected JButton buttonClosefile;

	/**Main editor panel, layout and content under the responsibility of subclasses*/
	protected JPanel panelMain;
	/**Main panel wrapping scrollpane, accessible for subclasses*/
	protected JScrollPane scrollPane;
	
	/**Observers for file title changes*/
	protected Set<Observer> titleObservers;
	
	/**
	 * Plain constructor, mainly initializes the header bar, keybindings etc.
	 */
	public EditorPane() {
		
		setFocusable(true);
		setBackground(ScyllaGUI.ColorBackground);
		setLayout(new GridLayout(0, 1, 0, 0));
		
		titleObservers = new HashSet<Observer>() {
			public boolean add(Observer o) {	
				boolean b = super.add(o);
				notifyTitleObservers(getFile());
				return b;
			}
		};
		
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
		labelFiletitle = new JLabel() {
			@Override
			/**
			 * Always resets color and font when text is changed, to enable easy error messaging
			 */
			public void setText(String text) {
				setForeground(Color.WHITE);
				setFont(ScyllaGUI.TITLEFONT);
				super.setText(text);
			}
		};
		showNoEditorLabel();
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
		buttonClosefile.setIcon(ScyllaGUI.ICON_CLOSE);
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
		initKeyBindings();
	}
	
	
	
	/**
	 * Initializes keybindings for save(as), open and new
	 */
	public void initKeyBindings(){
		getActionMap().put(ScyllaGUI.ACTIONKEY_SAVE, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				be_save();
			}
		});
		getActionMap().put(ScyllaGUI.ACTIONKEY_SAVEAS, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				be_saveAs();
			}
		});
		getActionMap().put(ScyllaGUI.ACTIONKEY_OPEN, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				be_open();
			}
		});
		getActionMap().put(ScyllaGUI.ACTIONKEY_NEW, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				be_create();
			}
		});
	}

	/**
	 * Button event for creating new files
	 */
	public void be_create(){
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
	public void be_save(){
		if(isSaved())return;
		if(getFile() != null && getFile().exists())save();
		else be_saveAs();
	}
	
	/**
	 * Button event for "save as" button
	 */
	public void be_saveAs(){
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
		if(getFile() != null && getFile().exists())chooser.setSelectedFile(getFile());
		else if(!getId().equals("")) chooser.setSelectedFile(new File(normalizePath(ScyllaGUI.DEFAULTFILEPATH+"/"+getId()+".xml")));
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
	public void be_open(){	
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
			if(chooser.getSelectedFile() != null){
				//Close current opened file
				close();
				//Update default file path
				setFile(chooser.getSelectedFile());
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
	public void be_close(){
	//If discard changes was pressed OR the gc was saved, it is closed
		if(isSaved() || showUnsavedChangesDialog() == 1) {
			close();
			notifyTitleObservers(this);
		}
	}
	
	//Actions
	protected abstract void create();
	protected abstract void save();
	protected abstract void open() throws JDOMException, IOException;
	protected abstract void close();
	
	/**
	 * Directly opens a file for editing without usr input.
	 * @param f: A file matching the specific sub-class
	 */
	public void openFile(File f) {
		if(f == null)return;
		try {
			setFile(f);
			open();
		} catch (JDOMException | IOException e) {
			e.printStackTrace();
		}
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
	
	public boolean isSaved(){
		return saved;
	}
	
	@Override
	public void setSaved(boolean b){
		saved = b;
		//Also set the save button to display if saved or not
		buttonSavefile.setEnabled(!b);
		notifyTitleObservers(getFile());
	}
	
	public Set<Observer> getTitleObservers() {
		return titleObservers;
	}
	
	protected void notifyTitleObservers(Object arg) {
		for(Observer o : titleObservers) {
			o.update(null, arg);
		}
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
				getFileName()+" has unsaved changes. Would you like to save them?",
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

	/**
	 * Sets the edited file, changes title labels etc. 
	 * @param file
	 */
	protected void setFile(File file) {
		if(file != null)labelFiletitle.setText(file.getPath());
		else showNoEditorLabel();
		this.file = file;	
		notifyTitleObservers(getFile());
	}
	
	/**
	 * Enables or disables all User input components
	 */
	@Override
	public void setEnabled(boolean b){
		buttonClosefile.setEnabled(b);
		buttonSavefileAs.setEnabled(b);
	}
	
	/**
	 * Displays a message to indicate, that there is currently no file edited
	 */
	protected void showNoEditorLabel() {
		labelFiletitle.setText("<no file>");
	}

}
