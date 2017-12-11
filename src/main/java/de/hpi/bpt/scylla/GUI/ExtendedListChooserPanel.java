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
import javax.swing.JTable;
import javax.swing.JTextField;

/**
 * Panel that allows the user to select items on the left side to display their content on the right side.<br>
 * Items can be added, removed and renamed by abstract methods.
 * @author Leon Bein
 *
 */
@SuppressWarnings("serial")
public abstract class ExtendedListChooserPanel extends ListChooserPanel{
	
	/**Button to add new items*/
	private JButton buttonAdd;
	/**Button to remove current selected item*/
	private JButton buttonRemove;
	
	/**
	 * Constructor without initial data
	 */
	public ExtendedListChooserPanel(){
		this(null);
	}
	
	/**
	 * Constructor with initial data
	 * @param data : intial data
	 */
	public ExtendedListChooserPanel(List<ComponentHolder> data) {
		super(data);
		
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
		
		//Button to add items
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
		
		//Button to remove items
		buttonRemove = new JButton();
		buttonRemove.setIcon(ScyllaGUI.ICON_REMOVE);
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

		//Add editing possibility
		editable = true;
		list.setDefaultEditor(Object.class, new ComponentHolderCellEditor());
	}
	
	/**
	 * Enables/disables all necessary components in order to prevent invalid states
	 */
	@Override
	public void setEnabled(boolean b){
		super.setEnabled(b);
		buttonAdd.setEnabled(b);
		buttonRemove.setEnabled(b);
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
