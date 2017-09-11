package de.hpi.bpt.scylla.GUI;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.DayOfWeek;
import java.time.LocalTime;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.MatteBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.Insets;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
public class TimetablePanel extends JSplitPane {
	private JTable tableTime;
	private JTable tableRowHeader;
	private TimetableSelectionModel selectionModel;
	
	private CellInterval selected;
	private JLabel labelInstanceTitle;
	private JLabel labelStart;
	private JLabel labelEnd;
	private JComboBox comboboxStartTime;
	private JLabel labelStartAt;
	private JLabel labelEndAt;
	private JComboBox comboBoxEndTime;
	private JComboBox comboboxStartDay;
	private JComboBox comboboxEndDay;
	private JButton buttonDelete;

	/**
	 * Create the panel.
	 */
	public TimetablePanel() {
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
					CellInterval val = (CellInterval) tableTime.getValueAt(tableTime.rowAtPoint(p), tableTime.columnAtPoint(p));
					selected = val;
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
		
		labelStart = new JLabel("Start");
		GridBagConstraints gbc_labelStart = new GridBagConstraints();
		gbc_labelStart.anchor = GridBagConstraints.WEST;
		gbc_labelStart.insets = new Insets(0, 15, 5, 5);
		gbc_labelStart.gridx = 0;
		gbc_labelStart.gridy = 1;
		gbc_labelStart.weightx = 1;
		panelBottom.add(labelStart, gbc_labelStart);
		
		comboboxStartTime = new JComboBox();
		GridBagConstraints gbc_comboboxStartTime = new GridBagConstraints();
		gbc_comboboxStartTime.weightx = 8.0;
		gbc_comboboxStartTime.insets = new Insets(0, 0, 5, 5);
		gbc_comboboxStartTime.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboboxStartTime.gridx = 1;
		gbc_comboboxStartTime.gridy = 1;
		panelBottom.add(comboboxStartTime, gbc_comboboxStartTime);
		
		labelStartAt = new JLabel("at");
		GridBagConstraints gbc_labelStartAt = new GridBagConstraints();
		gbc_labelStartAt.weightx = 0.5;
		gbc_labelStartAt.insets = new Insets(0, 0, 5, 5);
		gbc_labelStartAt.gridx = 2;
		gbc_labelStartAt.gridy = 1;
		panelBottom.add(labelStartAt, gbc_labelStartAt);
		
		comboboxStartDay = new JComboBox();
		GridBagConstraints gbc_comboboxStartDay = new GridBagConstraints();
		gbc_comboboxStartDay.weightx = 8.0;
		gbc_comboboxStartDay.insets = new Insets(0, 0, 5, 5);
		gbc_comboboxStartDay.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboboxStartDay.gridx = 3;
		gbc_comboboxStartDay.gridy = 1;
		panelBottom.add(comboboxStartDay, gbc_comboboxStartDay);
		
		buttonDelete = new JButton("D");
		GridBagConstraints gbc_buttonDelete = new GridBagConstraints();
		gbc_buttonDelete.weightx = 0.5;
		gbc_buttonDelete.gridheight = 2;
		gbc_buttonDelete.insets = new Insets(0, 0, 5, 0);
		gbc_buttonDelete.gridx = 4;
		gbc_buttonDelete.gridy = 1;
		panelBottom.add(buttonDelete, gbc_buttonDelete);
		
		labelEnd = new JLabel("End");
		GridBagConstraints gbc_labelEnd = new GridBagConstraints();
		gbc_labelEnd.anchor = GridBagConstraints.WEST;
		gbc_labelEnd.weightx = 1.0;
		gbc_labelEnd.insets = new Insets(0, 15, 0, 5);
		gbc_labelEnd.gridx = 0;
		gbc_labelEnd.gridy = 2;
		panelBottom.add(labelEnd, gbc_labelEnd);
		
		comboBoxEndTime = new JComboBox();
		GridBagConstraints gbc_comboBoxEndTime = new GridBagConstraints();
		gbc_comboBoxEndTime.weightx = 8.0;
		gbc_comboBoxEndTime.insets = new Insets(0, 0, 0, 5);
		gbc_comboBoxEndTime.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBoxEndTime.gridx = 1;
		gbc_comboBoxEndTime.gridy = 2;
		panelBottom.add(comboBoxEndTime, gbc_comboBoxEndTime);
		
		labelEndAt = new JLabel("at");
		GridBagConstraints gbc_labelEndAt = new GridBagConstraints();
		gbc_labelEndAt.weightx = 0.5;
		gbc_labelEndAt.insets = new Insets(0, 0, 0, 5);
		gbc_labelEndAt.gridx = 2;
		gbc_labelEndAt.gridy = 2;
		panelBottom.add(labelEndAt, gbc_labelEndAt);
		
		comboboxEndDay = new JComboBox();
		GridBagConstraints gbc_comboboxEndDay = new GridBagConstraints();
		gbc_comboboxEndDay.weightx = 8.0;
		gbc_comboboxEndDay.insets = new Insets(0, 0, 0, 5);
		gbc_comboboxEndDay.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboboxEndDay.gridx = 3;
		gbc_comboboxEndDay.gridy = 2;
		panelBottom.add(comboboxEndDay, gbc_comboboxEndDay);
		
	}
	
	private class CellInterval{
		int x1,y1,x2,y2;
		Color c;
		String s;
		public CellInterval(int x1,int y1,int x2,int y2){
			this.x1 = x1;
			this.y1 = y1;
			this.x2 = x2;
			this.y2 = y2;
			c = new Color((int)(Math.random()*255),(int)(Math.random()*255),(int)(Math.random()*255));
			s = ""+Math.random();
		}
		@Override
		public String toString(){return s;}
	}
	
	private class TimetableSelectionModel{
		
		private int rowMin,rowMax,colMin,colMax;
		private boolean rowChanging;
		private boolean colChanging;
		private boolean block;
		private boolean[][] selection;
		
		public TimetableSelectionModel(){
			selection = new boolean[24][7];
		}

		public void valueChanged(){
			block = true;
			if(!colChanging && !rowChanging && tableTime.getValueAt(rowMin, colMin) != null && tableTime.getValueAt(rowMin, colMin) == tableTime.getValueAt(rowMax, colMax)){
				selected = (CellInterval) tableTime.getValueAt(rowMin, colMin);
				clearSelection();
			}else{
				CellInterval iv = null;
				if(!colChanging && !rowChanging){
					iv = new CellInterval(colMin,rowMin,colMax,rowMax);
				}
				for(int y = 0; y < selection.length; y++){
					for(int x = 0; x < selection[y].length; x++){
						if(x > colMin && x < colMax)selection[y][x] = true;
						else if(colMin == colMax){
							//selected[y][x] = y >= rowMin && y <= rowMax;
							if(x == colMin && y >= rowMin && y <= rowMax)selection[y][x] = true;
							else selection[y][x] = false;
						}else{
							if(x == colMin && y >= rowMin)selection[y][x] = true;
							else if(x == colMax && y <= rowMax)selection[y][x] = true;
							else selection[y][x] = false;
						}
						if(iv != null && selection[y][x])tableTime.setValueAt(iv, y, x);
					}
				}
				if(iv != null){
					tableTime.clearSelection();
					clearSelection();
					selected = null;
				}
			}
			
			tableTime.repaint();
			//tableTime.setRowSelectionInterval(0, 0);
			
			block = false;
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
			for(int x = selected.x1; x <= selected.x2; x++){
				for(int y = 0; y < 24; y++){
					if(x == selected.x2 && y > selected.y2)return;
					if(x == selected.x1 && y < selected.y1)continue;
					tableTime.setValueAt(null,y,x);
				}
			}
		}
		
		private void clearSelection(){
			for(int y = 0; y < selection.length; y++)
				for(int x = 0; x < selection[y].length; x++)
					selection[y][x] = false;
		}
		
		
	}
	
	public class CellIntervalRenderer extends JLabel implements TableCellRenderer {
		
		MatteBorder border = BorderFactory.createMatteBorder(0, 0, 0, 0, Color.BLACK);

		public CellIntervalRenderer() {
			setOpaque(true);
			setBorder(border);
		}

		public Component getTableCellRendererComponent(JTable table, Object interval, boolean isSelected, boolean hasFocus,int row, int col) {
			if(interval == null){
				setBackground(Color.WHITE);
				setBorder(BorderFactory.createMatteBorder(0, 0, 0, 0, Color.BLACK));
			}else{
				CellInterval iv = (CellInterval)interval;
				Color newColor = iv.c;
				setBackground(newColor);
				Color bordercolor = Color.BLACK;
				int f = 5;
				if(iv == selected){
					setBackground(getBackground().brighter());
					bordercolor = bordercolor.brighter();
					f*=2;
				}
				setBorder(BorderFactory.createMatteBorder((row == iv.y1 && col == iv.x1) ? f : 0, f, (row == iv.y2 && col == iv.x2) ? f : 0, f, bordercolor));
			}
			if(selectionModel.selection[row][col])setBackground(getBackground().darker());
			return this;
		}
	}
	
	
}