package de.hpi.bpt.scylla.plugin.guiPluginPrototypes;

import java.awt.Color;

import javax.swing.JPanel;

import de.hpi.bpt.scylla.GUI.EditorPane;
import de.hpi.bpt.scylla.GUI.GlobalConfigurationPane.GlobalConfigurationPane;
import de.hpi.bpt.scylla.GUI.plugin.EditorTabPluggable;

public class PrototypeEditorTabPlugin extends EditorTabPluggable<JPanel>{
	
	private JPanel panel;
	
	public PrototypeEditorTabPlugin() {
		panel = new JPanel();
		panel.setBackground(Color.RED);
	}

	@Override
	public JPanel getComponent() {
		return panel;
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Editor Tab Prototype";
	}

	@Override
	public Class<GlobalConfigurationPane> targetClass() {
		return GlobalConfigurationPane.class;
	}
	

}
