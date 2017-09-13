package de.hpi.bpt.scylla.GUI.GlobalConfigurationPane;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.DocumentEvent;

import org.jdom2.JDOMException;

import de.hpi.bpt.scylla.GUI.ExpandPanel;
import de.hpi.bpt.scylla.GUI.FormulaManager;
import de.hpi.bpt.scylla.GUI.InsertRemoveListener;
import de.hpi.bpt.scylla.GUI.ListChooserPanel;
import de.hpi.bpt.scylla.GUI.ListChooserPanel.ComponentHolder;
import de.hpi.bpt.scylla.GUI.ScalingFileChooser;
import de.hpi.bpt.scylla.GUI.ScyllaGUI;
import de.hpi.bpt.scylla.creation.GlobalConfiguration.GlobalConfigurationCreator;
import de.hpi.bpt.scylla.creation.GlobalConfiguration.GlobalConfigurationCreator.ResourceType;

/**
 * 
 * @author Leon Bein
 *
 */
@SuppressWarnings("serial")
public class GlobalConfigurationPane extends JPanel implements FormulaManager{
	private JLabel labelFiletitle;
	private JTextField textfieldId;
	private JFormattedTextField textfieldSeed;
	private JComboBox<ZoneOffset> comboboxTimezone;

	private ListChooserPanel panelTimetables;
	private List<ListModel<String>> timetableObserverList;
	private ListChooserPanel panelResources;
	
	private GlobalConfigurationCreator creator;
	private File file;
	private boolean saved;
	private JButton buttonSavefile;
	private JButton buttonClosefile;
	private int changeFlag;

	/**
	 * Create the panel.
	 */
	public GlobalConfigurationPane() {
		setBackground(ScyllaGUI.ColorBackground);
		setLayout(new GridLayout(0, 1, 0, 0));
		
		JPanel panelHeader = new JPanel();
		panelHeader.setBackground(Color.DARK_GRAY);
		GridBagLayout gbl_panelHeader = new GridBagLayout();
		panelHeader.setLayout(gbl_panelHeader);
		
		JButton buttonNewfile = new JButton();
		buttonNewfile.setToolTipText("New File");
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
		
		buttonSavefile = new JButton();
		buttonSavefile.setToolTipText("Save");
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
		
		JButton buttonOpenfile = new JButton();
		buttonOpenfile.setToolTipText("Open");
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
		
		labelFiletitle = new JLabel();
		labelFiletitle.setForeground(Color.WHITE);
		GridBagConstraints gbc_textfieldFiletitle = new GridBagConstraints();
		gbc_textfieldFiletitle.weightx = 37;
		gbc_textfieldFiletitle.insets = new Insets(0,ScyllaGUI.TITLEFONT.getSize(),  0, 0);
		gbc_textfieldFiletitle.fill = GridBagConstraints.BOTH;
		panelHeader.add(labelFiletitle, gbc_textfieldFiletitle);
		
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
		

		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		add(scrollPane);
		
		scrollPane.setColumnHeaderView(panelHeader);

		JPanel panelMain = new JPanel();
		panelMain.setBackground(new Color(200, 20, 20));
		scrollPane.setViewportView(panelMain);
		GridBagLayout gbl_panelMain = new GridBagLayout();
		panelMain.setLayout(gbl_panelMain);
		
		JPanel panelGeneral = new JPanel();
		GridBagConstraints gbc_panelGeneral = new GridBagConstraints();
		gbc_panelGeneral.anchor = GridBagConstraints.PAGE_START;
		int inset_b = 25;//(int)(25.0*ScyllaGUI.SCALE);
		int inset_s = 5;//(int)(2.5*ScyllaGUI.SCALE);TODO
		gbc_panelGeneral.insets = new Insets(inset_b,inset_b,inset_b,inset_b);
		gbc_panelGeneral.gridx = 0;
		gbc_panelGeneral.gridy = 0;
		gbc_panelGeneral.fill = GridBagConstraints.HORIZONTAL;
		gbc_panelGeneral.weightx = 1;
		panelMain.add(panelGeneral, gbc_panelGeneral);
		GridBagLayout gbl_panelGeneral = new GridBagLayout();
		gbl_panelGeneral.columnWeights = new double[]{1.0,3.0};
		panelGeneral.setLayout(gbl_panelGeneral);
		
		JLabel labelId = new JLabel();
		labelId.setText("ID");
		GridBagConstraints gbc_textfieldId = new GridBagConstraints();
		gbc_textfieldId.insets = new Insets(inset_s, inset_s, inset_s, inset_s);
		gbc_textfieldId.fill = GridBagConstraints.HORIZONTAL;
		gbc_textfieldId.gridx = 0;
		gbc_textfieldId.gridy = 0;
		panelGeneral.add(labelId, gbc_textfieldId);
		
		textfieldId = new JTextField();
		textfieldId.getDocument().addDocumentListener(new InsertRemoveListener((DocumentEvent e)->{
			if(isChangeFlag())return;
			creator.setId(textfieldId.getText());
			setSaved(false);
		}));
		GridBagConstraints gbc_textfieldIdEdit = new GridBagConstraints();
		gbc_textfieldIdEdit.insets = new Insets(inset_s, inset_s, inset_s, inset_b);
		gbc_textfieldIdEdit.fill = GridBagConstraints.HORIZONTAL;
		gbc_textfieldIdEdit.gridx = 1;
		gbc_textfieldIdEdit.gridy = 0;
		panelGeneral.add(textfieldId, gbc_textfieldIdEdit);
		textfieldId.setColumns(10);
		
		JLabel labelSeed = new JLabel();
		labelSeed.setText("Seed");
		GridBagConstraints gbc_textfieldSeed = new GridBagConstraints();
		gbc_textfieldSeed.insets = new Insets(inset_s, inset_s, inset_s, inset_s);
		gbc_textfieldSeed.fill = GridBagConstraints.HORIZONTAL;
		gbc_textfieldSeed.gridx = 0;
		gbc_textfieldSeed.gridy = 1;
		panelGeneral.add(labelSeed, gbc_textfieldSeed);
		
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
		gbc_textfieldSeedEdit.insets = new Insets(inset_s, inset_s, inset_s, inset_b);
		gbc_textfieldSeedEdit.fill = GridBagConstraints.HORIZONTAL;
		gbc_textfieldSeedEdit.gridx = 1;
		gbc_textfieldSeedEdit.gridy = 1;
		panelGeneral.add(textfieldSeed, gbc_textfieldSeedEdit);
		textfieldSeed.setColumns(10);
		
		JLabel labelTimezone = new JLabel();
		labelTimezone.setText("Timezone");
		GridBagConstraints gbc_textfieldTimezone = new GridBagConstraints();
		gbc_textfieldTimezone.insets = new Insets(inset_s, inset_s, inset_s, inset_s);
		gbc_textfieldTimezone.fill = GridBagConstraints.HORIZONTAL;
		gbc_textfieldTimezone.gridx = 0;
		gbc_textfieldTimezone.gridy = 2;
		panelGeneral.add(labelTimezone, gbc_textfieldTimezone);
		
		ZoneOffset[] timeZones = new ZoneOffset[25];
		for(int i = -12; i <= 12; i++){
			timeZones[i+12] = ZoneOffset.ofHours(i);
		}
		comboboxTimezone = new JComboBox<ZoneOffset>(timeZones);
		comboboxTimezone.addItemListener((ItemEvent e)->{
			if(e.getStateChange() == ItemEvent.SELECTED){
				if(isChangeFlag())return;
				creator.setTimeOffset((ZoneOffset)comboboxTimezone.getSelectedItem());
				setSaved(false);
			}
		});
		GridBagConstraints gbc_comboboxTimezone = new GridBagConstraints();
		gbc_comboboxTimezone.insets = new Insets(inset_s, inset_s, inset_s, inset_b);
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

		ExpandPanel panelResourcesExpand = new ExpandPanel(new JLabel("Resources"), panelResources);
		panelResourcesExpand.expand();
		panelMain.add(panelResourcesExpand, gbc_panelResources);
		
		
		timetableObserverList = new ArrayList<ListModel<String>>();
		panelTimetables = new ListChooserPanel(){

			@Override
			public void onDelete(ComponentHolder toDel) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public ComponentHolder onCreate() {
				// TODO Auto-generated method stub
				return null;
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
		
		ExpandPanel panelTimetablesExpand = new ExpandPanel(new JLabel("Timetables"), panelTimetables);
		panelTimetablesExpand.expand();
		panelMain.add(panelTimetablesExpand, gbc_panelTimetables);
		
		panelTimetables.add(new ListChooserPanel.ComponentHolder() {
					TimetablePanel p = new TimetablePanel();
					String name = "A Timetable";
					@Override
					public Component getComponent() {
						return p;
					}
					@Override
					public String toString(){
						return name;
					}
					@Override
					public void setName(String s){
						name = s;
					}
				});
		
		JPanel panelBuffer = new JPanel();
		panelBuffer.setBackground(panelMain.getBackground());
		GridBagConstraints gbc_panelBuffer = new GridBagConstraints();
		gbc_panelBuffer.anchor = GridBagConstraints.PAGE_START;
		gbc_panelBuffer.fill = GridBagConstraints.BOTH;
		gbc_panelBuffer.weighty = 100;
		gbc_panelBuffer.weightx = 1;
		gbc_panelBuffer.gridx = 0;
		gbc_panelBuffer.gridy = 3;
		panelMain.add(panelBuffer,gbc_panelBuffer);
		
		setEnabled(false);
	}
	
	private void be_create(){
		setChangeFlag(true);
		labelFiletitle.setText("<unsaved file>");
		createGC();
		setChangeFlag(false);
		textfieldId.setText("NewGlobalConfiguration");
		textfieldId.requestFocusInWindow();
		textfieldId.selectAll();
	}
	
	private void createGC(){
		creator = new GlobalConfigurationCreator();
		setSaved(false);
		setEnabled(true);
	}

	private void be_open(){
		be_close();
		ScalingFileChooser chooser = new ScalingFileChooser(ScyllaGUI.DEFAULTFILEPATH);
		chooser.setDialogTitle("Open");
		int c = chooser.showDialog(this,"Open");
		if(c == ScalingFileChooser.APPROVE_OPTION){
			file = chooser.getSelectedFile();
			if(file != null){
				ScyllaGUI.DEFAULTFILEPATH = chooser.getSelectedFile().getPath();
				try {
					creator = GlobalConfigurationCreator.createFromFile(file.getPath());
					labelFiletitle.setText(chooser.getSelectedFile().getPath());
				} catch (JDOMException | IOException e) {
					e.printStackTrace();
				}
				openGC();
			}else{
				System.err.println("Could not find file");//TODO
			}
		}
		
	}	
	
	private void openGC(){
		//TODO
		setChangeFlag(true);
		textfieldId.setText(creator.getId());
		textfieldSeed.setValue(creator.getSeed());
		comboboxTimezone.setSelectedItem(creator.getTimeOffset());
		
		for(ResourceType res : creator.getResourceTypes()){
			importResource(res);
		}
		
		setChangeFlag(false);
		setEnabled(true);
	}

	
	private void be_save(){
		ScalingFileChooser chooser = new ScalingFileChooser(ScyllaGUI.DEFAULTFILEPATH);
		if(file != null)chooser.setSelectedFile(file);
		else if(!creator.getId().equals("")) chooser.setSelectedFile(new File(ScyllaGUI.DEFAULTFILEPATH+"\\"+creator.getId()+".xml"));
		chooser.setDialogTitle("Save");
		chooser.setFont(ScyllaGUI.fileChooserFont);
		chooser.setPreferredSize(ScyllaGUI.fileChooserDimension);
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
	
	private void saveGC(){
		try {
			setSaved(true);
			creator.save(file.getPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void be_close(){
		if(!saved){
			int i = showUnsavedChangesDialog();
			if(i == 1)closeGC();
		}
		else closeGC();
	}
	private void closeGC(){
		setChangeFlag(true);
		creator = null;
		labelFiletitle.setText("");
		textfieldId.setText("");
		textfieldSeed.setValue(null);
		comboboxTimezone.setSelectedItem(null);
		
		panelResources.clear();
		panelTimetables.clear();
		setChangeFlag(false);
		setSaved(true);
		setEnabled(false);
	}
	
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
	
	public void setSaved(boolean b){
		saved = b;
		buttonSavefile.setEnabled(!b);
	}
	
	@Override
	public boolean isChangeFlag() {
		return changeFlag > 0;
	}
	
	@Override
	public void setChangeFlag(boolean b) {
		if(b)changeFlag++;
		else changeFlag--;
		System.out.println("Changeflag to: "+changeFlag);
		if(changeFlag < 0)throw new java.lang.NegativeArraySizeException();
	}
	
	@Override
	public List<ListModel<String>> getTimetableObserverList() {
		return timetableObserverList;
	}

	private String getFileName(){
		if(file != null)return file.getName();
		else return labelFiletitle.getText();
	}
	
	@Override
	public void setEnabled(boolean b){
		buttonClosefile.setEnabled(b);
		textfieldId.setEnabled(b);
		textfieldSeed.setEnabled(b);
		comboboxTimezone.setEnabled(b);
		panelTimetables.setEnabled(b);
		panelResources.setEnabled(b);
		super.setEnabled(b);
	}
	
	public void importResource(ResourceType res){
		panelResources.add(
				newResource(res)
		);
	}
	
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
				res.setId(s);
				setSaved(false);
			}
		};
	}

	
	
	
	
}