package de.hpi.bpt.scylla.GUI;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;


/**
 * @author Leon Bein
 *
 */
@SuppressWarnings("serial")
public class ListPanel extends JPanel {

	private JTextField textfield_title;
	private CheckBoxList<? extends CheckBoxList.CheckBoxObserver> list;
	
	/**
	 * Create the panel.
	 */
	public ListPanel(String text,List<? extends CheckBoxList.CheckBoxObserver> li) {
		setLayout(new BorderLayout(0, 0));
		
		textfield_title = new JTextField(text);
		textfield_title.setHighlighter(null);
		textfield_title.setEditable(false);
		textfield_title.setBackground(ScyllaGUI.ColorField1);
		add(textfield_title,BorderLayout.NORTH);
		
		CheckBoxList.CheckBoxObserver[] a = new CheckBoxList.CheckBoxObserver[li.size()];
		li.toArray(a);
		list = new CheckBoxList<>(a);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setBackground(ScyllaGUI.ColorField2);
		list.fixComponents();
		add(list,BorderLayout.CENTER);
		
		
		textfield_title.setFont(ScyllaGUI.DEFAULTFONT);
		list.setFont(ScyllaGUI.DEFAULTFONT);
		


	}
	
}
