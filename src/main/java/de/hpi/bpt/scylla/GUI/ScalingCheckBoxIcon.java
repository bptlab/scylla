package de.hpi.bpt.scylla.GUI;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.Icon;

public class ScalingCheckBoxIcon implements Icon{
	
	private int size;
	
	public ScalingCheckBoxIcon(int sz) {
		size = sz;
	}

	@Override
	public int getIconHeight() {
		return size;
	}

	@Override
	public int getIconWidth() {
		return size;
	}

	@Override
	public void paintIcon(Component checkBox, Graphics g, int x, int y) {

		int xoff = (size)/8;
		int yoff = (checkBox.getHeight()-size)/2;
		int border = size/8;
		
		ButtonModel checkBoxModel = ((AbstractButton)checkBox).getModel();
		g.setColor(ScyllaGUI.ColorField2);
		g.fillRect(xoff,yoff,size,size);
		
		g.setColor(ScyllaGUI.ColorBackground);
		if(!checkBoxModel.isRollover())g.drawRect(xoff,yoff,size,size);
		if(checkBoxModel.isPressed()){
			g.setColor(ScyllaGUI.ColorField1);
			g.fillRect(border+xoff,border+yoff, size-2*border+1, size-2*border+1);
		}else if(checkBoxModel.isSelected()){
			g.setColor(ScyllaGUI.ColorField0);
			g.fillRect(border+xoff,border+yoff, size-2*border+1, size-2*border+1);
		}
		
	}

}



