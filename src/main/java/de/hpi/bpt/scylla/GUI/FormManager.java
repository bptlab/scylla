package de.hpi.bpt.scylla.GUI;

import java.util.List;

import javax.swing.JComboBox;

/**
 * Central interface for managing the whole form
 * @author Leon Bein
 * TODO
 */
public interface FormManager {
	
	/**
	 * Sets if the form is saved or not
	 * @param b
	 */
	public void setSaved(boolean b);
	
	/**
	 * Determines whether programmatic changes are done to the form that are no user input 
	 * @return true if changes are done, false if not
	 */
	public boolean isChangeFlag();
	
	/**
	 * Sets the flag determining if there are any programmatic changes to the form;
	 * Called with true before the process, called with false afterwards
	 * @param b
	 */
	public void setChangeFlag(boolean b);
	
	/**
	 * List of comboboxes, that show timetable selection and therefore have to be updated
	 * @return
	 */
	public List<JComboBox<String>> getTimetableObserverList();
	
	/**
	 * List of current timetable ids, e.g. for creation of new comboboxes
	 * @return
	 */
	public List<String> getTimetables();

}
