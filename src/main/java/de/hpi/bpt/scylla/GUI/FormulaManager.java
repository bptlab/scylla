package de.hpi.bpt.scylla.GUI;

import java.util.List;

import javax.swing.ListModel;

public interface FormulaManager {
	
	public void setSaved(boolean b);
	public boolean isChangeFlag();
	public void setChangeFlag(boolean b);
	
	public List<ListModel<String>> getTimetableObserverList();

}
