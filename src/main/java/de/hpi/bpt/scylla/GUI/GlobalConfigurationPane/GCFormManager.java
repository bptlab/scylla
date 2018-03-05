package de.hpi.bpt.scylla.GUI.GlobalConfigurationPane;

import java.util.List;

import de.hpi.bpt.scylla.GUI.FormManager;

/**
 * Central interface for managing the whole form
 * @author Leon Bein
 * TODO
 */
public interface GCFormManager extends FormManager{
	
	/**
	 * List of comboboxes, that show timetable selection and therefore have to be updated
	 * @return
	 */
	public List<SetObserver<String>> getTimetableObserverList();
	
	/**
	 * List of current timetable ids, e.g. for creation of new comboboxes
	 * @return
	 */
	public List<String> getTimetables();
	


}
