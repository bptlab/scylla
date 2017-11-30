package de.hpi.bpt.scylla.GUI;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.Icon;

/**
 * Utility Icon for checkboxes on large displays
 * @author Leon Bein
 *
 */
public class ScalingCheckBoxIcon implements Icon{
	
	/**The size the icon is scaled to in px*/
	private int size;
	
	/**
	 * Constructor
	 * @param sz : The size to give this icon
	 */
	public ScalingCheckBoxIcon(int sz) {
		size = sz;
	}

	/**
	 * Returns the size (icon is quadratic)
	 */
	@Override
	public int getIconHeight() {
		return size;
	}

	/**
	 * Returns the size (icon is quadratic)
	 */
	@Override
	public int getIconWidth() {
		return size;
	}

	/**
	 * Overriden paint method
	 */
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
			g.setColor(ScyllaGUI.ColorBackground);
			g.fillRect(border+xoff,border+yoff, size-2*border+1, size-2*border+1);
		}
		
	}

}



