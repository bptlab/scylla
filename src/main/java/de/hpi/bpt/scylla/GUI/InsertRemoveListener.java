package de.hpi.bpt.scylla.GUI;

import java.util.function.Consumer;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Utility listener for textfields and similar to listen for insert or remove changes
 * @author Leon Bein
 *
 */
public class InsertRemoveListener implements DocumentListener {
	
	/**
	 * Functional interface that is called when an event is fired
	 */
	private Consumer<DocumentEvent> co;
	
	/**
	 * Constructor
	 * @param c : Functional interface that is called in order to process the DocumentEvents; recommended to use lambdas here
	 */
	public InsertRemoveListener(Consumer<DocumentEvent> c) {
		co = c;
	}

	/**
	 * Needs to be overriden by interface conditions, does not do anything
	 */
	@Override
	public void changedUpdate(DocumentEvent e) {
	}

	/**
	 * Calls the functional interface
	 */
	@Override
	public void insertUpdate(DocumentEvent e) {
		co.accept(e);
	}

	/**
	 * Calls the functional interface
	 */
	@Override
	public void removeUpdate(DocumentEvent e) {
		co.accept(e);
	}

}
