package de.hpi.bpt.scylla.creation.SimulationConfiguration;

import org.jdom2.Element;

import de.hpi.bpt.scylla.creation.ElementLink;

public class Resource extends ElementLink{

	private String name;
	private String id;

	/**
	 * Link constructor
	 * @param toLink
	 */
	private Resource(Element toLink) {
		super(toLink);
		// TODO create linking constructor
	}
	
	public Resource(String i, String n){
		super(new Element("resource",stdNsp));
		id = i;
		setAttribute("id",id);
		name = n;
		setAmount(0);
	}

	public String getName(){return name;}
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
