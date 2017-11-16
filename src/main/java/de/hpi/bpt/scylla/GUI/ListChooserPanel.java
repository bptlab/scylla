package de.hpi.bpt.scylla.GUI;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

/**
 * Panel that allows the user to select items on the left side to display their content on the right side.<br>
 * @author Leon Bein
 *
 */
@SuppressWarnings("serial")
public class ListChooserPanel extends JSplitPane {
	
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
	protected DefaultTableModel model;
	/**List realization by a tably*/
	protected JTable list;
	/**Empty panel if no item is selected*/
	protected JPanel panelRight;
	
	/**Panel for list*/
	protected JPanel panelLeft;
	/**For subclass use: determines whether there exists any editing possibility*/
	protected boolean editable;

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
		
		super.setEnabled(false);
		
		panelLeft = new JPanel();
		setLeftComponent(panelLeft);
		GridBagLayout gbl_panelLeft = new GridBagLayout();
		panelLeft.setLayout(gbl_panelLeft);
		
		editable = false;
		model = new DefaultTableModel(new ComponentHolder[][]{}, new Object[]{"Column1"}){
			public boolean isCellEditable(int row, int col){
				return editable && super.isCellEditable(row, col);
			}
		};
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
		gbc_scrollPane.gridy = 1;//Allow Contents to be added at 0
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
		

		scrollPane.setViewportView(list);
		
		
		panelRight = new JPanel();
		setRightComponent(panelRight);
		panelRight.setLayout(null);
		

	}
	
	/**
	 * Enables/disables all necessary components in order to prevent invalid states
	 */
	@Override
	public void setEnabled(boolean b){
		list.setEnabled(b);
		panelRight.setEnabled(b);
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
	 * Returns whether the list contains an item with the given name
	 * @param s : The name to check
	 * @return true if such an item exists, false if not
	 */
	protected boolean contains(String s){
		int h = model.getRowCount();
		for(int i = 0; i < h; i++){
			if(model.getValueAt(i, 0).equals(s))return true;
		}
		return false;
	}
	
	/**
	 * Calls a method for every componentholder element saved inside the list
	 * @param lambda : Consumer functional interface, gets each element passed as argument
	 */
	public void forAll(Consumer<ComponentHolder> lambda) {
		for(int i = 0; i < model.getRowCount(); i++) {
			lambda.accept((ComponentHolder) model.getValueAt(i, 0));
		}
	}
	
	

}
