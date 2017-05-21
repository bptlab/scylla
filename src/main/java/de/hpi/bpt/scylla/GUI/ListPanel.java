package de.hpi.bpt.scylla.GUI;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import de.hpi.bpt.scylla.GUI.CheckBoxList.StateObserver;


/**
 * Panel that can be selected via a checkbox and expanded via a button to show a {@link de.hpi.bpt.scylla.GUI.CheckBoxList<T extends StateObserver>}
 * @author Leon Bein
 *
 */
@SuppressWarnings("serial")
public class ListPanel extends JPanel implements StateObserver{

	/**Checkbox to make Panel selectable*/
	private JCheckBox checkbox_title;
	/**Margin Textfield*/
	private JTextField textfield_sub;
	/**Expansion button*/
	private JButton button_expand;
	/**Checkboxlist*/
	private CheckBoxList<? extends CheckBoxList.StateObserver> list;
	
	/**Tells whether the panel is expanded or not*/
	private boolean expanded;
	/**GBC for list, to preserve layout when collapsing*/
	private GridBagConstraints gbc_list;
	
	/**
	 * Creates a new Panel
	 * @param text : Title text
	 * @param li : Objects for the checkboxlist
	 */
	public ListPanel(String text,List<? extends CheckBoxList.StateObserver> li) {
		GridBagLayout gridBagLayout = new GridBagLayout();
		setLayout(gridBagLayout);
		
		checkbox_title = new JCheckBox(text);
		checkbox_title.setHorizontalAlignment(SwingConstants.LEFT);
		checkbox_title.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				list.setAll(checkbox_title.isSelected());
			}
		});
		checkbox_title.setBackground(ScyllaGUI.ColorField1);
		checkbox_title.setFont(ScyllaGUI.DEFAULTFONT);
		checkbox_title.setFocusPainted(false);
		GridBagConstraints gbc_checkbox_title = new GridBagConstraints();
		gbc_checkbox_title.weightx = 1.0;
		gbc_checkbox_title.fill = GridBagConstraints.HORIZONTAL;
		gbc_checkbox_title.gridx = 0;
		gbc_checkbox_title.gridy = 0;
		add(checkbox_title, gbc_checkbox_title);
		
		button_expand = new JButton("+");
		button_expand.setToolTipText("Expand");
		button_expand.setHorizontalAlignment(SwingConstants.CENTER);
		button_expand.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(expanded)collapse(); else expand();
			}
		});
		button_expand.setBackground(ScyllaGUI.ColorField1);
		button_expand.setFont(ScyllaGUI.DEFAULTFONT);
		button_expand.setFocusPainted(false);
		GridBagConstraints gbc_button_expand = new GridBagConstraints();
		gbc_checkbox_title.weightx = 0.0;
		gbc_checkbox_title.gridx = 1;
		gbc_checkbox_title.gridy = 0;
		add(button_expand, gbc_button_expand);
		
		CheckBoxList.StateObserver[] a = new CheckBoxList.StateObserver[li.size()];
		li.toArray(a);
		list = new CheckBoxList<>(a);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setBackground(ScyllaGUI.ColorField2);
		list.fixComponents();
		gbc_list = new GridBagConstraints();
		gbc_list.fill = GridBagConstraints.HORIZONTAL;
		gbc_list.gridx = 0;
		gbc_list.gridy = 1;
		gbc_list.gridwidth = 2;
		add(list, gbc_list);
		list.setFont(ScyllaGUI.DEFAULTFONT);
		
		list.setObserver(this);
		checkbox_title.setSelected(list.anythingSelected());
		
		textfield_sub = new JTextField(" ");
		textfield_sub.setHorizontalAlignment(SwingConstants.LEFT);
		textfield_sub.setForeground(Color.WHITE);
		textfield_sub.setBorder(new EmptyBorder(0, 0, 0, 0));
		textfield_sub.setHighlighter(null);
		textfield_sub.setEditable(false);
		textfield_sub.setBackground(ScyllaGUI.ColorField2);
		GridBagConstraints gbc_textfield_sub = new GridBagConstraints();
		gbc_textfield_sub.fill = GridBagConstraints.HORIZONTAL;
		gbc_textfield_sub.gridx = 0;
		gbc_textfield_sub.gridy = 2;
		gbc_textfield_sub.gridwidth = 2;
		add(textfield_sub, gbc_textfield_sub);
		
		expanded = true;
		collapse();

	}
	
	/**
	 * Expands the panel
	 */
	public void expand(){
		if(!expanded){
			button_expand.setText("-");
			button_expand.setToolTipText("Collapse");
			add(list,gbc_list);
			revalidate();
		}
		expanded = true;
	}
	
	
	/**
	 * Collapses the panel
	 */
	public void collapse(){
		if(expanded){
			button_expand.setText("+");
			button_expand.setToolTipText("Expand");
			remove(list);
			revalidate();
		}
		expanded = false;
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
	
}
