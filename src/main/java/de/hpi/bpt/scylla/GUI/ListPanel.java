package de.hpi.bpt.scylla.GUI;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.border.EmptyBorder;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.SwingConstants;
import javax.swing.BoxLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;


/**
 * @author Leon Bein
 *
 */
@SuppressWarnings("serial")
public class ListPanel extends JPanel {

	private JButton button_title;
	private JTextField textfield_sub;
	private CheckBoxList<? extends CheckBoxList.CheckBoxObserver> list;
	
	private boolean expanded;
	private GridBagConstraints gbc_list;
	
	/**
	 * Create the panel.
	 */
	public ListPanel(String text,List<? extends CheckBoxList.CheckBoxObserver> li) {
		GridBagLayout gridBagLayout = new GridBagLayout();
		setLayout(gridBagLayout);

		
		CheckBoxList.CheckBoxObserver[] a = new CheckBoxList.CheckBoxObserver[li.size()];
		li.toArray(a);
		
		button_title = new JButton(text);
		button_title.setToolTipText("Show components of "+text);
		button_title.setHorizontalAlignment(SwingConstants.LEFT);
		button_title.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(expanded)collapse(); else expand();
			}
		});
		button_title.setBackground(ScyllaGUI.ColorField1);
		GridBagConstraints gbc_button_title = new GridBagConstraints();
		gbc_button_title.weightx = 1.0;
		gbc_button_title.fill = GridBagConstraints.HORIZONTAL;
		gbc_button_title.gridx = 0;
		gbc_button_title.gridy = 0;
		add(button_title, gbc_button_title);
		
		button_title.setFont(ScyllaGUI.DEFAULTFONT);
		list = new CheckBoxList<>(a);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setBackground(ScyllaGUI.ColorField2);
		list.fixComponents();
		gbc_list = new GridBagConstraints();
		gbc_list.fill = GridBagConstraints.HORIZONTAL;
		gbc_list.gridx = 0;
		gbc_list.gridy = 1;
		add(list, gbc_list);
		list.setFont(ScyllaGUI.DEFAULTFONT);
		
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
		add(textfield_sub, gbc_textfield_sub);
		
		expanded = true;
		collapse();

	}
	
	public void expand(){
		if(!expanded){
			add(list,gbc_list);
			revalidate();
		}
		expanded = true;
	}
	
	public void collapse(){
		if(expanded){
			remove(list);
			revalidate();
		}
		expanded = false;
	}
	
}
