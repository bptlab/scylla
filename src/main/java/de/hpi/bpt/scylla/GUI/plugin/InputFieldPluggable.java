package de.hpi.bpt.scylla.GUI.plugin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.hpi.bpt.scylla.GUI.EditorPane;
import de.hpi.bpt.scylla.GUI.InputFields.InputField;
import de.hpi.bpt.scylla.creation.ElementLink;
import de.hpi.bpt.scylla.plugin_loader.PluginLoader;

public abstract class InputFieldPluggable<TargetClass extends EditorPane<?>> implements IGUIPlugin<TargetClass>{
		

	public static <CreatorType extends ElementLink> void runPlugins(EditorPane<CreatorType> editor) {
		Iterator<InputFieldPluggable> inputFieldExtensions = PluginLoader.dGetPlugins(InputFieldPluggable.class);

        while (inputFieldExtensions.hasNext()) {
        	InputFieldPluggable inputFieldExtension = inputFieldExtensions.next();
        	if(!inputFieldExtension.isTarget(editor))continue;
        	inputFieldExtension.runPlugin(editor);
        }
	}
	
	public static <CreatorType extends ElementLink> void notifyPluginsOnLoad(EditorPane<CreatorType> editor) {
		Iterator<InputFieldPluggable> inputFieldExtensions = PluginLoader.dGetPlugins(InputFieldPluggable.class);

        while (inputFieldExtensions.hasNext()) {
        	InputFieldPluggable inputFieldExtension = inputFieldExtensions.next();
        	if(!inputFieldExtension.isTarget(editor))continue;
        	inputFieldExtension.onLoad(editor);
        }
	}
	
	protected List<InputField<?, ?>> fieldsToLoad = new ArrayList<>();

	protected abstract void runPlugin(TargetClass editor);
	protected List<InputField<?, ?>> fieldsToLoad(){
		return fieldsToLoad;
	}
	protected void onLoad(TargetClass editor) {
		for(InputField<?, ?> field : fieldsToLoad()) {
			field.loadSavedValue();
		}
	}
	


}
