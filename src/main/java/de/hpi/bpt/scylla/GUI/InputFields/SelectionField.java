package de.hpi.bpt.scylla.GUI.InputFields;

import javax.swing.JComboBox;

import de.hpi.bpt.scylla.GUI.FormManager;
import de.hpi.bpt.scylla.GUI.FormManager.SetObserver;

public abstract class SelectionField<DataType> extends InputField<DataType, JComboBox<DataType>> implements SetObserver<DataType>{

	public SelectionField(FormManager fm, DataType[] data) {
		super(fm,data);
	}
	

	@Override
	protected JComboBox<DataType> createComponent(Object data) {
		@SuppressWarnings("unchecked")
		JComboBox<DataType> box = new JComboBox<DataType>((DataType[]) data);
		return box;
	}
	
	/**
	 * Does not do anything, as never called due to override of {@link InputField#createComponent(Object)}
	 */
	@Override
	protected JComboBox<DataType> createComponent() {return null;}
	

	@SuppressWarnings("unchecked")
	@Override
	public DataType getValue() {
		return (DataType) getComponent().getSelectedItem();
	}

	@Override
	public void setValue(DataType v) {
		getComponent().setSelectedItem(v);
	}

	@Override
	public void reset() {
		getComponent().setSelectedIndex(0);
	}

	@Override
	public void clear() {
		setValue(null);
	}
	

	@Override
	public void notifyCreation(DataType id) {
		getComponent().addItem(id);
	}

	@Override
	public void notifyDeletion(DataType id) {
		DataType sel = getSavedValue();
		getComponent().removeItem(id);
		if(sel != null && sel.equals(id))clear();
	}

	@Override
	public void notifyRenaming(DataType id, DataType newid) {
		DataType sel = getSavedValue();
		getComponent().removeItem(id);
		getComponent().addItem(newid);
		//Reset combobox selection to null, as it will automatically change when the set is changed
		if(sel == null)clear();
		//If the current item is the selected one, it is also renamed in the box
		else if(sel.equals(id))setValue(newid);
	}
		

}
