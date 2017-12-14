package de.hpi.bpt.scylla.GUI;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Panel that replaces JList for JComponents, using a swing Box for displaying various contents in a column.
 * @author Leon Bein
 *
 * @param <T> : A subclass of JComponent, type of components that are going to be displayed.
 */
@SuppressWarnings("serial")
public class ListPanel<T extends JComponent> extends JPanel{
	
	/**Listmodel to manage the list model stuff behind this component*/
	private DefaultListModel<T> model;
	/**Swing box to manage the list display stuff behind this component*/
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
	
	/**
	 * Adds element to list model and display list.
	 * @param toAdd : A component of parameterized type
	 */
	public void addElement(T toAdd) {
		model.addElement(toAdd);
		boxHolder.add(toAdd);
		revalidate();
		repaint();
	}
	
	/**
	 * Adds an element to list model and display list at a specific index.
	 * @param index : Index where to add the object, has to be inside list range or -1 (to add at the end, but use {@link #addElement(T)} instead)
	 * @param toAdd : A component of parameterized type
	 */
	public void add(int index, T toAdd) {
		model.add(index, toAdd);
		boxHolder.add(toAdd,index);
		revalidate();
		repaint();
	}
	
	/**
	 * Removes a given element from list model and display list
	 * @param toRem : A component of parameterized type
	 * @return true if the argument was a component of this list; false otherwise
	 */
	public boolean removeElement(T toRem) {
		boxHolder.remove(toRem);
		revalidate();
		repaint();
		return model.removeElement(toRem);
	}

	/**
	 * @return List element at specific index 
	 * @param index : An index in valid range (-1 to list size)
	 */
	public T getElementAt(int index) {
		return model.get(index);
	}
	
	/**
	 * @return The size of the list model
	 */
	public int getListSize() {
		return model.size();
	}


}
