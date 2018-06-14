package de.hpi.bpt.scylla.plugin.guiPluginPrototypes;

import de.hpi.bpt.scylla.GUI.EditorPane;
import de.hpi.bpt.scylla.GUI.GlobalConfigurationPane.GlobalConfigurationPane;
import de.hpi.bpt.scylla.GUI.InputFields.StringField;
import de.hpi.bpt.scylla.GUI.plugin.InputFieldPluggable;
import de.hpi.bpt.scylla.creation.GlobalConfiguration.GlobalConfigurationCreator;

public class PrototypeInputFieldPluggable extends InputFieldPluggable<GlobalConfigurationPane>{
	
	private String model = "Look at mee!";
	private StringField field;
	
	@Override
	protected void runPlugin(GlobalConfigurationPane editor) {
		createInputField(editor);
    	editor.addInputField(
			getTabIdentifier(),
			getLabel(),
			field.getComponent());
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	
	protected void createInputField(EditorPane<GlobalConfigurationCreator> editor) {
		field = new StringField(editor){
				
				@Override
				protected void setSavedValue(String v) {
					if(editor.getCreator() == null)return;
					editor.getCreator().setId(v);
					System.out.println(getName()+" saved "+v);
				}
				
				@Override
				protected String getSavedValue() {
					if(editor.getCreator() == null)return null;
					return editor.getCreator().getId();
				}
				
				public void loadSavedValue() {
					super.loadSavedValue();
					//TODO Getsavedvalue returns null
					System.out.println("Loaded "+getSavedValue());
				}
			};
		fieldsToLoad.add(field);
	}

	protected String getLabel() {
		return "Protoype Input:";
	}

	public Class<GlobalConfigurationPane> targetClass() {
		return GlobalConfigurationPane.class;
	}

	protected Object getTabIdentifier() {
		return GlobalConfigurationPane.Tabs.GENERAL;
	}


}
