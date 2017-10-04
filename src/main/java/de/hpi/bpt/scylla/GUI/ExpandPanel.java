package de.hpi.bpt.scylla.GUI;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingConstants;


/**
 * Panel that can be expanded and collapsed via a button
 * @author Leon Bein
 *
 */
@SuppressWarnings("serial")
public class ExpandPanel extends JPanel{

	/**Header Component*/
	protected Component header;
	/**Expansion button*/
	protected JButton button_expand;
	
	/**Tells whether the panel is expanded or not*/
	protected boolean expanded;

	/**Content component*/
	protected Component content;
	/**GBC for content, to preserve layout when collapsing*/
	protected GridBagConstraints gbc_content;
	
	/**
	 * Creates a new Panel
	 * @param h : Header Component 
	 * @param c : Content Component
	 */
	public ExpandPanel(Component h,Component c){
		GridBagLayout gridBagLayout = new GridBagLayout();
		setLayout(gridBagLayout);
		
		header = h;
		GridBagConstraints gbc_header = new GridBagConstraints();
		gbc_header.weightx = 1.0;
		gbc_header.fill = GridBagConstraints.HORIZONTAL;
		gbc_header.gridx = 0;
		gbc_header.gridy = 0;
		add(header, gbc_header);
		if(header.getBackground() != null){
			setBackground(header.getBackground());
		}
		
		button_expand = new JButton();
		button_expand.setIcon(ScyllaGUI.ICON_EXPAND);
		button_expand.setToolTipText("Expand");
		button_expand.setHorizontalAlignment(SwingConstants.CENTER);
		button_expand.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(expanded)collapse(); else expand();
			}
		});
		button_expand.setFont(ScyllaGUI.DEFAULTFONT);
		button_expand.setFocusPainted(false);
		GridBagConstraints gbc_button_expand = new GridBagConstraints();
		gbc_button_expand.weightx = 0.0;
		gbc_button_expand.gridx = 1;
		gbc_button_expand.gridy = 0;
		add(button_expand, gbc_button_expand);
		
		content = c;
		gbc_content = new GridBagConstraints();
		gbc_content.fill = GridBagConstraints.HORIZONTAL;
		gbc_content.gridx = 0;
		gbc_content.gridy = 1;
		gbc_content.gridwidth = 2;
		add(content, gbc_content);
		
		expanded = true;
		collapse();

	}
	
	/**
	 * Expands the panel
	 */
	public void expand(){
		if(!expanded){
			button_expand.setIcon(ScyllaGUI.ICON_COLLAPSE);
			button_expand.setToolTipText("Collapse");
			add(content,gbc_content);
			revalidate();
		}
		expanded = true;
	}
	
	
	/**
	 * Collapses the panel
	 */
	public void collapse(){
		if(expanded){
			button_expand.setIcon(ScyllaGUI.ICON_EXPAND);
			button_expand.setToolTipText("Expand");
			remove(content);
			revalidate();
		}
		expanded = false;
	}

	
}
