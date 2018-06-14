package de.hpi.bpt.scylla.GUI;

/**
 * Central interface for managing the whole form
 * @author Leon Bein
 * TODO
 */
public interface FormManager<ModelType> {
	
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
	
	public interface SetObserver<ValueType>{
		public void notifyCreation(ValueType id);
		public void notifyDeletion(ValueType id);
		public void notifyRenaming(ValueType id, ValueType newid);
	}
	
	public ModelType getModel();

}
