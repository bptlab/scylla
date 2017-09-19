package de.hpi.bpt.scylla.GUI;

import java.util.List;

import javax.swing.JComboBox;

public interface FormManager {
	
	public void setSaved(boolean b);
	public boolean isChangeFlag();
	public void setChangeFlag(boolean b);
	
	public List<JComboBox<String>> getTimetableObserverList();
	public List<String> getTimetables();

}
