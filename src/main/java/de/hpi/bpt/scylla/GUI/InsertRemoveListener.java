package de.hpi.bpt.scylla.GUI;

import java.util.function.Consumer;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


public class InsertRemoveListener implements DocumentListener {
	
	private Consumer<DocumentEvent> co;
	
	public InsertRemoveListener(Consumer<DocumentEvent> c) {
		co = c;
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		co.accept(e);
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		co.accept(e);
	}

}
