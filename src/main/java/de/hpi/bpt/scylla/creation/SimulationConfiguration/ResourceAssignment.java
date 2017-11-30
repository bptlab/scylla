package de.hpi.bpt.scylla.creation.SimulationConfiguration;

import org.jdom2.Element;

import de.hpi.bpt.scylla.creation.ElementLink;
import de.hpi.bpt.scylla.creation.GlobalConfiguration.GlobalConfigurationCreator.ResourceType;

/**
 * Wrapper class for Task resource assignment elements
 * @author Leon Bein
 *
 */
public class ResourceAssignment extends ElementLink{
	
	/**Resource id*/
	private String id;
	/**Global configuration resource type, if determinable*/
	private ResourceType type;
	
	/**
	 * Link constructor, links to an existing resource assignment element.
	 * A GC ResourceType may also be linked, but does not have to.
	 * No errors are thrown, but warnings are shown if no resource type is specified or if the specified type does not match the element
	 * @param toLink
	 * @param t
	 */
	public ResourceAssignment(Element toLink, ResourceType t) {
		super(toLink);
		id = el.getAttributeValue("id");
		if(t == null)System.err.println("Warning: Type id of assignment definition with id "+id+" is not defined.");
		else if(!id.equals(t.getId()))System.err.println("Warning: Type id "+t.getId()+" does not fit assignment definition id "+id+".");
		type = t;
	}
	
	/**
	 * Constructor, creates new object and element with given GC resource type and amount 1
	 * @param t : GC resource type, a GCC should be specified in order get one
	 */
	public ResourceAssignment(ResourceType t){
		super(new Element("resource",stdNsp));
		this.id = t.getId();
		setAttribute("id",id);
		setAmount(1);
	}
	
	/**
	 * Sets the resource type if it fits to the elements id, otherwise shows a warning.
	 * Used to set correct type even after object has already been created
	 * @param t : GC resource type, not null
	 * @throws NullPointerException when t == null
	 */
	public void setResourceType(ResourceType t) {
		if(t == null)throw new NullPointerException("Cannot set resource type: is null.");
		if(!id.equals(t.getId()))System.err.println("Warning: Type id "+t.getId()+" does not fit assignment definition id "+id+".");
		else type = t;
	}

	/**
	 * @return this objects resource type if specified
	 */
	public ResourceType getType(){return type;}
	/**
	 * @return this objects resource id
	 */
	public String getId(){return id;}
	
	/**
	 * @return The amount of resources assigned in this assignment
	 */
	public String getAmount(){
		return el.getAttributeValue("amount");
	}
	
	/**
	 * Sets th amount of resources assigned in this assignment
	 * @param i : Amount to be set
	 */
	public void setAmount(int i){
		setAttribute("amount",i);
	}
	
	/**
	 * Creates an assignment priority element if none exists and sets its value.
	 * TODO Assignment priorities are a currently a not used concept
	 * @param i : Priority to be set
	 */
	public void setAssignmentPriority(int i){
		getAssignmentPriorityElement().setText(i+"");
	}
	
	/**
	 * Removes the assignment definition element
	 */
	public void removeAssignmentDefinition(){
		el.removeChild("assignmentDefinition",nsp);
	}
	
	/**
	 * Creates a new assignment priority element if none exists 
	 * and returns the assignment priority element
	 * @return The element responsible for assignment priorities
	 */
	private Element getAssignmentPriorityElement(){
		Element assignmentDefinition = getAssignmentDefinition();
		Element e = assignmentDefinition.getChild("priority",nsp);
		if(e == null){
			e = new Element("priority",nsp);
			assignmentDefinition.addContent(e);
		}
		return e;
	}
	
	/**
	 * Creates a new assignment definition element if none exists 
	 * and returns the assignment definition element
	 * @return The element responsible for assignment definitions in general
	 */
	private Element getAssignmentDefinition(){
		Element e = el.getChild("assignmentDefinition",nsp);
		if(e == null){
			e = new Element("assignmentDefinition",nsp);
			el.addContent(e);
		}
		return e;
	}

}
