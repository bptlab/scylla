package de.hpi.bpt.scylla.GUI;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class ListPanel<T extends JComponent> extends JPanel{
	
	private DefaultListModel<T> model;
	private Box boxHolder;

	/**
	 * Create the panel.
	 */
	public ListPanel() {
		setBackground(ScyllaGUI.ColorField2);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		boxHolder = Box.createVerticalBox();
		GridBagConstraints gbc_boxHolder = new GridBagConstraints();
		gbc_boxHolder.anchor = GridBagConstraints.PAGE_START;
		gbc_boxHolder.fill = GridBagConstraints.HORIZONTAL;
		gbc_boxHolder.gridx = 0;
		gbc_boxHolder.gridy = 0;
		gbc_boxHolder.weightx = 1;
		add(boxHolder, gbc_boxHolder);
		model = new DefaultListModel<T>();
	}
	
	public void addElement(T toAdd) {
		model.addElement(toAdd);
		boxHolder.add(toAdd);
		revalidate();
		repaint();
	}
	
	public void add(int index, T toAdd) {
		model.add(index, toAdd);
		boxHolder.add(toAdd,index);
		revalidate();
		repaint();
	}
	
	public boolean removeElement(T toRem) {
		boxHolder.remove(toRem);
		revalidate();
		repaint();
		return model.removeElement(toRem);
	}

	public T getElementAt(int index) {
		return model.getElementAt(index);
	}
	
	public int getListSize() {
		return model.size();
	}
	
//	public DefaultListModel<T> getModel(){
//		return model;
//	}

}
