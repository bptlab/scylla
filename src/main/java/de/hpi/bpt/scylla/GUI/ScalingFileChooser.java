package de.hpi.bpt.scylla.GUI;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JFileChooser;

/**
 * Utility filechooser for large displays
 * @author Leon Bein
 *
 */
@SuppressWarnings("serial")
public class ScalingFileChooser extends JFileChooser{
	
	/**
	 * Constructor, automatically sets dimension and font <br>
	 * @see {@link javax.swing.JFileChooser#JFileChooser(String s)}
	 */
	public ScalingFileChooser(String s){
		super(s);
		setPreferredSize(new Dimension((int)(800.0*ScyllaGUI.SCALE),(int)(500.0*ScyllaGUI.SCALE)));
		setFont(getComponents(),ScyllaGUI.FILECHOOSERFONT);
	}
	
	/**
	 * Recursive method to set the font for all descending components 
	 * @param c : All children of the current "node"
	 * @param f : The font to be set
	 */
	private void setFont(Component[] c, Font f){
		for(int i = 0; i < c.length; i++){
			if(c[i] instanceof Container)setFont(((Container)c[i]).getComponents(),f);
			try{c[i].setFont(f);}catch(Exception e){};
		}
	}
	

}
