package de.hpi.bpt.scylla.GUI;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


@SuppressWarnings("serial")
public class CheckBoxList<T extends CheckBoxList.CheckBoxObserver> extends JList<T>{
	
	private JCheckBox[] boxes;
	
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
	
	private class CheckBoxCellRenderer<V> implements ListCellRenderer<V>{

		@Override
		public Component getListCellRendererComponent(JList<? extends V> list, V value, int index, boolean isSelected,
				boolean cellHasFocus) {
			boxes[index].setText(value.toString());
			return boxes[index];
		}
		
	}
	
	public interface CheckBoxObserver{
		public void stateChanged(boolean b);
		public boolean getState();
	}

	private class ObserverCheckBox extends JCheckBox{
		
		private CheckBoxObserver observer;
		private ObserverCheckBox(CheckBoxObserver o) {
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
