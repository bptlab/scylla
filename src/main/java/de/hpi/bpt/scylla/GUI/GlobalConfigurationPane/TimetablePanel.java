package de.hpi.bpt.scylla.GUI.GlobalConfigurationPane;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
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

import de.hpi.bpt.scylla.GUI.FormulaManager;
import de.hpi.bpt.scylla.GUI.InsertRemoveListener;
import de.hpi.bpt.scylla.GUI.ScyllaGUI;
import de.hpi.bpt.scylla.creation.GlobalConfiguration.GlobalConfigurationCreator.Timetable;
import de.hpi.bpt.scylla.creation.GlobalConfiguration.GlobalConfigurationCreator.Timetable.TimetableItem;

@SuppressWarnings("serial")
public class TimetablePanel extends JSplitPane {
	private JTable tableTime;
	private JTable tableRowHeader;
	private TimetableSelectionModel selectionModel;

	private JFormattedTextField textfieldStartTime;
	private JFormattedTextField textfieldEndTime;
	private JComboBox<DayOfWeek> comboboxStartDay;
	private JComboBox<DayOfWeek> comboboxEndDay;
	private JButton buttonDelete;
	
	private JLabel labelInstanceTitle;
	private JLabel labelStart;
	private JLabel labelStartAt;
	private JLabel labelEnd;
	private JLabel labelEndAt;

	
	private FormulaManager formulaManager;
	private TimetableItem selected;
	private Timetable timeTable;
	/**
	 * Create the panel.
	 * @param globalConfigurationPane 
	 */
	public TimetablePanel(FormulaManager fm) {
		formulaManager = fm;
		setOrientation(JSplitPane.VERTICAL_SPLIT);
		setEnabled(false);
		
		JPanel panelTop = new JPanel();
		setLeftComponent(panelTop);
		Object[][] times = new Object[24][1];
		for(int i = 0; i < times.length; i++){
			times[i] = new Object[]{LocalTime.of(i, 0)};
		}
		GridBagLayout gbl_panelTop = new GridBagLayout();
		panelTop.setLayout(gbl_panelTop);
		
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
		
		GridBagConstraints gbc_tableTimeHeader = new GridBagConstraints();
		gbc_tableTimeHeader.gridx = 1;
		gbc_tableTimeHeader.gridy = 0;
		gbc_tableTimeHeader.weightx = 7;
		gbc_tableTimeHeader.weighty = 0;
		gbc_tableTimeHeader.fill = GridBagConstraints.HORIZONTAL;
		
		panelTop.add(tableTime.getTableHeader(),gbc_tableTimeHeader);
		panelTop.add(tableTime, gbc_tableTime);
		
		selectionModel = new TimetableSelectionModel();
		tableTime.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				DefaultListSelectionModel s = (DefaultListSelectionModel)e.getSource();
				selectionModel.rowChange(s.getMinSelectionIndex(), s.getMaxSelectionIndex(), s.getValueIsAdjusting());
			}
		});
		
		tableTime.getColumnModel().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				DefaultListSelectionModel s = (DefaultListSelectionModel)e.getSource();
				selectionModel.colChange(s.getMinSelectionIndex(), s.getMaxSelectionIndex(), s.getValueIsAdjusting());
			}
		});
		
		tableTime.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e){
				if(SwingUtilities.isRightMouseButton(e)){
					Point p = e.getPoint();
					TimetableItem val = (TimetableItem) tableTime.getValueAt(tableTime.rowAtPoint(p), tableTime.columnAtPoint(p));
					select(val);
					tableTime.repaint();
				}
			}
		});
		
//		addKeyListener(new KeyAdapter() {
//			@Override
//			public void keyTyped(KeyEvent e){
//					System.out.println("del");
//					selectionModel.deleteSelected();
//			}
//		});
		tableTime.getInputMap().put(KeyStroke.getKeyStroke("DELETE"),"delete");
		tableTime.getInputMap().put(KeyStroke.getKeyStroke("BACK_SPACE"),"delete");
		tableTime.getActionMap().put("delete", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectionModel.deleteSelected();
			}
		});
		
		
		//---Bottom Panel ---

		JPanel panelBottom = new JPanel();
		setRightComponent(panelBottom);
		GridBagLayout gbl_panelBottom = new GridBagLayout();
		gbl_panelBottom.columnWeights = new double[]{0.0, 1.0, 0.0, 1.0, 0.0};
		panelBottom.setLayout(gbl_panelBottom);
		
		labelInstanceTitle = new JLabel("Current Item");
		GridBagConstraints gbc_labelInstanceTitle = new GridBagConstraints();
		gbc_labelInstanceTitle.insets = new Insets(0, 10, 30, 0);
		gbc_labelInstanceTitle.anchor = GridBagConstraints.PAGE_START;
		gbc_labelInstanceTitle.gridx = 0;
		gbc_labelInstanceTitle.gridy = 0;
		gbc_labelInstanceTitle.fill = GridBagConstraints.HORIZONTAL;
		gbc_labelInstanceTitle.gridwidth = 5;
		panelBottom.add(labelInstanceTitle, gbc_labelInstanceTitle);
		
		labelStart = new JLabel("Start Time");
		GridBagConstraints gbc_labelStart = new GridBagConstraints();
		gbc_labelStart.anchor = GridBagConstraints.WEST;
		gbc_labelStart.insets = new Insets(0, 15, 5, 5);
		gbc_labelStart.gridx = 0;
		gbc_labelStart.gridy = 1;
		gbc_labelStart.weightx = 1;
		panelBottom.add(labelStart, gbc_labelStart);
		
		textfieldStartTime = new JFormattedTextField(DateTimeFormatter.ISO_LOCAL_TIME.toFormat());
		textfieldStartTime.getDocument().addDocumentListener(new InsertRemoveListener((DocumentEvent e)->{
			if(formulaManager.isChangeFlag())return;
			if(selected == null)return;
			try{
				LocalTime l = LocalTime.parse(textfieldStartTime.getText());
				if(!l.equals(selected.getBeginTime())){
					selected.setBeginTime(l);
					selectionModel.alterItem(selected);
					formulaManager.setSaved(false);
				}
			}catch(Exception exc){}
		}));
		GridBagConstraints gbc_spinnerStartTime = new GridBagConstraints();
		gbc_spinnerStartTime.weightx = 8.0;
		gbc_spinnerStartTime.insets = new Insets(0, 0, 5, 5);
		gbc_spinnerStartTime.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinnerStartTime.gridx = 1;
		gbc_spinnerStartTime.gridy = 1;
		panelBottom.add(textfieldStartTime, gbc_spinnerStartTime);
		
		labelStartAt = new JLabel("at");
		GridBagConstraints gbc_labelStartAt = new GridBagConstraints();
		gbc_labelStartAt.weightx = 0.5;
		gbc_labelStartAt.insets = new Insets(0, 0, 5, 5);
		gbc_labelStartAt.gridx = 2;
		gbc_labelStartAt.gridy = 1;
		panelBottom.add(labelStartAt, gbc_labelStartAt);
		
		comboboxStartDay = new JComboBox<DayOfWeek>(DayOfWeek.values());
		comboboxStartDay.addItemListener((ItemEvent e)->{
			if(e.getStateChange() == ItemEvent.SELECTED){
				if(formulaManager.isChangeFlag())return;
				if(selected == null)return;
				selected.setFrom((DayOfWeek) comboboxStartDay.getSelectedItem());
				selectionModel.alterItem(selected);
				formulaManager.setSaved(false);
			}
		});
		GridBagConstraints gbc_comboboxStartDay = new GridBagConstraints();
		gbc_comboboxStartDay.weightx = 8.0;
		gbc_comboboxStartDay.insets = new Insets(0, 0, 5, 5);
		gbc_comboboxStartDay.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboboxStartDay.gridx = 3;
		gbc_comboboxStartDay.gridy = 1;
		panelBottom.add(comboboxStartDay, gbc_comboboxStartDay);
		
		buttonDelete = new JButton();
		buttonDelete.setIcon(ScyllaGUI.ICON_X);
		buttonDelete.setToolTipText("Delete selected item");
		buttonDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				selectionModel.deleteSelected();
			}
		});
		GridBagConstraints gbc_buttonDelete = new GridBagConstraints();
		gbc_buttonDelete.weightx = 0.5;
		gbc_buttonDelete.gridheight = 2;
		gbc_buttonDelete.insets = new Insets(0, 0, 5, 0);
		gbc_buttonDelete.gridx = 4;
		gbc_buttonDelete.gridy = 1;
		panelBottom.add(buttonDelete, gbc_buttonDelete);
		
		labelEnd = new JLabel("End Time");
		GridBagConstraints gbc_labelEnd = new GridBagConstraints();
		gbc_labelEnd.anchor = GridBagConstraints.WEST;
		gbc_labelEnd.weightx = 1.0;
		gbc_labelEnd.insets = new Insets(0, 15, 0, 5);
		gbc_labelEnd.gridx = 0;
		gbc_labelEnd.gridy = 2;
		panelBottom.add(labelEnd, gbc_labelEnd);
		
		textfieldEndTime = new JFormattedTextField(DateTimeFormatter.ISO_LOCAL_TIME.toFormat());
		textfieldEndTime.getDocument().addDocumentListener(new InsertRemoveListener((DocumentEvent e)->{
			if(formulaManager.isChangeFlag())return;
			if(selected == null)return;
			try{
				LocalTime l = LocalTime.parse(textfieldEndTime.getText());
				if(!l.equals(selected.getEndTime())){
					selected.setEndTime(l);
					selectionModel.alterItem(selected);
					formulaManager.setSaved(false);
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
		
		labelEndAt = new JLabel("at");
		GridBagConstraints gbc_labelEndAt = new GridBagConstraints();
		gbc_labelEndAt.weightx = 0.5;
		gbc_labelEndAt.insets = new Insets(0, 0, 0, 5);
		gbc_labelEndAt.gridx = 2;
		gbc_labelEndAt.gridy = 2;
		panelBottom.add(labelEndAt, gbc_labelEndAt);
		
		comboboxEndDay = new JComboBox<DayOfWeek>(DayOfWeek.values());
		comboboxEndDay.addItemListener((ItemEvent e)->{
			if(e.getStateChange() == ItemEvent.SELECTED){
				if(formulaManager.isChangeFlag())return;
				if(selected == null)return;
				selected.setTo((DayOfWeek) comboboxEndDay.getSelectedItem());
				selectionModel.alterItem(selected);
				formulaManager.setSaved(false);
			}
		});
		GridBagConstraints gbc_comboboxEndDay = new GridBagConstraints();
		gbc_comboboxEndDay.weightx = 8.0;
		gbc_comboboxEndDay.insets = new Insets(0, 0, 0, 5);
		gbc_comboboxEndDay.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboboxEndDay.gridx = 3;
		gbc_comboboxEndDay.gridy = 2;
		panelBottom.add(comboboxEndDay, gbc_comboboxEndDay);
		
		select(null);
	}
	
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

	public void select(TimetableItem item) {
		selected = item;
		boolean enabled = item != null;
		
		textfieldStartTime.setEnabled(enabled);
		textfieldEndTime.setEnabled(enabled);
		comboboxStartDay.setEnabled(enabled);
		comboboxEndDay.setEnabled(enabled);
		
		labelInstanceTitle.setEnabled(enabled);
		labelStart.setEnabled(enabled);
		labelStartAt.setEnabled(enabled);
		labelEnd.setEnabled(enabled);
		labelEndAt.setEnabled(enabled);
		
		if(enabled){
			textfieldStartTime.setValue(item.getBeginTime());
			textfieldEndTime.setValue(item.getEndTime());
			comboboxStartDay.setSelectedItem(item.getFrom());
			comboboxEndDay.setSelectedItem(item.getTo());
		}else{
			resetFormula();
		}
	}

	public void setTimetable(Timetable t) {
		timeTable = t;
		selectionModel.clearSelection();
		for(int i = 0; i < t.getNumItems(); i++){
			selectionModel.alterItem(t.getItem(i));
		}
	}
	
	private void resetFormula(){
		textfieldStartTime.setValue(LocalTime.of(0, 0, 0));
		textfieldEndTime.setValue(LocalTime.of(0, 0, 0));
		comboboxStartDay.setSelectedItem(null);
		comboboxEndDay.setSelectedItem(null);
	}
	
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
	
	private class TimetableSelectionModel{
		
		private int rowMin,rowMax,colMin,colMax;
		private boolean rowChanging;
		private boolean colChanging;
		private boolean block;
		private boolean[][] selection;
		
		public TimetableSelectionModel(){
			selection = new boolean[24][7];
		}

		public void alterItem(TimetableItem item) {
			selectionModel.clearSelection();
			selectionModel.setSelection(item.getBeginTime().getHour(), item.getEndTime().getHour()-1, item.getFrom().ordinal(), item.getTo().ordinal());
			selectionModel.setSelected(item);
			selectionModel.clearSelection();
		}

		public void valueChanged(){
			block = true;
			if(!colChanging && !rowChanging && tableTime.getValueAt(rowMin, colMin) != null && tableTime.getValueAt(rowMin, colMin) == tableTime.getValueAt(rowMax, colMax)){
				select((TimetableItem) tableTime.getValueAt(rowMin, colMin));
				clearSelection();
				tableTime.clearSelection();
			}else{
				TimetableItem iv = null;
				updateSelection();
				if(!colChanging && !rowChanging){
					iv = timeTable.addItem(DayOfWeek.values()[colMin],DayOfWeek.values()[colMax],LocalTime.of(rowMin,0,0),LocalTime.of(rowMax+1,0,0));
					formulaManager.setSaved(false);
					setSelected(iv);
					tableTime.clearSelection();
					clearSelection();
					setSelected(null);
				}
			}
			
			tableTime.repaint();
			//tableTime.setRowSelectionInterval(0, 0);
			
			block = false;
		}
		
		private void updateSelection(){
			for(int y = 0; y < selection.length; y++){
				for(int x = 0; x < selection[y].length; x++){
					if(x > colMin && x < colMax)selection[y][x] = true;
					else if(colMin == colMax){
						if(x == colMin && y >= rowMin && y <= rowMax)selection[y][x] = true;
						else selection[y][x] = false;
					}else{
						if(x == colMin && y >= rowMin)selection[y][x] = true;
						else if(x == colMax && y <= rowMax)selection[y][x] = true;
						else selection[y][x] = false;
					}
				}
			}
		}
		
		private void setSelected(TimetableItem iv){
			for(int y = 0; y < selection.length; y++){
				for(int x = 0; x < selection[y].length; x++){
					if(selection[y][x])tableTime.setValueAt(iv, y, x);
				}
			}
		}
		
		private void setSelection(int rmin, int rmax, int cmin, int cmax){
			rowMin = rmin;
			rowMax = rmax;
			colMin = cmin;
			colMax = cmax;
			updateSelection();
		}
		
		public void rowChange(int min, int max, boolean changing){
			if(block)return;
			rowMin = min;
			rowMax = max;
			rowChanging = changing;
			valueChanged();
		}

		public void colChange(int min, int max, boolean changing){
			if(block)return;
			colMin = min;
			colMax = max;
			colChanging = changing;
			valueChanged();
		}
		
		public void deleteSelected() {
			if(selected == null)return;
			for(int x = selected.getFrom().ordinal(); x <= selected.getTo().ordinal(); x++){
				for(int y = 0; y < 24; y++){
					if(x == selected.getTo().ordinal() && y > selected.getEndTime().getHour())return;
					if(x == selected.getFrom().ordinal() && y < selected.getBeginTime().getHour())continue;
					tableTime.setValueAt(null,y,x);
				}
			}
			formulaManager.setSaved(false);
			timeTable.removeItem(selected);
			setSelected(null);
			TimetablePanel.this.select(null);
		}
		
		private void clearSelection(){
			for(int y = 0; y < selection.length; y++)
				for(int x = 0; x < selection[y].length; x++)
					selection[y][x] = false;
		}
		
		
	}
	
	public class CellIntervalRenderer extends JPanel implements TableCellRenderer {
		
		private final int BORDERWIDTH = (int)(ScyllaGUI.SCALE*2.5);
		private final Color itemColor = Color.LIGHT_GRAY;
		private final Color selectedColor = itemColor.darker();
		private final Color borderColor = Color.GRAY;
		private final Color selecetedBorderColor = borderColor.darker();
		
		private JLabel upper;
		private JLabel middle;
		private JLabel lower;
		private GridBagLayout layout;

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
			Color emptyColor = selectionModel.selection[row][col] ? Color.WHITE.darker() : Color.WHITE;
			middle.setBackground(emptyColor);
			upper.setBorder(null);
			lower.setBorder(null);
			TimetableItem pre = (TimetableItem)getPreceding(row, col);
			TimetableItem iv = (TimetableItem)interval;
			if(pre != interval){//!Object compared by ==!
				upper.setOpaque(true);
				middle.setOpaque(true);
				lower.setOpaque(true);
				setBorder(null);
				int timePre = 0;
				int timeIv = 0;
				if(pre == null)upper.setBackground(emptyColor);
				else{
					int f = BORDERWIDTH;
					if(pre == selected)f*=2;
					upper.setBorder(BorderFactory.createMatteBorder(0, f, f, f,pre == selected ? selecetedBorderColor : borderColor));
					upper.setBackground(pre == selected ? selectedColor : itemColor);
					timePre = pre.getEndTime().getMinute();
				}
				if(interval == null)lower.setBackground(emptyColor);
				else{
					int f = BORDERWIDTH;
					if(iv == selected)f*=2;
					lower.setBorder(BorderFactory.createMatteBorder(f, f, 0, f,iv == selected ? selecetedBorderColor : borderColor));
					lower.setBackground(iv == selected ? selectedColor : itemColor);
					timeIv = 60-iv.getBeginTime().getMinute();
				}
				int timeMiddle = 60 - (timePre+timeIv);
				reset(upper,timePre,0);
				reset(middle,timeMiddle,1);
				reset(lower,timeIv,2);

			}else{
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
	
					//Border black = BorderFactory.createMatteBorder((row == iv.y1.getHour() && col == iv.x1.ordinal()) ? f : 0, f, (row == iv.y2.getHour() && col == iv.x2.ordinal()) ? f : 0, f, bordercolor);
					MatteBorder black = BorderFactory.createMatteBorder(0, f, 0, f, iv == selected ? selecetedBorderColor : borderColor);
					setBorder(black);
				}

			}
			
			
			return this;
		}
		
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