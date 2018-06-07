package de.hpi.bpt.scylla.GUI.GlobalConfigurationPane;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

import org.eclipse.wb.swing.FocusTraversalOnArray;
import org.jdom2.JDOMException;

import de.hpi.bpt.scylla.GUI.EditorPane;
import de.hpi.bpt.scylla.GUI.ExpandPanel;
import de.hpi.bpt.scylla.GUI.ExtendedListChooserPanel;
import de.hpi.bpt.scylla.GUI.ListChooserPanel.ComponentHolder;
import de.hpi.bpt.scylla.GUI.ScyllaGUI;
import de.hpi.bpt.scylla.GUI.InputFields.NumberField;
import de.hpi.bpt.scylla.GUI.InputFields.SelectionField;
import de.hpi.bpt.scylla.GUI.InputFields.StringField;
import de.hpi.bpt.scylla.GUI.plugin.EditorTabPluggable;
import de.hpi.bpt.scylla.GUI.plugin.InputFieldPluggable;
import de.hpi.bpt.scylla.creation.GlobalConfiguration.GlobalConfigurationCreator;
import de.hpi.bpt.scylla.creation.GlobalConfiguration.GlobalConfigurationCreator.ResourceType;
import de.hpi.bpt.scylla.creation.GlobalConfiguration.GlobalConfigurationCreator.Timetable;

/**
 * 
 * @author Leon Bein
 *
 */
@SuppressWarnings("serial")
public class GlobalConfigurationPane extends EditorPane implements GCFormManager{
	
	//General Information form components
	private StringField textfieldId;
	private NumberField<Long> textfieldSeed;
	private SelectionField<ZoneId> comboboxTimezone;

	/**Timetable Panel*/
	private ExtendedListChooserPanel panelTimetables;
	/**Resource Panel*/
	private ExtendedListChooserPanel panelResources;

	/**List of all JComboboxes, that display timetables, in order to update their entries*/
	private List<SetObserver<String>> timetableObserverList;
	/**List of all known timetable ids, in order to pass them to newly created displays (e.g. Comboboxes)*/
	private List<String> timetables;
	
	/**Central XML-Link object*/
	private GlobalConfigurationCreator creator;
	
	public enum Tabs {GENERAL, RESOURCES, TIMETABLES}


	/**
	 * Create the panel.
	 */
	public GlobalConfigurationPane() {
		
		// -- General Information Panel --
		JPanel panelGeneral = new JPanel();
		panelGeneral.setFocusable(true);
		GridBagConstraints gbc_panelGeneral = new GridBagConstraints();
		gbc_panelGeneral.anchor = GridBagConstraints.PAGE_START;
		gbc_panelGeneral.insets = new Insets(INSET_B,INSET_B,INSET_B,INSET_B);
		gbc_panelGeneral.gridx = 0;
		gbc_panelGeneral.gridy = GridBagConstraints.RELATIVE;
		gbc_panelGeneral.fill = GridBagConstraints.HORIZONTAL;
		gbc_panelGeneral.weightx = 1;
		addTab(Tabs.GENERAL, panelGeneral, gbc_panelGeneral);
		GridBagLayout gbl_panelGeneral = new GridBagLayout();
		gbl_panelGeneral.columnWeights = new double[]{1.0,3.0};
		panelGeneral.setLayout(gbl_panelGeneral);
		
		//Label id
		JLabel labelId = new JLabel("ID");
		GridBagConstraints gbc_labelId = createInputLabelConstraints();
		panelGeneral.add(labelId, gbc_labelId);
		
		//Text input field for id
//		textfieldId = new JTextField();
//		textfieldId.getDocument().addDocumentListener(new InsertRemoveListener((DocumentEvent e)->{
//			if(isChangeFlag())return;
//			creator.setId(textfieldId.getText());
//			setSaved(false);
//		}));
		textfieldId = new StringField(this) {
			protected String getSavedValue() {return creator != null ? creator.getId() : null;}
			protected void setSavedValue(String v) {creator.setId(v);}
		};
		GridBagConstraints gbc_textfieldId = createInputFieldConstraints();
		panelGeneral.add(textfieldId.getComponent(), gbc_textfieldId);
//		textfieldId.setColumns(10);
		
		//Label seed
		JLabel labelSeed = new JLabel("Seed");
		GridBagConstraints gbc_textfieldSeed = createInputLabelConstraints();
		panelGeneral.add(labelSeed, gbc_textfieldSeed);
		
		//Seed input field
//		NumberFormat format = NumberFormat.getInstance();
//		format.setGroupingUsed(false);
//		textfieldSeed = new JFormattedTextField(format);
//		textfieldSeed.getDocument().addDocumentListener(new InsertRemoveListener((DocumentEvent e)->{
//			if(isChangeFlag())return;
//			try{
//				long s = creator.getSeed();
//				long n = Long.parseLong(textfieldSeed.getText());
//				if(s != n){
//					creator.setSeed(n);
//					setSaved(false);
//				}
//			}catch(Exception exc){}
//		}));
		textfieldSeed = new NumberField<Long>(this) {
			
			@Override
			protected void setSavedValue(Long v) {
				if(creator != null)creator.setSeed(v);
			}
			
			@Override
			protected Long getSavedValue() {
				return creator != null ? creator.getSeed() : null;
			}
		};
		
		GridBagConstraints gbc_textfieldSeedEdit = createInputFieldConstraints();
		panelGeneral.add(textfieldSeed.getComponent(), gbc_textfieldSeedEdit);
		
		//Label timezone
		JLabel labelTimezone = new JLabel("Timezone");
		GridBagConstraints gbc_textfieldTimezone = createInputLabelConstraints();
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
		comboboxTimezone = new SelectionField<ZoneId>(this,timeZones) {

			@Override
			protected ZoneId getSavedValue() {
				if(creator == null || creator.getTimeOffset() == null)return null;
				return ZoneId.ofOffset("UTC",creator.getTimeOffset());
			}

			@Override
			protected void setSavedValue(ZoneId v) {
				creator.setTimeOffset(v.getRules().getStandardOffset(null));
			}
			
		};
		comboboxTimezone.getComponent().setRenderer(new DefaultListCellRenderer(){
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

		GridBagConstraints gbc_comboboxTimezone = createInputFieldConstraints();
		panelGeneral.add(comboboxTimezone.getComponent(), gbc_comboboxTimezone);
		
		//---Resource Panel---
		panelResources = new ExtendedListChooserPanel(){

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
		//Resource title label
		ExpandPanel panelResourcesExpand = addTab(Tabs.RESOURCES, "Resources", panelResources);
		panelResourcesExpand.expand();
		

		//Timetable initialization
		timetableObserverList = new ArrayList<SetObserver<String>>();
		timetables = new ArrayList<String>();
		timetables.add("");//"No timetable"-option
		
		//--- Timetable panel ---
		panelTimetables = new ExtendedListChooserPanel(){

			@Override
			public void onDelete(ComponentHolder toDel) {
				String toDels = toDel.toString();

				for(SetObserver<String> obs : getTimetableObserverList()) {
					obs.notifyDeletion(toDels);
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
		
		//Timetable title label
		ExpandPanel panelTimetablesExpand = addTab(Tabs.TIMETABLES, "Timetables", panelTimetables);
		panelTimetablesExpand.expand();
		
		EditorTabPluggable.runPlugins(this);
		InputFieldPluggable.runPlugins(this);
		
		//Layout fixing empty buffer panel
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
		addTab(null, panelBuffer,gbc_panelBuffer);
		
		//Disable, as no gc is opened
		setEnabled(false);
		
		setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{buttonNewfile, buttonSavefile, buttonSavefileAs, buttonOpenfile, buttonClosefile, textfieldId.getComponent(), textfieldSeed.getComponent(), comboboxTimezone.getComponent(), panelResourcesExpand, panelTimetablesExpand}));
		
	}
	
	protected String getId(){
		return creator.getId();
	}
	
	/**
	 * Creates a new global configuration
	 */
	@Override
	protected void create(){
		setChangeFlag(true);
		close();
		setFile(new File("NewFile"+unnamedcount++ + ".xml"));
		creator = new GlobalConfigurationCreator();
		setSaved(false);
		setEnabled(true);
		setChangeFlag(false);
		//Id should be edited right after creation
		textfieldId.setValue("NewGlobalConfiguration");
		textfieldId.getComponent().requestFocusInWindow();
		textfieldId.getComponent().selectAll();
	}

	
	/**
	 * Opens the gc that is currently loaded in the creator
	 * @throws IOException 
	 * @throws JDOMException 
	 */
	@Override
	protected void open() throws JDOMException, IOException{
		creator = GlobalConfigurationCreator.createFromFile(getFile().getPath());
		setChangeFlag(true);
		textfieldId.loadSavedValue();
		textfieldSeed.loadSavedValue();
		comboboxTimezone.loadSavedValue();
//		if(creator.getTimeOffset() != null)comboboxTimezone.setSelectedItem(ZoneId.ofOffset("UTC",creator.getTimeOffset()));
		
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
	 * Saves the gc to the current file
	 */
	@Override
	protected void save(){
		try {
			setSaved(true);
			creator.save(getFile().getPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Closes the gc, resets, clears and disables all fields
	 */
	@Override
	protected void close(){
		setChangeFlag(true);
		creator = null;
		setFile(null);
		textfieldId.reset();
		textfieldSeed.clear();
		comboboxTimezone.clear();
		
		panelResources.clear();
		panelTimetables.clear();
		timetableObserverList.clear();
		timetables.clear();
		timetables.add("");
		setChangeFlag(false);
		setSaved(true);
		setEnabled(false);
	}
	

	@Override
	public List<SetObserver<String>> getTimetableObserverList() {
		return timetableObserverList;
	}

	@Override
	public List<String> getTimetables() {
		return timetables;
	}
	

	/**
	 * Enables or disables all User input components
	 */
	@Override
	public void setEnabled(boolean b){
		textfieldId.getComponent().setEnabled(b);
		textfieldSeed.getComponent().setEnabled(b);
		comboboxTimezone.getComponent().setEnabled(b);
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
		return new ExtendedListChooserPanel.ComponentHolder() {
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
		for(SetObserver<String> obs : getTimetableObserverList()) {
			obs.notifyCreation(t.getId());
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
		return new ExtendedListChooserPanel.ComponentHolder() {
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
				String newId = n;
				//If the name has changed, but is already given,
				//it will be serially numbered, and the first free number is chosen
				int i = 2;
				if(!t.getId().equals(n)){
					while(creator.getTimetable(newId) != null){
						newId = n+"("+i+")";
						i++;
					}
				}
				//Notify timetable observer
				for(SetObserver<String> obs : getTimetableObserverList()) {
					obs.notifyRenaming(t.getId(), newId);
				}
				//Update timetable list
				timetables.remove(t.getId());
				timetables.add(newId);
				//Set id
				t.setId(newId);
				setSaved(false);
			}
		};
	}

	
	
	
	
}