package de.hpi.bpt.scylla.GUI.plugin;

import java.awt.Container;
import java.util.Iterator;

import de.hpi.bpt.scylla.GUI.EditorPane;
import de.hpi.bpt.scylla.plugin_loader.PluginLoader;

public abstract class EditorTabPluggable<TargetClass extends EditorPane<?>, T extends Container> implements IGUIPlugin<TargetClass>{

	
	public static <EditorType extends EditorPane> void runPlugins(EditorType editor) {
		Iterator<EditorTabPluggable> editorTabExtensions = PluginLoader.dGetPlugins(EditorTabPluggable.class);

        while (editorTabExtensions.hasNext()) {
        	EditorTabPluggable<?,?> editorTabExtension = editorTabExtensions.next();
        	if(!editorTabExtension.isTarget(editor))continue;
        	editor.addTab(
        			editorTabExtension.getTabIdentifier(), 
        			editorTabExtension.getTitle(), 
        			editorTabExtension.getComponent()
        	);
        }
	}
	
	protected abstract String getTitle();
	
	protected Object getTabIdentifier() {
		return getClass();
	}
	
	protected abstract T getComponent();
}
