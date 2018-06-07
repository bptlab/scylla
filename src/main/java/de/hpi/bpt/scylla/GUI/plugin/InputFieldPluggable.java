package de.hpi.bpt.scylla.GUI.plugin;

import java.util.Iterator;

import javax.swing.JComponent;

import de.hpi.bpt.scylla.GUI.EditorPane;
import de.hpi.bpt.scylla.GUI.InputFields.InputField;
import de.hpi.bpt.scylla.plugin_loader.PluginLoader;

public abstract class InputFieldPluggable<FieldType extends InputField<?,?>> implements IGUIPlugin<JComponent>{
		

	public static <EditorType extends EditorPane> void runPlugins(EditorType editor) {
		Iterator<InputFieldPluggable> inputFieldExtensions = PluginLoader.dGetPlugins(InputFieldPluggable.class);

        while (inputFieldExtensions.hasNext()) {
        	InputFieldPluggable<?> inputFieldExtension = inputFieldExtensions.next();
        	if(!inputFieldExtension.isTarget(editor))continue;
        	editor.addInputField(
        			inputFieldExtension.getTabIdentifier(),
        			inputFieldExtension.getLabel(),
        			inputFieldExtension.getComponent());
        }
	}
	
	@Override
	public JComponent getComponent() {
		return createInputField().getComponent();
	}
	
	protected abstract FieldType createInputField();
	
	protected abstract String getLabel();
	
	protected abstract Object getTabIdentifier();

}
