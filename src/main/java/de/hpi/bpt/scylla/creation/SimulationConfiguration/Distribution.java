package de.hpi.bpt.scylla.creation.SimulationConfiguration;

import java.util.Arrays;

import org.jdom2.Element;

import de.hpi.bpt.scylla.creation.ElementLink;

/**
 * Class for wrapping distribution xml elements inside simulation configurations, 
 * e.g. arrival or duration distributions 
 * The class contains a table with all keywords and datatypes of all known distributions
 * @author Leon Bein
 *
 */
public class Distribution extends ElementLink{
	
	/**Set of datatypes, where "entryset" is meant for afP-distribution and other of that kind that might follow*/
	public enum AttributeType{DOUBLE,INT,ENTRYSET}
	
	/**Table of all distribution keywords (ids), their display name, their attribute keywords and attribute types.
	 * (Note: id and name are listet here without an additional "Distribution" at the end, for better readability)
	 * @author Leon Bein
	 */
	public enum DistributionType{
		BINOMIAL(		"binomial",					"Binomial", 		new String[]{"probability","amount"}, 	new AttributeType[]{AttributeType.DOUBLE,AttributeType.INT}),
		CONSTANT(		"constant", 				"Constant", 		new String[]{"constantValue"}, 			new AttributeType[]{AttributeType.DOUBLE}),
		ERLANG(			"erlang", 					"Erlang", 			new String[]{"order","mean"}, 			new AttributeType[]{AttributeType.INT,AttributeType.DOUBLE}),
		EXPONENTIAL(	"exponential", 				"Exponential", 		new String[]{"mean"}, 					new AttributeType[]{AttributeType.DOUBLE}),
		TRIANGULAR(		"triangular", 				"Triangluar", 		new String[]{"lower","peak","upper"}, 	new AttributeType[]{AttributeType.DOUBLE,AttributeType.DOUBLE,AttributeType.DOUBLE}),
		NORMAL(			"normal", 					"Normal", 			new String[]{"mean","standardDeviation"}, new AttributeType[]{AttributeType.DOUBLE,AttributeType.DOUBLE}),
		POISSON(		"poisson", 					"Poisson", 			new String[]{"mean"}, 					new AttributeType[]{AttributeType.DOUBLE}),
		UNIFORM(		"uniform", 					"Uniform", 			new String[]{"lower","upper"}, 			new AttributeType[]{AttributeType.DOUBLE,AttributeType.DOUBLE}),
		DISCRETE(		"arbitraryFiniteProbability","Discrete", 		new String[]{}, 						new AttributeType[]{}),
		;
		/**Identification String inside the SC*/
		public final String id;
		/**Display name, free to not stick to any standard*/
		public final String displayName;
		/**Attribute keywords*/
		public final String[] attributes;
		/**Attribute types*/
		public final AttributeType[] types;
		/**
		 * Constructor for distribution types
		 * @param i : Id
		 * @param n : Displayname
		 * @param a : Attribute keywords
		 * @param t : Attribute types
		 * @throws ExceptionInInitializerError When the number of attribute keywords and attribute types does not match,
		 * to indicate that there was an error in definition.
		 */
		private DistributionType(String i, String n, String[] a, AttributeType[] t){
			if(a.length != t.length)throw new ExceptionInInitializerError("Quasi compile error, no valid distribution type definition");
			id = i+"Distribution";
			displayName = n+" Distribution";
			attributes = a;
			types = t;
		}
		
		/**
		 * Returns the displayname; helps with UI elements that have to display distribution types (e.g. checkboxes)
		 */
		@Override
		public String toString(){
			return displayName;
		}
		
		/**
		 * Better valueOf function, returns the distribution type with given id
		 * @param id : The id to compare
		 * @return A matching distribution type or null if an error occurs
		 * @throws IllegalArgumentException when there is no matching type
		 */
		public static DistributionType get(String id) {
			for(DistributionType type : values()) {
				if(type.id.equals(id))return type;
			}
			throw new IllegalArgumentException("No enum constant of "+DistributionType.class+" with id "+id+" exists.");
		}
	}
	//----- End of type classes ---- begin of distribution class -----
	
	/**
	 * A distributions type; final
	 */
	protected final DistributionType type;
	
	/**
	 * Link constructor/factory
	 * Creates a wrapper distribution object around the the given xml element
	 * @param toLink : The element to link with
	 * @return An object of class Distribution or one of its subclasses according to the parsed type
	 * @throws ExceptionInInitializerError to indicate that the element could not be parsed/ does not contain a valid distribution type
	 */
	public static Distribution create(Element toLink){
		try{
			DistributionType type = DistributionType.get(toLink.getName());
			switch(type){
			case DISCRETE : return new DiscreteDistribution(toLink);
			default : return new Distribution(toLink, type);
			}
		}catch(IllegalArgumentException e){
			throw new ExceptionInInitializerError("Error at creating distribution wrapper - "+toLink.getName()+" is not a valid distribution id");
		}
	}
	
	/**
	 * "Creation" constructor/factory
	 * Creates a new distribution wrapper and element for the given type
	 * @param type : Type the distribution shall have
	 * @return An object of class Distribution or one of its subclasses according to the parsed type
	 */
	public static Distribution create(DistributionType type){
		switch(type){
		case DISCRETE : return new DiscreteDistribution();
		default : return new Distribution( type);
		}
	}
	
	/**
	 * Link constructor
	 * Links the distribution to a given element and sets it's type;
	 * should only be called if the type is already parsed and validated (as it might be a type for any of the subclasses)
	 * @param toLink : Element to link with
	 * @param t : Type of the element
	 */
	protected Distribution(Element toLink, DistributionType t) {
		super(toLink);
		type = t;
	}
	
	/**
	 * "Creation" constructor
	 * Creates a new distribution object and xml element with a given type, 
	 * should only be called if the type is validated to belong to this class and not to one of its subclasses. 
	 * Also creates empty child elements for each attribute defined for the given type.
	 * @param t : Type the distribution shall have
	 */
	protected Distribution(DistributionType t){
		super(new Element(t.id,stdNsp));
		type = t;
		for(String attribute : type.attributes){
			el.addContent(new Element(attribute,nsp));
		}
		
	}
	
	/**
	 * Sets the value of a attribute with given index.
	 * Checks for out of bounds but not for negative ones
	 * @param index : Attribute index
	 * @param value : Value to be set
	 */
	public void setAttribute(int index, Object value){
		if(index < type.attributes.length)
		el.getChild(type.attributes[index], nsp).setText(value.toString());
	}
	
	/**
	 * Sets the value of a attribute with given id.
	 * Checks if the id exists
	 * @param id : Attribute id
	 * @param value : Value to be set
	 */
	public void setAttribute(String id, Object value){
		if(Arrays.asList(type.attributes).contains(id))
			el.getChild(id, nsp).setText(value.toString());
	}
	
	/**
	 * Returns value of the attribute with given index
	 * Checks if index  is valid
	 * @param index : Index of attribute to be returned
	 * @return String value of child element with given index or null if index is invalid.
	 */
	public String getAttribute(int index){
		if(index >= 0 && index < type.attributes.length)return el.getChildText(type.attributes[index], nsp);
		return null;
		
	}
	
	/**
	 * Returns value of the attribute with given id
	 * Checks if id  is valid
	 * @param id : Id of attribute to be returned
	 * @return String value of child element with given id or null if id is invalid.
	 */
	public String getAttribute(String id){
		if(Arrays.asList(type.attributes).contains(id))return el.getChildText(id, nsp);
		return null;
	}
	
	/**
	 * @return The type of this distribution
	 */
	public DistributionType getType(){
		return type;
	}
	
	/**
	 * Subclass for discrete distribution (name may differ)
	 * This distribution shows elementary different behavior and therefore got its own class.
	 * It is entry based, meaning that it has a dynamic number of children with dynamic values and frequencies.
	 * @author Leon Bein
	 *
	 */
	public static class DiscreteDistribution extends Distribution{

		/**
		 * Link constructor, should only be called if the elements type is definitely of type descrete 
		 * @param toLink : Element to link with
		 */
		private DiscreteDistribution(Element toLink) {
			super(toLink,DistributionType.DISCRETE);
		}
		
		/**
		 * Creates a new distribution object and element of type descrete
		 */
		private DiscreteDistribution() {
			super(DistributionType.DISCRETE);
		}
		
		/**
		 * Adds an entry for a given value-frequency-pair
		 * @param value : The value to be set.
		 * @param frequency : Frequency of that value
		 */
		public void addEntry(Object value, Double frequency){
			Element entry = new Element("entry",nsp);
			entry.setAttribute("value", value.toString());
			entry.setAttribute("frequency",frequency.toString());
			el.addContent(entry);
		}
		
		/**
		 * Removes the first entry with given value
		 * @param value : Value to be removed.
		 */
		public void removeEntry(Object value){
			for(Element e : el.getChildren()){
				if(e.getAttributeValue("value").equals(value.toString())){
					el.removeContent(e);
					break;
				}
			}
		}
		
		/**
		 * Removes entry with given index
		 * @param index : Index of value to be removed.
		 */
		public void removeEntry(int index){
			el.removeContent(index);
		}
		
		/**
		 * @return Number of entries in the entryset
		 */
		public int getEntrySize(){
			return el.getChildren().size();
		}
		
		/**
		 * Returns the value at a given index
		 * @param index : Index to look at
		 * @return String value at index
		 */
		public String getValue(int index){
			return el.getChildren().get(index).getAttributeValue("value");
		}
		
		/**
		 * Returns the frequency at a given index
		 * @param index : Index to look at
		 * @return frequency value as double (NOT in %)
		 */
		public double getFrequency(int index){
			return Double.parseDouble(el.getChildren().get(index).getAttributeValue("frequency"));
		}
		
		/**
		 * Set the value at a given index
		 * @param index : Index to set value at
		 * @param value : Value to be saved at that index (can be of any class as long as it can be converted to String and vice versa)
		 */
		public void setValue(int index, Object value){
			el.getChildren().get(index).setAttribute("value",value.toString());
		}
		
		/**
		 * Set the value at a given index
		 * @param index : Index to set value at
		 * @param value : Frequency of the value at the given index, double
		 */
		public void setFrequency(int index, double value){
			el.getChildren().get(index).setAttribute("frequency",value+"");
		}
		
	}
	
}
