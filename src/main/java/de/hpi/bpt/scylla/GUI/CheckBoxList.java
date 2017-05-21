package de.hpi.bpt.scylla.GUI;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


@SuppressWarnings("serial")
public class CheckBoxList<T extends CheckBoxList.StateObserver> extends JList<T>{
	
	private JCheckBox[] boxes;
	private StateObserver observer;
	
	public CheckBoxList(T[] o) {
		super(o);
		setFont(ScyllaGUI.DEFAULTFONT);
		setCellRenderer(new CheckBoxCellRenderer<T>());
		boxes = new JCheckBox[o.length];
		for(int i = 0; i < o.length; i++){
			boxes[i] = new ObserverCheckBox(o[i]);
		}
		getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(!e.getValueIsAdjusting()){
					ListSelectionModel source = (ListSelectionModel)e.getSource();
					int i = source.getMinSelectionIndex();
					if(i == -1)return;
					boxes[i].setSelected(!boxes[i].isSelected());
					source.clearSelection();
					if(observer != null)observer.stateChanged(anythingSelected());
				}
			}
		});
	}
	
	public void fixComponents(){
		for(int i = 0; i < boxes.length; i++){
			boxes[i].setFont(getFont());
			boxes[i].setBackground(getBackground());
			boxes[i].setForeground(getForeground());
		}
	}
	
	public void setAll(boolean b){
		for(int i = 0; i < boxes.length; i++){
			boxes[i].setSelected(b);
		}
		repaint();
	}
	
	public void setObserver(StateObserver o){
		observer = o;
	}
	
	public boolean anythingSelected(){
		for(int i = 0; i < boxes.length; i++){
			if(boxes[i].isSelected())return true;
		}
		return false;
	}
	
	private class CheckBoxCellRenderer<V> implements ListCellRenderer<V>{

		@Override
		public Component getListCellRendererComponent(JList<? extends V> list, V value, int index, boolean isSelected,
				boolean cellHasFocus) {
			boxes[index].setText(value.toString());
			return boxes[index];
		}
		
	}
	
	public interface StateObserver{
		public void stateChanged(boolean b);
		public boolean getState();
	}

	private class ObserverCheckBox extends JCheckBox{
		
		private StateObserver observer;
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
