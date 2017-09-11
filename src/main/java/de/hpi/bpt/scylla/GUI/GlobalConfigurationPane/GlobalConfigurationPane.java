package de.hpi.bpt.scylla.GUI.GlobalConfigurationPane;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.text.NumberFormat;
import java.time.ZoneId;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import de.hpi.bpt.scylla.GUI.ExpandPanel;
import de.hpi.bpt.scylla.GUI.ListChooserPanel;
import de.hpi.bpt.scylla.GUI.ScyllaGUI;
import de.hpi.bpt.scylla.creation.GlobalConfiguration.GlobalConfigurationCreator;
import de.hpi.bpt.scylla.GUI.ListChooserPanel.ComponentHolder;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * 
 * @author Leon Bein
 *
 */
@SuppressWarnings("serial")
public class GlobalConfigurationPane extends JPanel {
	private JLabel labelFiletitle;
	//private JLabel labelId;
	private JTextField textfieldId;
	//private JLabel labelSeed;
	private JFormattedTextField textfieldSeed;
	//private JLabel labelTimezone;
	private JComboBox<ZoneId> comboboxTimezone;

	private ListChooserPanel panelTimetables;
	private ListChooserPanel panelResources;
	
	private GlobalConfigurationCreator creator;
	private boolean saved;

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
		
		JButton buttonSavefile = new JButton();
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
		GridBagConstraints gbc_textfieldFiletitle = new GridBagConstraints();
		gbc_textfieldFiletitle.weightx = 37;
		gbc_textfieldFiletitle.fill = GridBagConstraints.BOTH;
		panelHeader.add(labelFiletitle, gbc_textfieldFiletitle);
		
		JButton buttonClosefile = new JButton();
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
		
		textfieldSeed = new JFormattedTextField(NumberFormat.getNumberInstance());
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
		
		comboboxTimezone = new JComboBox<ZoneId>();
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
				System.out.println("Deleted "+toDel);//TODO
			}

			@Override
			public ComponentHolder onCreate() {
				// TODO Auto-generated method stub
				return 	new ListChooserPanel.ComponentHolder() {
					ResourcePanel p = new ResourcePanel();
					String name = "Resource #"+(int)(Math.random()*10000);
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
				};
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
		
		panelResources.add(						
				new ListChooserPanel.ComponentHolder() {
					ResourcePanel p = new ResourcePanel();
					String name = "A Resource";
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
				}
		);
		
		panelResources.add(						
				new ListChooserPanel.ComponentHolder() {
					ResourcePanel p = new ResourcePanel();
					String name = "Another Resource";
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
				}
		);
		
		
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
		
	}
	
	private void be_create(){
		
	}
	private void createGC(){
		
	}

	private void be_open(){
		
	}	
	private void openGC(){
		
	}

	
	private void be_save(){
		
	}
	private void saveGC(){
		
	}
	
	private void be_close(){
		if(!saved){
			int i = JOptionPane.showOptionDialog(
					this,
					"has unsaved changes. Would you like to save them?",
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
								closeGC();
							}
						});
					}},"Discard Changes","Cancel"}, 0);
			if(i != 1)return;
		}
		closeGC();
	}
	private void closeGC(){
		creator = null;
		labelFiletitle.setText("");
		textfieldId.setText("");
		textfieldSeed.setValue(null);
		
		panelResources.clear();
		panelTimetables.clear();
	}
	
	
}