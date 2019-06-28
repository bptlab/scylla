package de.hpi.bpt.scylla.GUI.ModelerPane;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;

import javax.swing.JPanel;

import org.jdom2.JDOMException;

import de.hpi.bpt.scylla.GUI.EditorPane;

public class ModelerPane extends EditorPane {
	
	private Modeler modeler;
	
	public ModelerPane() {
		modeler = new Modeler();		
		GridBagConstraints gbc_modeler = new GridBagConstraints();
		gbc_modeler.anchor = GridBagConstraints.PAGE_START;
//		gbc_model.insets = new Insets(inset_b,inset_b,inset_b,inset_b);
		gbc_modeler.gridx = 0;
		gbc_modeler.gridy = 0;
		gbc_modeler.fill = GridBagConstraints.BOTH;
		gbc_modeler.weightx = 1;
		gbc_modeler.weighty = 1;
		panelMain.add(modeler.getComponent(), gbc_modeler);
	}

	@Override
	protected void create() {
		setChangeFlag(true);
		close();
		setFile(new File("NewModel"+unnamedcount++ + ".bpmn"));
		modeler.createNew();
		setSaved(false);
		setEnabled(true);
		setChangeFlag(false);
	}

	@Override
	protected void save() {
		try {
			modeler.save(getFile().getPath());
			setSaved(true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void open() throws JDOMException, IOException {
		setChangeFlag(true);
		modeler.open(getFile());
		setChangeFlag(false);
	}

	@Override
	protected void close() {
		setChangeFlag(true);
		modeler.clear();
		setFile(null);
		setChangeFlag(false);
		setSaved(true);
		setEnabled(false);
	}

	@Override
	protected String getDefaultFileName() {
		return modeler.getProcessId();
	}
	
	@Override
	protected String getDefaultFileEnding() {
		return ".bpmn";
	}

}
