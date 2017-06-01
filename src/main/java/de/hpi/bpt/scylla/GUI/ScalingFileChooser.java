package de.hpi.bpt.scylla.GUI;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;

import javax.swing.JFileChooser;

@SuppressWarnings("serial")
public class ScalingFileChooser extends JFileChooser{
	
	public ScalingFileChooser(String s){
		super(s);
		setPreferredSize(ScyllaGUI.fileChooserDimension);
		setFont(getComponents(),ScyllaGUI.fileChooserFont);
	}
	
	private void setFont(Component[] c, Font f){
		for(int i = 0; i < c.length; i++){
			if(c[i] instanceof Container)setFont(((Container)c[i]).getComponents(),f);
			try{c[i].setFont(f);}catch(Exception e){};
		}
	}
	

}
