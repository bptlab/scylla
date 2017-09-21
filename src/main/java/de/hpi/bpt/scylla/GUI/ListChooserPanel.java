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
 * Panel that allows the user to select items on the left side to display their content on the right side.<br>
 * Items cann be added, removed and renamed by abstract methods.
 * @author Leon Bein
 *
 */
@SuppressWarnings("serial")
public abstract class ListChooserPanel extends JSplitPane{
	
	/**
	 * Item interface
	 * @author Leon Bein
	 *
	 */
	public interface ComponentHolder{
		/**Returns the Component that is shown when the item is selected*/
		public Component getComponent();
		/**Called when the item is renamed*/
		public void setName(String s);
		/**Optional method, called when the item is deleted*/
		public default void delete(){};
	}

	/**Model of the list on the left side, as realized by a JTable*/
	private DefaultTableModel model;
	/**List realization by a tably*/
	private JTable list;
	/**Empty panel if no item is selected*/
	private JPanel panelRight;
	/**Button to add new items*/
	private JButton buttonAdd;
	/**Button to remove current selected item*/
	private JButton buttonRemove;
	
	/**
	 * Constructor without initial data
	 */
	public ListChooserPanel(){
		this(null);
	}
	
	/**
	 * Constructor with initial data
	 * @param data : intial data
	 */
	public ListChooserPanel(List<ComponentHolder> data) {
		
		JPanel panelLeft = new JPanel();
		setLeftComponent(panelLeft);
		GridBagLayout gbl_panelLeft = new GridBagLayout();
		panelLeft.setLayout(gbl_panelLeft);
		
		//Panel for add and remove button
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
				if(list.getCellEditor() != null)list.getCellEditor().stopCellEditing();
				int index = model.getRowCount();
				model.addRow(new ComponentHolder[]{onCreate()});
				//Starts editing the newly added item in order to give it another name
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
				if(list.getCellEditor() != null)list.getCellEditor().stopCellEditing();
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
		
		//Scrollpane to scroll over many items if necessary
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

	/**
	 * Adds the item to the list
	 * @param item : A componentholder item
	 */
	public void add(ComponentHolder item) {
		model.addRow(new Object[]{item});
	}
	
	/**
	 * Deletes all saved items
	 */
	public void clear(){
		if(list.getCellEditor() != null)list.getCellEditor().cancelCellEditing();
		while(model.getRowCount() > 0){
			((ComponentHolder)model.getValueAt(0,0)).delete();
			model.removeRow(0);
		}
	}
	
	/**
	 * Enables/disables all necessary components in order to prevent invalid states
	 */
	@Override
	public void setEnabled(boolean b){
		list.setEnabled(b);
		buttonAdd.setEnabled(b);
		buttonRemove.setEnabled(b);
		panelRight.setEnabled(b);
		super.setEnabled(b);
	}
	
	/**
	 * Returns whether the list contains an item with the given name
	 * @param s : The name to check
	 * @return true if such an item exists, false if not
	 */
	private boolean contains(String s){
		int h = model.getRowCount();
		for(int i = 0; i < h; i++){
			if(model.getValueAt(i, 0).equals(s))return true;
		}
		return false;
	}
	
	/**
	 * Editor class for the table/list
	 * @author Leon Bein
	 *
	 */
	private class ComponentHolderCellEditor extends DefaultCellEditor{
		/**Editing component*/
		private JTextField textfield;
		/**Edited item*/
		private ComponentHolder current;
		
		/**
		 * Standard Constructor
		 */
		public ComponentHolderCellEditor(){
			this(new JTextField());
		}
		
		/**
		 * Additional constructor to be able to set the textfield attribute
		 * @param f
		 */
		private ComponentHolderCellEditor(JTextField f){
			super(f);
			textfield = f;
		}

		/**
		 * Sets the name of the edited item to the textfields value. <br>
		 * Special behavior: If the items name was "<enter name>", 
		 * the name is automatically converted to "Unnamed" plus a number if multiple of these instances exist
		 */
		@Override
		public ComponentHolder getCellEditorValue() {
			if(textfield.getText().equals("<enter name>")){
				String s = "Unnamed";
				String t = s;
				int i = 2;
				while(contains(t)){
					t = s+"("+i+")";
					i++;
				}
				textfield.setText(t);
			}
			current.setName(textfield.getText());
			return current;
		}

		/**
		 * Sets both the edited item plus the text of the textfield
		 */
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
