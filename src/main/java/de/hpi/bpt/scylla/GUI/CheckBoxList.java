package de.hpi.bpt.scylla.GUI;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


@SuppressWarnings("serial")
/**
 * A List that containing objects that are represented by checkboxes and observe their selection
 * @author Leon Bein
 *
 * @param <T>
 */
public class CheckBoxList<T extends CheckBoxList.StateObserver> extends JList<T>{
	
	/**List of checkboxes, for overriding standard JList rendering routines*/
	private JCheckBox[] boxes;
	/**An observer that is notified if one of the checkboxes' value has change, the value returned is {@link de.hpi.bpt.scylla.GUI.CheckBoxList.anythingSelected()}*/
	private StateObserver observer;
	
	/**
	 * Creates a CheckBoxList with given set of elements (that have to extend {@link de.hpi.bpt.scylla.GUI.CheckBoxList.StateObserver})
	 * @param o
	 */
	public CheckBoxList(T[] o) {
		super(o);
		setFont(ScyllaGUI.DEFAULTFONT);
		setBorder(new EmptyBorder(ScyllaGUI.LEFTMARGIN));
		setCellRenderer(new CheckBoxCellRenderer<T>());
		
		boxes = new JCheckBox[o.length];
		for(int i = 0; i < o.length; i++){
			boxes[i] = new ObserverCheckBox(o[i]);
		}
		
		getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			//Called when one of the checkboxes has been selected, as normal checkbox selection doesn't work here.
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(!e.getValueIsAdjusting()){
					ListSelectionModel source = (ListSelectionModel)e.getSource();
					int i = source.getMinSelectionIndex();
					if(i == -1)return;
					//Flip corresponding checkbox
					boxes[i].setSelected(!boxes[i].isSelected());
					//Setback selection (fires another element, thats what if(i == -1) is for)
					source.clearSelection();
					//Notify observer if existing
					if(observer != null)observer.stateChanged(anythingSelected());
				}
			}
		});
	}
	
	/**
	 * Method that sets design of the boxes, has to be called seperately from constructor after values are set, in order to prevent NullPointerExceptions
	 */
	public void fixComponents(){
		for(int i = 0; i < boxes.length; i++){
			boxes[i].setFont(getFont());
			boxes[i].setBackground(getBackground());
			boxes[i].setForeground(getForeground());
		}
	}
	
	/**
	 * Sets all checkboxes to specified value
	 * @param b : boolean value all boxes are set to
	 */
	public void setAll(boolean b){
		for(int i = 0; i < boxes.length; i++){
			boxes[i].setSelected(b);
		}
		repaint();
	}
	
	/**
	 * Sets observer
	 * @param o : Any state observer object, when calling {@link de.hpi.bpt.scylla.GUI.CheckBoxList.StateObserver.stateChanged(boolean)} the value of {@link de.hpi.bpt.scylla.GUI.CheckBoxList.anythingSelected()} is handed over
	 */
	public void setObserver(StateObserver o){
		observer = o;
	}
	
	/**
	 * @return whether there is at least one checkbox that is selected
	 */
	public boolean anythingSelected(){
		for(int i = 0; i < boxes.length; i++){
			if(boxes[i].isSelected())return true;
		}
		return false;
	}
	
	/**
	 * Overrides normal ListCellRenderer to render the boxes
	 * @author Leon Bein
	 *
	 * @param <V> Class of list elements that are not used here as they are represented by checkboxes
	 */
	private class CheckBoxCellRenderer<V> implements ListCellRenderer<V>{

		@Override
		public Component getListCellRendererComponent(JList<? extends V> list, V value, int index, boolean isSelected,
				boolean cellHasFocus) {
			boxes[index].setText(value.toString());
			return boxes[index];
		}
		
	}
	
	/**
	 * Abstract Observer Interface, that can listen on general state changes
	 * @author Leon Bein
	 *
	 */
	public interface StateObserver{
		/**
		 * Notification for state change
		 * @param b : The new state, can be nearly anything
		 */
		public void stateChanged(boolean b);
		/**
		 * @return The current state that the observer has (for synchronization purpose)
		 */
		public boolean getState();
	}

	/**
	 * Checkbox class, that notifies a {@link de.hpi.bpt.scylla.GUI.CheckBoxList.StateObserver} when its selection state changes
	 * @author Leon Bein
	 *
	 */
	private class ObserverCheckBox extends JCheckBox{
		
		/**Selection state observer*/
		private StateObserver observer;
		
		/**
		 * Creates a new ObserverCheckBox.
		 * Initially sets the box' state to the observers
		 * @param o
		 */
		private ObserverCheckBox(StateObserver o) {
			super();
			observer = o;
			setSelected(o.getState());
		}
		

		@Override
		public void setSelected(boolean b){
			super.setSelected(b);
			observer.stateChanged(b);
		}
		
	}
	


}
