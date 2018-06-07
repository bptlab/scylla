package de.hpi.bpt.scylla.GUI.plugin;

import java.awt.Component;

import de.hpi.bpt.scylla.GUI.EditorPane;
import de.hpi.bpt.scylla.plugin_type.IPluggable;

public interface IGUIPlugin<T extends Component> extends IPluggable{
	
	public T getComponent();
	
	default boolean isTarget(EditorPane editor) {
		return targetClass().isInstance(editor);
	}
	
	Class<? extends EditorPane> targetClass();
	
	

}
