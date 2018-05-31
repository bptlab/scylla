package de.hpi.bpt.scylla.GUI.plugin;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.util.Iterator;

import de.hpi.bpt.scylla.GUI.EditorPane;
import de.hpi.bpt.scylla.model.SimulationInput;
import de.hpi.bpt.scylla.plugin_loader.PluginLoader;

public abstract class EditorTabPluggable<T extends Component> implements IGUIPlugin<T>{

	public abstract String getTitle();
	
	public static int runPlugins(Container parent, int index) {
		Iterator<? extends EditorTabPluggable> editorTabExtensions = (Iterator<? extends EditorTabPluggable>)PluginLoader.dGetPlugins(EditorTabPluggable.class);

        while (editorTabExtensions.hasNext()) {
        	EditorTabPluggable editorTabExtension = editorTabExtensions.next();
        	Component tab = EditorPane.createTab(editorTabExtension.getTitle(), editorTabExtension.getComponent());
        	GridBagConstraints gbc = EditorPane.createTabConstraints(index++);
        	parent.add(tab, gbc);
        }
        
        return index;
	}
	
}
