package de.hpi.bpt.scylla.GUI;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Utility filechooser for large displays
 * @author Leon Bein
 *
 */
@SuppressWarnings("serial")
public class ScalingFileChooser extends JFileChooser {
	

	public static final FileFilter FILEFILTER_XML = new FileNameExtensionFilter("XML files","xml");
	public static final FileFilter FILEFILTER_BPMN = new FileNameExtensionFilter("BPMN files","bpmn");
	
	/**
	 * Constructor, automatically sets dimension and font <br>
	 * @see {@link javax.swing.JFileChooser#JFileChooser(String s)}
	 */
	public ScalingFileChooser(String s) {
		super(s);
		setPreferredSize(new Dimension((int)(800.0*ScyllaGUI.SCALE),(int)(500.0*ScyllaGUI.SCALE)));
		setFont(getComponents(),ScyllaGUI.FILECHOOSERFONT);
	}
	
	/**
	 * Recursive method to set the font for all descending components 
	 * @param c : All children of the current "node"
	 * @param f : The font to be set
	 */
	private void setFont(Component[] c, Font f) {
		for(int i = 0; i < c.length; i++) {
			if(c[i] instanceof Container)setFont(((Container)c[i]).getComponents(),f);
			try{c[i].setFont(f);}catch(Exception e){};
		}
	}
	
	@Override
	public void setFileFilter(FileFilter filter) {
		addChoosableFileFilter(filter);
		super.setFileFilter(filter);
	}
	
	public void setXMLFilter() {
		setFileFilter(FILEFILTER_XML);
	}
	
	public void setBPMNFilter() {
		setFileFilter(FILEFILTER_BPMN);
	}
	

}
