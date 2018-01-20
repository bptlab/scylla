package de.hpi.bpt.scylla.GUI.InputFields;

import java.beans.PropertyChangeEvent;
import java.lang.reflect.ParameterizedType;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.JComponent;
import javax.swing.JFormattedTextField;

import de.hpi.bpt.scylla.GUI.FormManager;

public abstract class InputField<DataType,ComponentType extends JComponent>{
	

	/**Form manager to notify on changes.*/
	private FormManager formManager;

	/**The Ui component*/
	protected ComponentType component;
	
	/**
	 * Initialization constructor, always call when subclassing <br>
	 * Initializes form manager, component, listener and onEdit behavior
	 * @param fm: Form manager to be notified on changes
	 */
	public InputField(FormManager fm) {
		setFormManager(fm);
		component = createComponent();
		createListener();
		if(getSavedValue() != null)setValue(getSavedValue());
	}
	
	/**
	 * Creates a listener for the specific class of the component, 
	 * must be overridden, if type is not known in {@link ListenerType}
	 */
	protected void createListener() {
		switch(ListenerType.of(component.getClass())) {
		case PROPERTYCHANGE:
			component.addPropertyChangeListener("value", (PropertyChangeEvent evt)->{onChange();});return;
		default: return;
		}
	}
	
	/**Create the component according to the component type*/
	protected abstract ComponentType createComponent();
	
	/**Method to be called when an event is fired by the listener*/
	protected void onChange() {
		if(formManager.isChangeFlag())return;
		if(!getValue().equals(getSavedValue())){
			onEdit(getValue());
			formManager.setSaved(false);
		}
	}

	/**
	 * Notifies that there has been an edit and a value has been accepted
	 * and that should change the saved value. By default, just setSavedValue is called.
	 * @param value
	 */
	protected void onEdit(DataType value) {
		setSavedValue(value);
	}

	/**
	 * @return The value that is currently inside the component (not the saved value)
	 */
	protected abstract DataType getValue();
	/**
	 * Sets the value of the component (not the saved value)
	 * @param v: Value to be set
	 */
	protected abstract void setValue(DataType v);
	
	/**
	 * @return The value that is currently saved (not the one in the component)
	 */
	protected abstract DataType getSavedValue();
	/**
	 * Sets the value that is saved (not the one in the component)
	 * @param v: Value to be set
	 */
	protected abstract void setSavedValue(DataType v);
	

	
	/**
	 * @return The form manager that is notified on changes
	 */
	public FormManager getFormManager() {
		return formManager;
	}
	/**
	 * Sets the form manager that is notified on changes, usually called in constructor.
	 * @param formManager: Form manager to be set
	 */
	public void setFormManager(FormManager formManager) {
		this.formManager = formManager;
	}
	
	/**
	 * @return The ui component for this field
	 */
	public ComponentType getComponent() {
		return component;
	}
	
	
	/***
	 * Creates an example input field
	 * @param fm
	 * @param setSaved
	 * @param getSaved
	 * @return
	 */
	public static InputField<Integer,JFormattedTextField> createIntegerField(FormManager fm, Consumer<Integer> setSaved, Supplier<Integer> getSaved){
		return new NumberField<Integer>(fm) {

			@Override
			protected Integer getSavedValue() {
				return getSaved.get();
			}

			@Override
			protected void setSavedValue(Integer v) {
				setSaved.accept(v);
			}
			
		};
	}
	
	
	/**
	 * Internal class to determine listeners on different component class
	 * @author Leon Bein
	 */
	protected enum ListenerType{
		/**Property change listener*/
		PROPERTYCHANGE
		;
		
		/**
		 * Determines which type to use for a given component clas
		 * @param componentClass: Class of component to determine listener type for
		 * @return An enum representing the needed listener type
		 */
		protected static ListenerType of(Class<? extends JComponent> componentClass) {
			switch(componentClass.getName()) {
			case "javax.swing.JFormattedTextField": 
				return PROPERTYCHANGE;
			default: 
				return null;
			}
		}
	}
	
	/**
	 * @return The data type class. Must be overridden by subclass, if not parameterized anymore or first parameter is not the data type.
	 */
	@SuppressWarnings("unchecked")
	protected Class<DataType> getDataTypeClass(){
		
		return (Class<DataType>)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
	}

	
}
