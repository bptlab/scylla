package de.hpi.bpt.scylla.GUI;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

/**
 * 
 * @author Leon Bein
 *
 */
@SuppressWarnings("serial")
public abstract class ListChooserPanel extends JSplitPane{
	
	public interface ComponentHolder{
		public Component getComponent();
		public default void setName(String s){};
		public default void delete(){};
	}

	private DefaultTableModel model;
	private JTable list;
	private JPanel panelRight;
	private JButton buttonAdd;
	private JButton buttonRemove;
	
	
	public ListChooserPanel(){
		this(null);
	}
	
	public ListChooserPanel(List<ComponentHolder> data) {
		

		//setEnabled(false);
		JPanel panelLeft = new JPanel();
		setLeftComponent(panelLeft);
		GridBagLayout gbl_panelLeft = new GridBagLayout();
		panelLeft.setLayout(gbl_panelLeft);
		
		JPanel panelListHeader = new JPanel();
		GridBagConstraints gbc_panelListHeader = new GridBagConstraints();
		gbc_panelListHeader.gridx = 0;
		gbc_panelListHeader.gridy = 0;
		gbc_panelListHeader.weighty = 0;
		gbc_panelListHeader.weightx = 1;
		gbc_panelListHeader.fill = GridBagConstraints.HORIZONTAL;
		panelLeft.add(panelListHeader, gbc_panelListHeader);
		GridBagLayout gbl_panelListHeader = new GridBagLayout();
		panelListHeader.setLayout(gbl_panelListHeader);
		
		buttonAdd = new JButton();
		buttonAdd.setIcon(ScyllaGUI.ICON_PLUS);
		buttonAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(list.getCellEditor() != null)list.getCellEditor().cancelCellEditing();
				int index = model.getRowCount();
				model.addRow(new ComponentHolder[]{onCreate()});
				list.editCellAt(index, 0);
				list.getEditorComponent().requestFocus();
				((JTextField) list.getEditorComponent()).selectAll();
				list.setRowSelectionInterval(index, index);
			}
		});
		
		GridBagConstraints gbc_buttonAdd = new GridBagConstraints();
		gbc_buttonAdd.anchor = GridBagConstraints.WEST;
		gbc_buttonAdd.gridx = 0;
		gbc_buttonAdd.gridy = 0;
		gbc_buttonAdd.fill = GridBagConstraints.HORIZONTAL;
		gbc_buttonAdd.weighty = 0;
		gbc_buttonAdd.weightx = 1;
		panelListHeader.add(buttonAdd, gbc_buttonAdd);
		
		buttonRemove = new JButton();
		buttonRemove.setIcon(ScyllaGUI.ICON_X);
		buttonRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(list.getCellEditor() != null)list.getCellEditor().cancelCellEditing();
				int sel = list.getSelectedRow();
				if(sel != -1){
					ComponentHolder toDel = (ComponentHolder) model.getValueAt(sel, 0);
					toDel.delete();
					onDelete(toDel);
					model.removeRow(sel);
				}
			}
		});
		GridBagConstraints gbc_buttonRemove = new GridBagConstraints();
		gbc_buttonRemove.anchor = GridBagConstraints.WEST;
		gbc_buttonRemove.gridx = 1;
		gbc_buttonRemove.gridy = 0;
		gbc_buttonRemove.fill = GridBagConstraints.HORIZONTAL;
		gbc_buttonRemove.weighty = 0;
		gbc_buttonRemove.weightx = 1;
		panelListHeader.add(buttonRemove, gbc_buttonRemove);

		model = new DefaultTableModel(new ComponentHolder[][]{}, new Object[]{"Column1"});
		if(data != null){
			for(ComponentHolder item : data){
				add(item);
			}
		}
		
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 1;
		gbc_scrollPane.weighty = 1;
		gbc_scrollPane.weightx = 1;
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		
		panelLeft.add(scrollPane, gbc_scrollPane);
		list = new JTable(model);
		list.setFillsViewportHeight(true);
		list.setShowVerticalLines(false);
		list.setShowHorizontalLines(false);
		list.setShowGrid(false);
		list.setTableHeader(null);
		list.setRowHeight(list.getFont().getSize());
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setSurrendersFocusOnKeystroke(true);
		list.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(!e.getValueIsAdjusting()){
					ListSelectionModel source = (ListSelectionModel)e.getSource();
					int i = source.getMinSelectionIndex();
					if(i == -1 || !(model.getValueAt(i,0) instanceof ComponentHolder))setRightComponent(panelRight);
					else setRightComponent(((ComponentHolder) model.getValueAt(i,0)).getComponent());
					getParent().revalidate();
					getParent().repaint();
					revalidate();
					repaint();
				}
			}
		});
		list.setDefaultEditor(Object.class, new ComponentHolderCellEditor());
		scrollPane.setViewportView(list);
		
		
		panelRight = new JPanel();
		setRightComponent(panelRight);
		panelRight.setLayout(null);
		
	}

	public void add(ComponentHolder item) {
		model.addRow(new Object[]{item});
	}
	
	public void clear(){
		if(list.getCellEditor() != null)list.getCellEditor().cancelCellEditing();
		while(model.getRowCount() > 0){
			((ComponentHolder)model.getValueAt(0,0)).delete();
			model.removeRow(0);
		}
	}
	
	@Override
	public void setEnabled(boolean b){
		list.setEnabled(b);
		buttonAdd.setEnabled(b);
		buttonRemove.setEnabled(b);
		panelRight.setEnabled(b);
		super.setEnabled(b);
	}
	
	private class ComponentHolderCellEditor extends DefaultCellEditor{
		private JTextField textfield;
		private ComponentHolder current;
		
		public ComponentHolderCellEditor(){
			this(new JTextField());
		}
		
		private ComponentHolderCellEditor(JTextField f){
			super(f);
			textfield = f;
		}

		@Override
		public ComponentHolder getCellEditorValue() {
			current.setName(textfield.getText());
			return current;
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
				int column) {
			current = (ComponentHolder) value;
			textfield.setText(value.toString());
			return textfield;
		}
		
	}
	
	/**
	 * Callback when an object is deleted via the delete button
	 * @param toDel: Componentholder Object that is deleted
	 */
	public abstract void onDelete(ComponentHolder toDel);
	/**
	 * Factory to determine which object to add, when the add button is pressed
	 * @return: New componentholder of chosen type with default parameters
	 */
	public abstract ComponentHolder onCreate();
	
	
}
