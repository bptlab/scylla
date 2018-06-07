package de.hpi.bpt.scylla.GUI.plugin;

import java.awt.Container;
import java.util.Iterator;

import de.hpi.bpt.scylla.GUI.EditorPane;
import de.hpi.bpt.scylla.plugin_loader.PluginLoader;

public abstract class EditorTabPluggable<T extends Container> implements IGUIPlugin<T>{

	
	public static <EditorType extends EditorPane> void runPlugins(EditorType editor) {
		Iterator<EditorTabPluggable> editorTabExtensions = PluginLoader.dGetPlugins(EditorTabPluggable.class);

        while (editorTabExtensions.hasNext()) {
        	EditorTabPluggable<?> editorTabExtension = editorTabExtensions.next();
        	if(!editorTabExtension.isTarget(editor))continue;
        	editor.addTab(editorTabExtension.getClass(), editorTabExtension.getTitle(), editorTabExtension.getComponent());
        }
	}
	
	protected abstract String getTitle();
}
