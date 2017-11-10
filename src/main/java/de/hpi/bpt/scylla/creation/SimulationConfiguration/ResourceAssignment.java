package de.hpi.bpt.scylla.creation.SimulationConfiguration;

import org.jdom2.Element;

import de.hpi.bpt.scylla.creation.ElementLink;
import de.hpi.bpt.scylla.creation.GlobalConfiguration.GlobalConfigurationCreator.ResourceType;

public class ResourceAssignment extends ElementLink{
	
	private String id;
	private ResourceType type;
	
	/**
	 * Link constructor
	 * @param toLink
	 */
	public ResourceAssignment(Element toLink, ResourceType t) {
		super(toLink);
		id = el.getAttributeValue("id");
		if(t == null)System.err.println("Warning: Type id of assignment definition with id "+id+" is not defined.");
		else if(!id.equals(t.getId()))System.err.println("Warning: Type id "+t.getId()+" does not fit assignment definition id "+id+".");
		type = t;
	}
	
	public ResourceAssignment(ResourceType t){
		super(new Element("resource",stdNsp));
		this.id = t.getId();
		setAttribute("id",id);
		setAmount(0);
	}
	
	public void setResourceType(ResourceType t) {
		if(t == null)throw new NullPointerException("Cannot set resource type: is null.");
		if(!id.equals(t.getId()))System.err.println("Warning: Type id "+t.getId()+" does not fit assignment definition id "+id+".");
		else type = t;
	}

	public ResourceType getType(){return type;}
	public String getId(){return id;}
	
	public String getAmount(){
		return el.getAttributeValue("amount");
	}
	
	public void setAmount(int i){
		setAttribute("amount",i);
	}
	
	public void setAssignmentPriority(int i){
		getAssignmentPriorityElement().setText(i+"");
	}
	
	public void removeAssignmentDefinition(){
		el.removeChild("assignmentDefinition",nsp);
	}
	
	private Element getAssignmentPriorityElement(){
		Element assignmentDefinition = getAssignmentDefinition();
		Element e = assignmentDefinition.getChild("priority",nsp);
		if(e == null){
			e = new Element("priority",nsp);
			assignmentDefinition.addContent(e);
		}
		return e;
	}
	
	private Element getAssignmentDefinition(){
		Element e = el.getChild("assignmentDefinition",nsp);
		if(e == null){
			e = new Element("assignmentDefinition",nsp);
			el.addContent(e);
		}
		return e;
	}

}
