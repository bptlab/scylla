package de.hpi.bpt.scylla.GUI.GlobalConfigurationPane;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import de.hpi.bpt.scylla.GUI.InsertRemoveListener;
import de.hpi.bpt.scylla.GUI.ScyllaGUI;
import de.hpi.bpt.scylla.creation.GlobalConfiguration.GlobalConfigurationCreator.Timetable;
import de.hpi.bpt.scylla.creation.GlobalConfiguration.GlobalConfigurationCreator.Timetable.TimetableItem;

/**
 * Display class for timetables
 * @author Leon Bein
 *
 */
@SuppressWarnings("serial")
public class TimetablePanel extends JSplitPane {
	/**Main timetable table*/
	private JTable tableTime;
	/**Column for row times*/
	private JTable tableRowHeader;
	/**Selection model of {@link TimetablePanel#tableTime}*/
	private TimetableSelectionModel selectionModel;

	//Item editing user input components
	private JFormattedTextField textfieldStartTime;
	private JFormattedTextField textfieldEndTime;
	private JComboBox<DayOfWeek> comboboxStartDay;
	private JComboBox<DayOfWeek> comboboxEndDay;
	private JButton buttonDelete;
	
	//Item editing labels (for enableing and disableing)
	private JLabel labelItemTitle;
	private JLabel labelStart;
	private JLabel labelStartAt;
	private JLabel labelEnd;
	private JLabel labelEndAt;

	/**The form manager*/
	private GCFormManager formManager;
	/**The current selected item*/
	private TimetableItem selected;
	/**Wrapper for the corresponding timetable*/
	private Timetable timeTable;
	
	/**
	 * Create the panel.
	 * @param globalConfigurationPane 
	 */
	public TimetablePanel(GCFormManager fm) {
		formManager = fm;
		setOrientation(JSplitPane.VERTICAL_SPLIT);
		setEnabled(false);
		
		//---Timetable panel---
		JPanel panelTop = new JPanel();
		setLeftComponent(panelTop);
		Object[][] times = new Object[24][1];
		for(int i = 0; i < times.length; i++){
			times[i] = new Object[]{LocalTime.of(i, 0)};
		}
		GridBagLayout gbl_panelTop = new GridBagLayout();
		panelTop.setLayout(gbl_panelTop);
		
		//Row time colomn
		tableRowHeader = new JTable();
		tableRowHeader.setRowHeight(tableRowHeader.getFont().getSize());
		tableRowHeader.setFont(ScyllaGUI.DEFAULTFONT);
		tableRowHeader.setModel(new DefaultTableModel(times,new String[] {""}));
		tableRowHeader.setRowSelectionAllowed(false);
		tableRowHeader.setEnabled(false);
		GridBagConstraints gbc_tableRowHeader = new GridBagConstraints();
		gbc_tableRowHeader.gridx = 0;
		gbc_tableRowHeader.gridy = 1;
		gbc_tableRowHeader.weightx = 0.1;
		gbc_tableRowHeader.fill = GridBagConstraints.BOTH;
		panelTop.add(tableRowHeader, gbc_tableRowHeader);
		
		//Main table
		tableTime = new JTable();
		tableTime.setColumnSelectionAllowed(true);
		tableTime.setDefaultRenderer(Object.class, new CellIntervalRenderer());
		tableTime.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		tableTime.setRowHeight(tableTime.getFont().getSize());
		tableTime.setModel(new DefaultTableModel(
			new Object[24][7],
			DayOfWeek.values()
		){
			@Override
			public boolean isCellEditable(int y, int x){return false;}
		});
		tableTime.getTableHeader().setReorderingAllowed(false);
		GridBagConstraints gbc_tableTime = new GridBagConstraints();
		gbc_tableTime.gridx = 1;
		gbc_tableTime.gridy = 1;
		gbc_tableTime.weightx = 7;
		gbc_tableTime.fill = GridBagConstraints.BOTH;
		//Table header (weekdays)
		GridBagConstraints gbc_tableTimeHeader = new GridBagConstraints();
		gbc_tableTimeHeader.gridx = 1;
		gbc_tableTimeHeader.gridy = 0;
		gbc_tableTimeHeader.weightx = 7;
		gbc_tableTimeHeader.weighty = 0;
		gbc_tableTimeHeader.fill = GridBagConstraints.HORIZONTAL;
		
		panelTop.add(tableTime.getTableHeader(),gbc_tableTimeHeader);
		panelTop.add(tableTime, gbc_tableTime);
		
		//Selection model
		selectionModel = new TimetableSelectionModel();
		//Catch row selection changes
		tableTime.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				DefaultListSelectionModel s = (DefaultListSelectionModel)e.getSource();
				selectionModel.rowChange(s.getMinSelectionIndex(), s.getMaxSelectionIndex(), s.getValueIsAdjusting());
			}
		});
		//Catch column selection changes
		tableTime.getColumnModel().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				DefaultListSelectionModel s = (DefaultListSelectionModel)e.getSource();
				selectionModel.colChange(s.getMinSelectionIndex(), s.getMaxSelectionIndex(), s.getValueIsAdjusting());
			}
		});
		
		//Unselect current item when right clicked
		tableTime.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e){
				if(SwingUtilities.isRightMouseButton(e)){
					//Point p = e.getPoint();
					//TimetableItem val = (TimetableItem) tableTime.getValueAt(tableTime.rowAtPoint(p), tableTime.columnAtPoint(p));
					select(null);
					tableTime.repaint();
				}
			}
		});
		
		//Delete current item when delete or backspace are pressed
		tableTime.getInputMap().put(KeyStroke.getKeyStroke("DELETE"),"delete");
		tableTime.getInputMap().put(KeyStroke.getKeyStroke("BACK_SPACE"),"delete");
		tableTime.getActionMap().put("delete", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteSelected();
			}
		});
		
		
		//---Bottom Panel ---
		JPanel panelBottom = new JPanel();
		setRightComponent(panelBottom);
		GridBagLayout gbl_panelBottom = new GridBagLayout();
		gbl_panelBottom.columnWeights = new double[]{0.0, 1.0, 0.0, 1.0, 0.0};
		panelBottom.setLayout(gbl_panelBottom);
		
		//Current item title label
		labelItemTitle = new JLabel("Current Item");
		GridBagConstraints gbc_labelItemTitle = new GridBagConstraints();
		gbc_labelItemTitle.insets = new Insets(ScyllaGUI.STDINSET, (int)(ScyllaGUI.SCALE*10.0), (int)(ScyllaGUI.SCALE*30.0), ScyllaGUI.STDINSET);
		gbc_labelItemTitle.anchor = GridBagConstraints.PAGE_START;
		gbc_labelItemTitle.gridx = 0;
		gbc_labelItemTitle.gridy = 0;
		gbc_labelItemTitle.fill = GridBagConstraints.HORIZONTAL;
		gbc_labelItemTitle.gridwidth = 5;
		panelBottom.add(labelItemTitle, gbc_labelItemTitle);
		
		//Start time label 
		labelStart = new JLabel("Start Time");
		GridBagConstraints gbc_labelStart = new GridBagConstraints();
		gbc_labelStart.anchor = GridBagConstraints.WEST;
		gbc_labelStart.insets = new Insets(0, (int)(ScyllaGUI.SCALE*15.0), ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
		gbc_labelStart.gridx = 0;
		gbc_labelStart.gridy = 1;
		gbc_labelStart.weightx = 1;
		panelBottom.add(labelStart, gbc_labelStart);
		
		//Start time input field
		textfieldStartTime = new JFormattedTextField(DateTimeFormatter.ISO_LOCAL_TIME.toFormat());
		textfieldStartTime.getDocument().addDocumentListener(new InsertRemoveListener((DocumentEvent e)->{
			if(formManager.isChangeFlag())return;
			if(selected == null)return;
			try{
				LocalTime l = LocalTime.parse(textfieldStartTime.getText());
				if(!l.equals(selected.getBeginTime())){
					selected.setBeginTime(l);
					//Display changes
					selectionModel.displayItem(selected);
					formManager.setSaved(false);
				}
			}catch(Exception exc){}
		}));
		GridBagConstraints gbc_textfieldTime = new GridBagConstraints();
		gbc_textfieldTime.weightx = 8.0;
		gbc_textfieldTime.insets = new Insets(0, 0, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
		gbc_textfieldTime.fill = GridBagConstraints.HORIZONTAL;
		gbc_textfieldTime.gridx = 1;
		gbc_textfieldTime.gridy = 1;
		panelBottom.add(textfieldStartTime, gbc_textfieldTime);
		
		//Label start time at
		labelStartAt = new JLabel("at");
		GridBagConstraints gbc_labelStartAt = new GridBagConstraints();
		gbc_labelStartAt.weightx = 0.5;
		gbc_labelStartAt.insets = new Insets(0, 0, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
		gbc_labelStartAt.gridx = 2;
		gbc_labelStartAt.gridy = 1;
		panelBottom.add(labelStartAt, gbc_labelStartAt);
		
		//Startday input combobox
		comboboxStartDay = new JComboBox<DayOfWeek>(DayOfWeek.values());
		comboboxStartDay.addItemListener((ItemEvent e)->{
			if(e.getStateChange() == ItemEvent.SELECTED){
				if(formManager.isChangeFlag())return;
				if(selected == null)return;
				selected.setFrom((DayOfWeek) comboboxStartDay.getSelectedItem());
				//Display changes
				selectionModel.displayItem(selected);
				formManager.setSaved(false);
			}
		});
		GridBagConstraints gbc_comboboxStartDay = new GridBagConstraints();
		gbc_comboboxStartDay.weightx = 8.0;
		gbc_comboboxStartDay.insets = new Insets(0, 0, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
		gbc_comboboxStartDay.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboboxStartDay.gridx = 3;
		gbc_comboboxStartDay.gridy = 1;
		panelBottom.add(comboboxStartDay, gbc_comboboxStartDay);
		
		//Delete selected item button
		buttonDelete = new JButton();
		buttonDelete.setIcon(ScyllaGUI.ICON_REMOVE);
		buttonDelete.setToolTipText("Delete selected item (Delete)");
		buttonDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				deleteSelected();
			}
		});
		GridBagConstraints gbc_buttonDelete = new GridBagConstraints();
		gbc_buttonDelete.weightx = 0.5;
		gbc_buttonDelete.gridheight = 2;
		gbc_buttonDelete.insets = new Insets(0, 0, 0, 0);
		gbc_buttonDelete.gridx = 4;
		gbc_buttonDelete.gridy = 1;
		panelBottom.add(buttonDelete, gbc_buttonDelete);
		
		//End time label
		labelEnd = new JLabel("End Time");
		GridBagConstraints gbc_labelEnd = new GridBagConstraints();
		gbc_labelEnd.anchor = GridBagConstraints.WEST;
		gbc_labelEnd.weightx = 1.0;
		gbc_labelEnd.insets = new Insets(0, (int)(ScyllaGUI.SCALE*15.0), ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
		gbc_labelEnd.gridx = 0;
		gbc_labelEnd.gridy = 2;
		panelBottom.add(labelEnd, gbc_labelEnd);
		
		//End time input field
		textfieldEndTime = new JFormattedTextField(DateTimeFormatter.ISO_LOCAL_TIME.toFormat());
		textfieldEndTime.getDocument().addDocumentListener(new InsertRemoveListener((DocumentEvent e)->{
			if(formManager.isChangeFlag())return;
			if(selected == null)return;
			try{
				LocalTime l = LocalTime.parse(textfieldEndTime.getText());
				if(!l.equals(selected.getEndTime())){
					selected.setEndTime(l);
					//Display changes
					selectionModel.displayItem(selected);
					formManager.setSaved(false);
				}
			}catch(Exception exc){}
		}));
		GridBagConstraints gbc_comboBoxEndTime = new GridBagConstraints();
		gbc_comboBoxEndTime.weightx = 8.0;
		gbc_comboBoxEndTime.insets = new Insets(0, 0, 0, 5);
		gbc_comboBoxEndTime.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBoxEndTime.gridx = 1;
		gbc_comboBoxEndTime.gridy = 2;
		panelBottom.add(textfieldEndTime, gbc_comboBoxEndTime);
		
		//Ent time at label
		labelEndAt = new JLabel("at");
		GridBagConstraints gbc_labelEndAt = new GridBagConstraints();
		gbc_labelEndAt.weightx = 0.5;
		gbc_labelEndAt.insets = new Insets(0, 0, 0, ScyllaGUI.STDINSET);
		gbc_labelEndAt.gridx = 2;
		gbc_labelEndAt.gridy = 2;
		panelBottom.add(labelEndAt, gbc_labelEndAt);
		
		//End day input combobox
		comboboxEndDay = new JComboBox<DayOfWeek>(DayOfWeek.values());
		comboboxEndDay.addItemListener((ItemEvent e)->{
			if(e.getStateChange() == ItemEvent.SELECTED){
				if(formManager.isChangeFlag())return;
				if(selected == null)return;
				selected.setTo((DayOfWeek) comboboxEndDay.getSelectedItem());
				//Display changes
				selectionModel.displayItem(selected);
				formManager.setSaved(false);
			}
		});
		GridBagConstraints gbc_comboboxEndDay = new GridBagConstraints();
		gbc_comboboxEndDay.weightx = 8.0;
		gbc_comboboxEndDay.insets = new Insets(0, 0, 0, ScyllaGUI.STDINSET);
		gbc_comboboxEndDay.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboboxEndDay.gridx = 3;
		gbc_comboboxEndDay.gridy = 2;
		panelBottom.add(comboboxEndDay, gbc_comboboxEndDay);
		
		select(null);
	}
	
	/**
	 * Return the value saved for the hour preceding a given time
	 * @param row : Hour of the time
	 * @param col : Day of the time
	 * @return : Value at row:col-1h
	 */
	private Object getPreceding(int row, int col){
		row--;
		if(row < 0){
			row += 24;
			col --;
		}
		if(col < 0){
			col += 7;
		}
		return tableTime.getValueAt(row, col);
	}

	/**
	 * Set a given timetable item as selected, i.e. enabling/disabling and setting the user input fields
	 * @param item : Wrapper of the item to be set
	 */
	public void select(TimetableItem item) {
		selected = item;
		boolean enabled = item != null;
		
		textfieldStartTime.setEnabled(enabled);
		textfieldEndTime.setEnabled(enabled);
		comboboxStartDay.setEnabled(enabled);
		comboboxEndDay.setEnabled(enabled);
		
		labelItemTitle.setEnabled(enabled);
		labelStart.setEnabled(enabled);
		labelStartAt.setEnabled(enabled);
		labelEnd.setEnabled(enabled);
		labelEndAt.setEnabled(enabled);
		buttonDelete.setEnabled(enabled);
		
		if(enabled){
			textfieldStartTime.setValue(item.getBeginTime());
			textfieldEndTime.setValue(item.getEndTime());
			comboboxStartDay.setSelectedItem(item.getFrom());
			comboboxEndDay.setSelectedItem(item.getTo());
		}else{
			resetForm();
		}
	}
	
	/**
	 * Deletes the selected item from the table
	 */
	public void deleteSelected() {
		if(selected == null)return;
		TimetableItem selected = this.selected;
		select(null);
		removeFromTable(selected);
		formManager.setSaved(false);
		timeTable.removeItem(selected);
		tableTime.repaint();
	}
	
	private void removeFromTable(TimetableItem i){
		if(i == null)return;
		for(int x = 0; x < 7; x++){
			for(int y = 0; y < 24; y++){
				if(i.equals(tableTime.getValueAt(y,x))){
					tableTime.setValueAt(null,y,x);
				}
			}
		}
	}
	
	/**
	 * Reset the formula to its default values
	 */
	private void resetForm(){
		textfieldStartTime.setValue(LocalTime.of(0, 0, 0));
		textfieldEndTime.setValue(LocalTime.of(0, 0, 0));
		comboboxStartDay.setSelectedItem(null);
		comboboxEndDay.setSelectedItem(null);
	}

	/**
	 * Sets the timetable:
	 * Imports all items
	 * @param res : Wrapper for the timetable to import
	 */
	public void setTimetable(Timetable t) {
		timeTable = t;
		selectionModel.clearSelection();
		for(int i = 0; i < t.getNumItems(); i++){
			selectionModel.displayItem(t.getItem(i));
		}
	}
	

// 	---Unused transitional class---
//	private class TimetableItem{
//		private DayOfWeek from;
//		private DayOfWeek to;
//		private LocalTime beginTime;
//		private LocalTime endTime;
//		Color c;
//		String s;
//		public TimetableItem(DayOfWeek x1,LocalTime y1,DayOfWeek x2,LocalTime y2){
////			this.setFrom(x1);
////			this.setBeginTime(y1);
////			this.setTo(x2);
////			this.setEndTime(y2);
//			c = new Color((int)(Math.random()*255),(int)(Math.random()*255),(int)(Math.random()*255));
//			s = ""+Math.random();
//		}
//		@Override
//		public String toString(){return s;}
//		DayOfWeek getFrom() {
//			return from;
//		}
//
//		DayOfWeek getTo() {
//			return to;
//		}
//
//		LocalTime getBeginTime() {
//			return beginTime;
//		}
//
//		LocalTime getEndTime() {
//			return endTime;
//		}
//
//	}
	
	/**
	 * Class for handling user inputs/selections <br>
	 * changes the standard timetable selection behavior from blockwise to interval
	 * @author Leon Bein
	 *
	 */
	private class TimetableSelectionModel{
		
		//Row and column indices of selection
		private int rowMin,rowMax,colMin,colMax;
		/**Indicates whether the row selection is changing 
		 * @see {@link  javax.swing.DefaultListSelectionModel#getValueIsAdjusting()}*/
		private boolean rowChanging;
		/**Indicates whether the column selection is changing 
		 * @see {@link  javax.swing.DefaultListSelectionModel#getValueIsAdjusting()}*/
		private boolean colChanging;
		/**Flag to block row and column change events, when they are triggered programmatically*/
		private boolean block;
		/**The current selected cells; true means selected, false means not selected*/
		private boolean[][] selection;
		/**Flag indicating if the current selection collides with an existing item*/
		private boolean collision;
		
		/**
		 * Constructor
		 */
		public TimetableSelectionModel(){
			selection = new boolean[24][7];
		}

		/**
		 * Called when a row or column change event is fired
		 */
		public void valueChanged(){
			block = true;
			//When the selection is final and the items of the first and the last selected cell are equal and not null, the item is selected
			if(!colChanging && !rowChanging && tableTime.getValueAt(rowMin, colMin) != null && tableTime.getValueAt(rowMin, colMin) == tableTime.getValueAt(rowMax, colMax)){
				select((TimetableItem) tableTime.getValueAt(rowMin, colMin));
				//Clear the selection table
				clearSelection();
				//Clear the main table selection
				tableTime.clearSelection();
			}else{
				TimetableItem iv = null;
				//Update the selection table according to the interval pattern
				updateSelection();
				//When the selection is final, create new item if no collision
				if(!colChanging && !rowChanging){
					int nextRow = rowMax+1;
					int nextCol = colMax;
					if(nextRow == 24){
						nextRow = 0;
						nextCol++;
						nextCol %= 7;
					}
					//if no collision, create new item
					if(!collision){
						LocalTime startTime = LocalTime.of(rowMin,0,0);
						LocalTime endTime = LocalTime.of(nextRow,0,0);
						//If an "overlapping" preceding item exists, the start time is adapted
						if(getPreceding(rowMin, colMin) != null)startTime = ((TimetableItem)getPreceding(rowMin, colMin)).getEndTime();
						//If an "overlapping" following item exists, the end time and the selection are adapted, to not override the value in the border zone
						if(tableTime.getValueAt(rowMax, colMax) != null){
							endTime = ((TimetableItem)tableTime.getValueAt(rowMax, colMax)).getBeginTime();
							selection[rowMax][colMax] = false;
						}
						//Create new item and add it
						iv = timeTable.addItem(DayOfWeek.values()[colMin],DayOfWeek.values()[nextCol],startTime,endTime);
						formManager.setSaved(false);
						//Set all cells that are inside the selection to the new item
						setSelected(iv);
					}
					tableTime.clearSelection();
					clearSelection();
					setSelected(null);
				}
			}
			
			tableTime.repaint();
			//tableTime.setRowSelectionInterval(0, 0);
			
			block = false;
		}
		
		/**
		 * Defines the selection behavior.
		 * Sets the selection table according to the given selection indices (rowMin,colMin,rowMax,colMax)
		 */
		private void updateSelection(){
			collision = false;
			for(int y = 0; y < selection.length; y++){
				for(int x = 0; x < selection[y].length; x++){
					//When the cell lies between the start and end day it lies inside the selection interval
					if(x > colMin && x < colMax)selection[y][x] = true;
					//If start and end day are equal, the cell row must just lie between the two given rows
					else if(colMin == colMax){
						if(x == colMin && y >= rowMin && y <= rowMax)selection[y][x] = true;
						else selection[y][x] = false;
					//Otherwise, the cell is on the start XOR the end day,
						// if on the start day: it must be after the start row
						// if on the end day: it must be before the end row
					}else{
						if(x == colMin && y >= rowMin)selection[y][x] = true;
						else if(x == colMax && y <= rowMax)selection[y][x] = true;
						else selection[y][x] = false;
					}
					//If the selection at this cell is true and there is already an item in this cell, there is a collision
					//Special case: If the item does not start at xx:00 there can be an "overlapping"
					if(selection[y][x] && tableTime.getValueAt(y, x) != null && 
							(!(y == rowMax && x == colMax) || ((TimetableItem)tableTime.getValueAt(y, x)).getBeginTime().getMinute() == 0))collision = true;
				}
			}
		}
		
		/**
		 * Set the value of all cells inside the selection to a given timetable item
		 * @param iv : Wrapper tof the item to set
		 */
		private void setSelected(TimetableItem iv){
			for(int y = 0; y < selection.length; y++){
				for(int x = 0; x < selection[y].length; x++){
					if(selection[y][x])tableTime.setValueAt(iv, y, x);
				}
			}
		}
		
		/**
		 * Set the selection borders
		 * @param rmin : interval start row
		 * @param cmin : interval start column
		 * @param rmax : interval end row
		 * @param cmax : interval end column
		 */
		private void setSelection(int rmin, int rmax, int cmin, int cmax){
			rowMin = rmin;
			rowMax = rmax;
			colMin = cmin;
			colMax = cmax;
			updateSelection();
		}
		
		/**
		 * Processes row selection change events
		 * @param min : Selection interval start row
		 * @param max : Selection interval end row
		 * @param changing : @see {@link  javax.swing.DefaultListSelectionModel#getValueIsAdjusting()}
		 */
		public void rowChange(int min, int max, boolean changing){
			if(block)return;
//			if(rowMin == max){
//				max += min;
//				min = max - min;
//				max = max - min;
//			}
			rowMin = min;
			rowMax = max;
			rowChanging = changing;
			valueChanged();
		}

		/**
		 * Processes column selection change events
		 * @param min : Selection interval start column
		 * @param max : Selection interval end column
		 * @param changing : @see {@link  javax.swing.DefaultListSelectionModel#getValueIsAdjusting()}
		 */
		public void colChange(int min, int max, boolean changing){
			if(block)return;
			colMin = min;
			colMax = max;
			colChanging = changing;
			valueChanged();
		}
		
		
		/**
		 * Resets the selection table to false
		 */
		private void clearSelection(){
			for(int y = 0; y < selection.length; y++)
				for(int x = 0; x < selection[y].length; x++)
					selection[y][x] = false;
		}
		
		/**
		 * Displays a given item in the table or updates how it is displayed
		 * @param item : Wrapper for the item
		 */
		public void displayItem(TimetableItem item) {
			clearSelection();
			removeFromTable(item);
			setSelection(item.getBeginTime().getHour(), item.getEndTime().getHour()-1, item.getFrom().ordinal(), item.getTo().ordinal());
			setSelected(item);
			clearSelection();
		}
		
		
	}
	
	public class CellIntervalRenderer extends JPanel implements TableCellRenderer {
		
		private final int BORDERWIDTH = (int)(ScyllaGUI.SCALE*2.5);
		private final Color itemColor = Color.LIGHT_GRAY;
		private final Color selectedColor = itemColor.darker();
		private final Color borderColor = Color.GRAY;
		private final Color selectedBorderColor = borderColor.darker();
		private final Color collisionColor = new Color(210,190,190);
		
		//Labels and layout to display border regions between two intervals
		//Render rule: The later item is saved in the table
		/**Label to display an eventual overlap of the preceding item*/
		private JLabel upper;
		/**Label to display free space before the current item, if necessary*/
		private JLabel middle;
		/**Label to display the current item*/
		private JLabel lower;
		/**Layout; access necessary to change label weights*/
		private GridBagLayout layout;

		/**
		 * Constructor
		 */
		public CellIntervalRenderer() {
			setOpaque(true);
			setBorder(null);
			layout = new GridBagLayout();
			setLayout(layout);
			
			GridBagConstraints gbc_upper = new GridBagConstraints();
			gbc_upper.fill = GridBagConstraints.BOTH;
			gbc_upper.gridx = 0;
			gbc_upper.gridy = 0;
			gbc_upper.weighty = 0.5;
			gbc_upper.weightx = 0.5;
			upper = new JLabel();
			
			GridBagConstraints gbc_middle = new GridBagConstraints();
			gbc_middle.fill = GridBagConstraints.BOTH;
			gbc_middle.gridx = 0;
			gbc_middle.gridy = 1;
			gbc_middle.weighty = 1;
			gbc_middle.weightx = 0.5;
			middle = new JLabel();
			middle.setBackground(Color.WHITE);
			
			GridBagConstraints gbc_lower = new GridBagConstraints();
			gbc_lower.fill = GridBagConstraints.BOTH;
			gbc_lower.gridx = 0;
			gbc_lower.gridy = 2;
			gbc_lower.weighty = 0.5;
			gbc_lower.weightx = 0.5;
			lower = new JLabel();
			
			add(upper,gbc_upper);
			add(middle,gbc_middle);
			add(lower,gbc_lower);
			
			gbc_upper.weighty = 0;
			//gbl.setConstraints(upper, gbc_upper);
		}

		public Component getTableCellRendererComponent(JTable table, Object interval, boolean isSelected, boolean hasFocus,int row, int col) {
			Color selectionColor = selectionModel.collision ? collisionColor : Color.WHITE.darker();
			Color emptyColor = selectionModel.selection[row][col] ? selectionColor : Color.WHITE;
			middle.setBackground(emptyColor);
			upper.setBorder(null);
			lower.setBorder(null);
			//Item in the preceding cell
			TimetableItem pre = (TimetableItem)getPreceding(row, col);
			//Item in this cell
			TimetableItem iv = (TimetableItem)interval;
			//First case: the preceding cell is different => this cell is the beginning cell of its interval/item
			if(pre != interval){//!Object compared by ==!
				upper.setOpaque(true);
				middle.setOpaque(true);
				lower.setOpaque(true);
				setBorder(null);
				//Number of minutes that the preceding item has in this hour
				int timePre = 0;
				//Number of minutes that this item has in this hour
				int timeIv = 0;
				//If no preceding => 0 minutes
				if(pre == null)upper.setBackground(emptyColor);
				else{
					//Else create border left,bottom,right and indicate selection if necessary
					int f = BORDERWIDTH;
					if(pre == selected)f*=2;
					upper.setBorder(BorderFactory.createMatteBorder(0, f, f, f,pre == selected ? selectedBorderColor : borderColor));
					upper.setBackground(pre == selected ? selectedColor : itemColor);
					timePre = pre.getEndTime().getMinute();
				}
				//Same thing as for preceding interval
				if(interval == null)lower.setBackground(emptyColor);
				else{
					int f = BORDERWIDTH;
					if(iv == selected)f*=2;
					lower.setBorder(BorderFactory.createMatteBorder(f, f, 0, f,iv == selected ? selectedBorderColor : borderColor));
					lower.setBackground(iv == selected ? selectedColor : itemColor);
					timeIv = 60-iv.getBeginTime().getMinute();
				}
				//Gap time = 60 minutes minus already allocated time
				int timeMiddle = 60 - (timePre+timeIv);
				//Give each label the weight it needs
				reset(upper,timePre,0);
				reset(middle,timeMiddle,1);
				reset(lower,timeIv,2);

			}else{
				//Second case: the item is in the middle of its interval => no helping labels needed
				upper.setOpaque(false);
				middle.setOpaque(false);
				lower.setOpaque(false);
				if(interval == null){
					setBackground(emptyColor);
					setBorder(null);
				}else{
					int f = BORDERWIDTH;
					if(iv == selected){
						f*=2;
					}
					setBackground(iv == selected ? selectedColor : itemColor);
					
					//Creat left, right border
					MatteBorder border = BorderFactory.createMatteBorder(0, f, 0, f, iv == selected ? selectedBorderColor : borderColor);
					setBorder(border);
				}

			}
			
			return this;
		}
		
		/**
		 * Resets a labels gridy and gridweight
		 * @param label : The label to be reseted
		 * @param weight : The gridweight to be set
		 * @param y : The the gridy to be set
		 */
		private void reset(JLabel label, double weight, int y){
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.BOTH;
			gbc.gridx = 0;
			gbc.gridy = y;
			gbc.weighty = weight;
			gbc.weightx = 1;
			layout.setConstraints(label, gbc);
		}
		
	}
	

	
	
}