package de.hpi.bpt.scylla.GUI.plugin;

import java.awt.Component;

import de.hpi.bpt.scylla.GUI.EditorPane;
import de.hpi.bpt.scylla.plugin_type.IPluggable;

public interface IGUIPlugin<TargetClass extends EditorPane<?>> extends IPluggable{
	
	
	default boolean isTarget(EditorPane<?> editor) {
		return targetClass().isInstance(editor);
	}
	
	Class<TargetClass> targetClass();
	
	

}
