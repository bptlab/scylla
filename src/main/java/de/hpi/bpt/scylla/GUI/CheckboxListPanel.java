package de.hpi.bpt.scylla.GUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

import de.hpi.bpt.scylla.GUI.CheckBoxList.StateObserver;


/**
 * Panel that can be selected via a checkbox and expanded via a button to show a {@link de.hpi.bpt.scylla.GUI.CheckBoxList<T extends StateObserver>}
 * @author Leon Bein
 *
 */
@SuppressWarnings("serial")
public class CheckboxListPanel extends ExpandPanel implements StateObserver{

	/**Checkbox to make Panel selectable*/
	private JCheckBox checkbox_title;

	/**Checkboxlist*/
	private CheckBoxList<? extends CheckBoxList.StateObserver> list;
	
	/**
	 * Creates a new Panel
	 * @param text : Title text
	 * @param li : Objects for the checkboxlist
	 */
	public CheckboxListPanel(String text,List<? extends CheckBoxList.StateObserver> li) {
		super(new JCheckBox(text),new CheckBoxList<>(
				li.toArray(new CheckBoxList.StateObserver[li.size()])
				));

		checkbox_title = (JCheckBox) header;
		checkbox_title.setIcon(new ScalingCheckBoxIcon(ScyllaGUI.DEFAULTFONT.getSize()));
		checkbox_title.setHorizontalAlignment(SwingConstants.LEFT);
		checkbox_title.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				list.setAll(checkbox_title.isSelected());
			}
		});
		checkbox_title.setBackground(ScyllaGUI.ColorField1);
		checkbox_title.setFont(ScyllaGUI.DEFAULTFONT);
		checkbox_title.setFocusPainted(false);
		
		@SuppressWarnings("unchecked")
		CheckBoxList<? extends StateObserver> cast = (CheckBoxList<? extends StateObserver>) content;
		list = cast;
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setBackground(ScyllaGUI.ColorField2);
		list.fixComponents();
		list.setFont(ScyllaGUI.DEFAULTFONT);
		list.setForeground(ScyllaGUI.DEFAULTFONT_COLOR);
		
		list.setObserver(this);
		checkbox_title.setSelected(list.anythingSelected());
	}

	/**
	 * Sets general checkbox based on if there are any selected checkboxes in the checkboxlist
	 */
	@Override
	public void stateChanged(boolean b) {
		checkbox_title.setSelected(b);
	}

	/**
	 * @return the state of the general checkbox
	 */
	@Override
	public boolean getState() {
		return checkbox_title.isSelected();
	}
	
	public void setSelected(boolean b){
		checkbox_title.setSelected(b);
		list.setAll(checkbox_title.isSelected());
	}
	
}
