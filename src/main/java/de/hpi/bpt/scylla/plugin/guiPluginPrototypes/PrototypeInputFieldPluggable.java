package de.hpi.bpt.scylla.plugin.guiPluginPrototypes;

import de.hpi.bpt.scylla.GUI.FormManager;
import de.hpi.bpt.scylla.GUI.GlobalConfigurationPane.GlobalConfigurationPane;
import de.hpi.bpt.scylla.GUI.InputFields.StringField;
import de.hpi.bpt.scylla.GUI.plugin.InputFieldPluggable;

public class PrototypeInputFieldPluggable extends InputFieldPluggable<StringField>{
	
	private String model;

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	@Override
	protected StringField createInputField() {
		return new StringField(
				new FormManager() {
					public void setSaved(boolean b) {}
					public boolean isChangeFlag() {return false;}
					public void setChangeFlag(boolean b) {}
				}){
				
				@Override
				protected void setSavedValue(String v) {
					model = v;
					System.out.println(getName()+" saved "+v);
				}
				
				@Override
				protected String getSavedValue() {
					return model;
				}
			};
	}

	@Override
	protected String getLabel() {
		return "Protoype Input:";
	}

	@Override
	public Class<GlobalConfigurationPane> targetClass() {
		return GlobalConfigurationPane.class;
	}

	@Override
	protected Object getTabIdentifier() {
		return GlobalConfigurationPane.Tabs.GENERAL;
	}

}
